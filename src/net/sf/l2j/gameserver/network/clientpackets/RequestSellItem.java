package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

public final class RequestSellItem extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 12;

    private int _listId;

    private IntIntHolder[] _items = null;

    protected void readImpl() {
        this._listId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 12 != this._buf.remaining())
            return;
        this._items = new IntIntHolder[count];
        for (int i = 0; i < count; i++) {
            int objectId = readD();
            int itemId = readD();
            int cnt = readD();
            if (objectId < 1 || itemId < 1 || cnt < 1) {
                this._items = null;
                return;
            }
            this._items[i] = new IntIntHolder(objectId, cnt);
        }
    }

    protected void runImpl() {
        if (this._items == null)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Npc merchant = (player.getTarget() instanceof net.sf.l2j.gameserver.model.actor.instance.Merchant || player.getTarget() instanceof net.sf.l2j.gameserver.model.actor.instance.MercenaryManagerNpc) ? (Npc) player.getTarget() : null;
        if ((merchant == null || !merchant.canInteract(player)) && !player.isSellItemCommunity())
            return;
        if (merchant != null &&
                this._listId > 1000000)
            if (merchant.getTemplate().getNpcId() != this._listId - 1000000)
                return;
        int totalPrice = 0;
        for (IntIntHolder i : this._items) {
            ItemInstance item = player.checkItemManipulation(i.getId(), i.getValue());
            if (item != null && item.isSellable()) {
                int price = item.getReferencePrice() / 2;
                totalPrice += price * i.getValue();
                if (Integer.MAX_VALUE / i.getValue() < price || totalPrice > Integer.MAX_VALUE)
                    return;
                item = player.getInventory().destroyItem("Sell", i.getId(), i.getValue(), player, merchant);
            }
        }
        player.addAdena("Sell", totalPrice, merchant, false);
        if (merchant != null) {
            String htmlFolder = "";
            if (merchant instanceof net.sf.l2j.gameserver.model.actor.instance.Fisherman) {
                htmlFolder = "fisherman";
            } else if (merchant instanceof net.sf.l2j.gameserver.model.actor.instance.Merchant) {
                htmlFolder = "merchant";
            }
            if (!htmlFolder.isEmpty()) {
                String content = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");
                if (content != null) {
                    NpcHtmlMessage html = new NpcHtmlMessage(merchant.getObjectId());
                    html.setHtml(content);
                    html.replace("%objectId%", merchant.getObjectId());
                    player.sendPacket(html);
                }
            }
        }
        player.setIsUsingSellItemCommunity(false);
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
        player.sendPacket(new ItemList(player, true));
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
