package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestDeleteMacro extends L2GameClientPacket {
    private int _id;

    protected void readImpl() {
        this._id = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.getMacroList().deleteMacro(this._id);
    }
}
