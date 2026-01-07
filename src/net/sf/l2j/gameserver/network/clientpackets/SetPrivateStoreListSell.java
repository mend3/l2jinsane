package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class SetPrivateStoreListSell extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 12;

    private boolean _packageSale;

    private Item[] _items = null;

    protected void readImpl() {
        this._packageSale = (readD() == 1);
        int count = readD();
        if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * 12 != this._buf.remaining())
            return;
        this._items = new Item[count];
        for (int i = 0; i < count; i++) {
            int itemId = readD();
            long cnt = readD();
            int price = readD();
            if (itemId < 1 || cnt < 1L || price < 0) {
                this._items = null;
                return;
            }
            this._items[i] = new Item(itemId, (int) cnt, price);
        }
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._items == null) {
            player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            player.setStoreType(StoreType.NONE);
            player.broadcastUserInfo();
            player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
            return;
        }
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (AttackStanceTaskManager.getInstance().isInAttackStance(player) || player.isCastingNow() || player.isCastingSimultaneouslyNow() || player.isInDuel()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
            return;
        }
        if (player.isInsideZone(ZoneId.MULTI_FUNCTION) && !Config.STORE_ZONE) {
            player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
            player.sendMessage("You cannot start store while inside Multifunction zone.");
            return;
        }
        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
            player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
            return;
        }
        if (this._items.length > player.getPrivateSellStoreLimit()) {
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
            player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
            return;
        }
        TradeList tradeList = player.getSellList();
        tradeList.clear();
        tradeList.setPackaged(this._packageSale);
        int totalCost = player.getAdena();
        for (Item i : this._items) {
            if (!i.addToTradeList(tradeList)) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
                player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
                return;
            }
            totalCost = (int) (totalCost + i.getPrice());
            if (totalCost > Integer.MAX_VALUE) {
                player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
                player.sendPacket(new PrivateStoreManageListSell(player, this._packageSale));
                return;
            }
        }
        player.sitDown();
        player.setStoreType(this._packageSale ? StoreType.PACKAGE_SELL : StoreType.SELL);
        player.broadcastUserInfo();
        player.broadcastPacket(new PrivateStoreMsgSell(player));
    }

    private record Item(int _itemId, int _count, int _price) {

        public boolean addToTradeList(TradeList list) {
                if (Integer.MAX_VALUE / this._count < this._price)
                    return false;
                list.addItem(this._itemId, this._count, this._price);
                return true;
            }

            public long getPrice() {
                return ((long) this._count * this._price);
            }
        }
}
