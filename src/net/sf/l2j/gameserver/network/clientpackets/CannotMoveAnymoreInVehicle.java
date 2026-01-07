package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;

public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket {
    private int _boatId;

    private int _x;

    private int _y;

    private int _z;

    private int _heading;

    protected void readImpl() {
        this._boatId = readD();
        this._x = readD();
        this._y = readD();
        this._z = readD();
        this._heading = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isInBoat() && player.getBoat().getObjectId() == this._boatId) {
            player.getBoatPosition().set(this._x, this._y, this._z, this._heading);
            player.broadcastPacket(new StopMoveInVehicle(player, this._boatId));
        }
    }
}
