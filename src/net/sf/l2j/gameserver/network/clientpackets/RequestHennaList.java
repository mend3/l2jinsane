package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.HennaEquipList;

public final class RequestHennaList extends L2GameClientPacket {
    private int _unknown;

    protected void readImpl() {
        this._unknown = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.sendPacket(new HennaEquipList(player));
    }
}
