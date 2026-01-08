package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Agathion;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket {
    private int _objectId;
    private int _originX;
    private int _originY;
    private int _originZ;
    private boolean _isShiftAction;

    protected void readImpl() {
        this._objectId = this.readD();
        this._originX = this.readD();
        this._originY = this.readD();
        this._originZ = this.readD();
        this._isShiftAction = this.readC() != 0;
    }

    protected void runImpl() {
        Player activeChar = (this.getClient()).getPlayer();
        if (activeChar != null) {
            if (activeChar.isInObserverMode()) {
                activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            } else {
                WorldObject target;
                if (activeChar.getTargetId() == this._objectId) {
                    target = activeChar.getTarget();
                } else {
                    target = World.getInstance().getObject(this._objectId);
                }

                if (target == null) {
                    activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                } else if (target instanceof Agathion) {
                    activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                } else {
                    if (activeChar.getTarget() != target) {
                        target.onAction(activeChar);
                    } else if (target.getObjectId() != activeChar.getObjectId() && !activeChar.isInStoreMode() && activeChar.getActiveRequester() == null) {
                        target.onForcedAttack(activeChar);
                    } else {
                        this.sendPacket(ActionFailed.STATIC_PACKET);
                    }

                }
            }
        }
    }
}
