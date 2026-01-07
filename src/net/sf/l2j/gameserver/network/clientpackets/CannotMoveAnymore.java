package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public final class CannotMoveAnymore extends L2GameClientPacket {
    private int _x;

    private int _y;

    private int _z;

    private int _heading;

    protected void readImpl() {
        this._x = readD();
        this._y = readD();
        this._z = readD();
        this._heading = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.hasAI())
            player.getAI().notifyEvent(AiEventType.ARRIVED_BLOCKED, new SpawnLocation(this._x, this._y, this._z, this._heading));
    }
}
