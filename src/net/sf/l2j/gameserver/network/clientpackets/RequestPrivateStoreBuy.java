package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.HashSet;
import java.util.Set;

public final class RequestPrivateStoreBuy extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 12;

    private int _storePlayerId;

    private Set<ItemRequest> _items = null;

    protected void readImpl() {
        this._storePlayerId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 12 != this._buf.remaining())
            return;
        this._items = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int objectId = readD();
            long cnt = readD();
            int price = readD();
            if (objectId < 1 || cnt < 1L || price < 0) {
                this._items = null;
                return;
            }
            this._items.add(new ItemRequest(objectId, (int) cnt, price));
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
        if (storePlayer.getStoreType() != StoreType.SELL && storePlayer.getStoreType() != StoreType.PACKAGE_SELL)
            return;
        TradeList storeList = storePlayer.getSellList();
        if (storeList == null)
            return;
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (storePlayer.getStoreType() == StoreType.PACKAGE_SELL && storeList.getItems().size() > this._items.size())
            return;
        if (!storeList.privateStoreBuy(player, this._items))
            return;
        if (storeList.getItems().isEmpty()) {
            storePlayer.setStoreType(StoreType.NONE);
            storePlayer.broadcastUserInfo();
        }
    }
}
