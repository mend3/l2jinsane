package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket {
    private int _objectId;

    private int _originX;

    private int _originY;

    private int _originZ;

    private boolean _isShiftAction;

    protected void readImpl() {
        this._objectId = readD();
        this._originX = readD();
        this._originY = readD();
        this._originZ = readD();
        this._isShiftAction = (readC() != 0);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isInObserverMode()) {
            player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.getActiveRequester() != null) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        WorldObject target = (player.getTargetId() == this._objectId) ? player.getTarget() : World.getInstance().getObject(this._objectId);
        if (target == null) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Player targetPlayer = target.getActingPlayer();
        if (targetPlayer != null && targetPlayer.getDuelState() == Duel.DuelState.DEAD) {
            player.sendPacket(SystemMessageId.OTHER_PARTY_IS_FROZEN);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (target instanceof net.sf.l2j.gameserver.model.actor.instance.Agathion) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (this._isShiftAction) {
            target.onActionShift(player);
        } else {
            target.onAction(player);
        }
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
