package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.manor.SeedProduction;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBuySeed extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 8;

    private int _manorId;

    private List<IntIntHolder> _items;

    protected void readImpl() {
        this._manorId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 8 != this._buf.remaining())
            return;
        this._items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int itemId = readD();
            int cnt = readD();
            if (cnt < 1 || itemId < 1) {
                this._items = null;
                return;
            }
            this._items.add(new IntIntHolder(itemId, cnt));
        }
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.MANOR))
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._items == null) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        CastleManorManager manor = CastleManorManager.getInstance();
        if (manor.isUnderMaintenance()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Castle castle = CastleManager.getInstance().getCastleById(this._manorId);
        if (castle == null) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Folk folk = player.getCurrentFolk();
        if (!(folk instanceof net.sf.l2j.gameserver.model.actor.instance.ManorManagerNpc) || !folk.canInteract(player) || folk.getCastle() != castle) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        int totalPrice = 0;
        int slots = 0;
        int totalWeight = 0;
        Map<Integer, SeedProduction> _productInfo = new HashMap<>();
        for (IntIntHolder ih : this._items) {
            SeedProduction sp = manor.getSeedProduct(this._manorId, ih.getId(), false);
            if (sp == null || sp.getPrice() <= 0 || sp.getAmount() < ih.getValue() || Integer.MAX_VALUE / ih.getValue() < sp.getPrice()) {
                sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            totalPrice += sp.getPrice() * ih.getValue();
            if (totalPrice > Integer.MAX_VALUE) {
                sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            Item template = ItemTable.getInstance().getTemplate(ih.getId());
            totalWeight += ih.getValue() * template.getWeight();
            if (!template.isStackable()) {
                slots += ih.getValue();
            } else if (player.getInventory().getItemByItemId(ih.getId()) == null) {
                slots++;
            }
            _productInfo.put(ih.getId(), sp);
        }
        if (!player.getInventory().validateWeight(totalWeight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
            return;
        }
        if (!player.getInventory().validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
            return;
        }
        if (totalPrice < 0 || player.getAdena() < totalPrice) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            return;
        }
        for (IntIntHolder i : this._items) {
            SeedProduction sp = _productInfo.get(i.getId());
            int price = sp.getPrice() * i.getValue();
            if (!sp.decreaseAmount(i.getValue()) || !player.reduceAdena("Buy", price, player, false)) {
                totalPrice -= price;
                continue;
            }
            player.addItem("Buy", i.getId(), i.getValue(), folk, true);
        }
        if (totalPrice > 0) {
            castle.addToTreasuryNoTax(totalPrice);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addItemNumber(totalPrice));
        }
    }
}
