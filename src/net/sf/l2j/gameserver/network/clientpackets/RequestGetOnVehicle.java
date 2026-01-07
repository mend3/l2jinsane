package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;

public final class RequestGetOnVehicle extends L2GameClientPacket {
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
        Boat boat;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isInBoat()) {
            boat = activeChar.getBoat();
            if (boat.getObjectId() != this._boatId) {
                sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
        } else {
            boat = BoatManager.getInstance().getBoat(this._boatId);
            if (boat == null || boat.isMoving() || !activeChar.isInsideRadius(boat, 1000, true, false)) {
                sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
        }
        activeChar.getBoatPosition().set(this._x, this._y, this._z, activeChar.getHeading());
        activeChar.setBoat(boat);
        activeChar.broadcastPacket(new GetOnVehicle(activeChar.getObjectId(), boat.getObjectId(), this._x, this._y, this._z));
        activeChar.setXYZ(boat.getX(), boat.getY(), boat.getZ());
        activeChar.revalidateZone(true);
    }
}
