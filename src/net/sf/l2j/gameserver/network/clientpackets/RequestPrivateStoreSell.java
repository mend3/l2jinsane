package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestPrivateStoreSell extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 20;

    private int _storePlayerId;

    private ItemRequest[] _items = null;

    protected void readImpl() {
        this._storePlayerId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 20 != this._buf.remaining())
            return;
        this._items = new ItemRequest[count];
        for (int i = 0; i < count; i++) {
            int objectId = readD();
            int itemId = readD();
            readH();
            readH();
            long cnt = readD();
            int price = readD();
            if (objectId < 1 || itemId < 1 || cnt < 1L || price < 0) {
                this._items = null;
                return;
            }
            this._items[i] = new ItemRequest(objectId, itemId, (int) cnt, price);
        }
    }

    protected void runImpl() {
        if (this._items == null)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isCursedWeaponEquipped())
            return;
        Player storePlayer = World.getInstance().getPlayer(this._storePlayerId);
        if (storePlayer == null)
            return;
        if (!player.isInsideRadius(storePlayer, 150, true, false))
            return;
        if (storePlayer.getStoreType() != StoreType.BUY)
            return;
        TradeList storeList = storePlayer.getBuyList();
        if (storeList == null)
            return;
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (!storeList.privateStoreSell(player, this._items))
            return;
        if (storeList.getItems().isEmpty()) {
            storePlayer.setStoreType(StoreType.NONE);
            storePlayer.broadcastUserInfo();
        }
    }
}
