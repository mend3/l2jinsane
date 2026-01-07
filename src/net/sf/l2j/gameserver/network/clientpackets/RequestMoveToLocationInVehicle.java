package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocationInVehicle;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestMoveToLocationInVehicle extends L2GameClientPacket {
    private int _boatId;

    private int _targetX;

    private int _targetY;

    private int _targetZ;

    private int _originX;

    private int _originY;

    private int _originZ;

    protected void readImpl() {
        this._boatId = readD();
        this._targetX = readD();
        this._targetY = readD();
        this._targetZ = readD();
        this._originX = readD();
        this._originY = readD();
        this._originZ = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ) {
            activeChar.sendPacket(new StopMoveInVehicle(activeChar, this._boatId));
            return;
        }
        if (activeChar.isAttackingNow() && activeChar.getAttackType() == WeaponType.BOW) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activeChar.isSitting() || activeChar.isMovementDisabled()) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activeChar.getSummon() != null) {
            activeChar.sendPacket(SystemMessageId.RELEASE_PET_ON_BOAT);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activeChar.isInBoat()) {
            Boat boat = activeChar.getBoat();
            if (boat.getObjectId() != this._boatId) {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
        } else {
            Boat boat = BoatManager.getInstance().getBoat(this._boatId);
            if (boat == null || !boat.isInsideRadius(activeChar, 300, true, false)) {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            activeChar.setBoat(boat);
        }
        activeChar.getBoatPosition().set(this._targetX, this._targetY, this._targetZ);
        activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, this._targetX, this._targetY, this._targetZ, this._originX, this._originY, this._originZ));
    }
}
