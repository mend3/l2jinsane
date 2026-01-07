package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestBuyItem extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 8;

    private int _listId;

    private IntIntHolder[] _items = null;

    protected void readImpl() {
        this._listId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 8 != this._buf.remaining())
            return;
        this._items = new IntIntHolder[count];
        for (int i = 0; i < count; i++) {
            int itemId = readD();
            int cnt = readD();
            if (itemId < 1 || cnt < 1) {
                this._items = null;
                return;
            }
            this._items[i] = new IntIntHolder(itemId, cnt);
        }
    }

    protected void runImpl() {
        if (this._items == null)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        NpcBuyList buyList = BuyListManager.getInstance().getBuyList(this._listId);
        if (buyList == null)
            return;
        double castleTaxRate = 0.0D;
        Npc merchant = null;
        if (buyList.getNpcId() > 0) {
            WorldObject target = player.getTarget();
            if (target instanceof net.sf.l2j.gameserver.model.actor.instance.Merchant)
                merchant = (Npc) target;
            if (merchant == null || !buyList.isNpcAllowed(merchant.getNpcId()) || !merchant.canInteract(player))
                return;
            if (merchant.getCastle() != null)
                castleTaxRate = merchant.getCastle().getTaxRate();
        }
        int subTotal = 0;
        int slots = 0;
        int weight = 0;
        for (IntIntHolder i : this._items) {
            int price = -1;
            Product product = buyList.getProductByItemId(i.getId());
            if (product == null)
                return;
            if (!product.getItem().isStackable() && i.getValue() > 1) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                return;
            }
            price = product.getPrice();
            if (i.getId() >= 3960 && i.getId() <= 4026)
                price = (int) (price * Config.RATE_SIEGE_GUARDS_PRICE);
            if (price < 0)
                return;
            if (price == 0 && !player.isGM())
                return;
            if (product.hasLimitedStock())
                if (i.getValue() > product.getCount())
                    return;
            if (Integer.MAX_VALUE / i.getValue() < price)
                return;
            price = (int) (price * (1.0D + castleTaxRate));
            subTotal += i.getValue() * price;
            if (subTotal > Integer.MAX_VALUE)
                return;
            weight += i.getValue() * product.getItem().getWeight();
            if (!product.getItem().isStackable()) {
                slots += i.getValue();
            } else if (player.getInventory().getItemByItemId(i.getId()) == null) {
                slots++;
            }
        }
        if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight(weight)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
            return;
        }
        if (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
            return;
        }
        if (subTotal < 0 || !player.reduceAdena("Buy", subTotal, player.getCurrentFolk(), false)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            return;
        }
        for (IntIntHolder i : this._items) {
            Product product = buyList.getProductByItemId(i.getId());
            if (product != null)
                if (product.hasLimitedStock()) {
                    if (product.decreaseCount(i.getValue()))
                        player.getInventory().addItem("Buy", i.getId(), i.getValue(), player, merchant);
                } else {
                    player.getInventory().addItem("Buy", i.getId(), i.getValue(), player, merchant);
                }
        }
        if (merchant != null) {
            if (merchant.getCastle() != null)
                merchant.getCastle().addToTreasury((int) (subTotal * castleTaxRate));
            String htmlFolder = "";
            if (merchant instanceof net.sf.l2j.gameserver.model.actor.instance.Fisherman) {
                htmlFolder = "fisherman";
            } else if (merchant instanceof net.sf.l2j.gameserver.model.actor.instance.Merchant) {
                htmlFolder = "merchant";
            }
            if (!htmlFolder.isEmpty()) {
                String content = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-bought.htm");
                if (content != null) {
                    NpcHtmlMessage html = new NpcHtmlMessage(merchant.getObjectId());
                    html.setHtml(content);
                    html.replace("%objectId%", merchant.getObjectId());
                    player.sendPacket(html);
                }
            }
        }
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
        player.sendPacket(new ItemList(player, true));
    }
}
