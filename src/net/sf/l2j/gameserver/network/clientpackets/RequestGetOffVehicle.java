package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.GetOffVehicle;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestGetOffVehicle extends L2GameClientPacket {
    private int _boatId;

    private int _x;

    private int _y;

    private int _z;

    protected void readImpl() {
        this._boatId = readD();
        this._x = readD();
        this._y = readD();
        this._z = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (!activeChar.isInBoat() || activeChar.getBoat().getObjectId() != this._boatId || activeChar.getBoat().isMoving() || !activeChar.isInsideRadius(this._x, this._y, this._z, 1000, true, false)) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, this._boatId));
        activeChar.setBoat(null);
        sendPacket(ActionFailed.STATIC_PACKET);
        activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), this._boatId, this._x, this._y, this._z));
        activeChar.setXYZ(this._x, this._y, this._z + 50);
        activeChar.revalidateZone(true);
    }
}
