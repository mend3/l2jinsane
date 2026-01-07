package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.PackageSendableList;

public final class RequestPackageSendableItemList extends L2GameClientPacket {
    private int _objectID;

    protected void readImpl() {
        this._objectID = readD();
    }

    public void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        ItemInstance[] items = player.getInventory().getAvailableItems(true, false);
        if (items == null)
            return;
        sendPacket(new PackageSendableList(items, this._objectID));
    }
}
