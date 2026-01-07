package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Merchant;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ShopPreviewInfo;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

import java.util.HashMap;
import java.util.Map;

public final class RequestPreviewItem extends L2GameClientPacket {
    private Map<Integer, Integer> _itemList;

    private int _unk;

    private int _listId;

    private int _count;

    private int[] _items;

    protected void readImpl() {
        this._unk = readD();
        this._listId = readD();
        this._count = readD();
        if (this._count < 0) {
            this._count = 0;
        } else if (this._count > 100) {
            return;
        }
        this._items = new int[this._count];
        for (int i = 0; i < this._count; i++)
            this._items[i] = readD();
    }

    protected void runImpl() {
        if (this._items == null)
            return;
        if (this._count < 1 || this._listId >= 4000000) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        WorldObject target = activeChar.getTarget();
        if (!activeChar.isGM() && (target == null || !(target instanceof Merchant) || !activeChar.isInsideRadius(target, 150, false, false)))
            return;
        Merchant merchant = (target instanceof Merchant) ? (Merchant) target : null;
        if (merchant == null)
            return;
        NpcBuyList buyList = BuyListManager.getInstance().getBuyList(this._listId);
        if (buyList == null)
            return;
        int totalPrice = 0;
        this._listId = buyList.getListId();
        this._itemList = new HashMap<>();
        for (int i = 0; i < this._count; i++) {
            int itemId = this._items[i];
            Product product = buyList.getProductByItemId(itemId);
            if (product == null)
                return;
            Item template = product.getItem();
            if (template != null) {
                int slot = Inventory.getPaperdollIndex(template.getBodyPart());
                if (slot >= 0) {
                    if (this._itemList.containsKey(slot)) {
                        activeChar.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
                        return;
                    }
                    this._itemList.put(slot, itemId);
                    totalPrice += Config.WEAR_PRICE;
                    if (totalPrice > Integer.MAX_VALUE)
                        return;
                }
            }
        }
        if (totalPrice < 0 || !activeChar.reduceAdena("Wear", totalPrice, activeChar.getCurrentFolk(), true)) {
            activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return;
        }
        if (!this._itemList.isEmpty()) {
            activeChar.sendPacket(new ShopPreviewInfo(this._itemList));
            ThreadPool.schedule(() -> {
                activeChar.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
                activeChar.sendPacket(new UserInfo(activeChar));
            }, (Config.WEAR_DELAY * 1000L));
        }
    }
}
