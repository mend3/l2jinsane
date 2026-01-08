package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Agathion;
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
        this._objectId = this.readD();
        this._originX = this.readD();
        this._originY = this.readD();
        this._originZ = this.readD();
        this._isShiftAction = this.readC() != 0;
    }

    protected void runImpl() {
        Player player = (this.getClient()).getPlayer();
        if (player != null) {
            if (player.isInObserverMode()) {
                player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (player.getActiveRequester() != null) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else {
                WorldObject target = player.getTargetId() == this._objectId ? player.getTarget() : World.getInstance().getObject(this._objectId);
                if (target == null) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                } else {
                    Player targetPlayer = target.getActingPlayer();
                    if (targetPlayer != null && targetPlayer.getDuelState() == Duel.DuelState.DEAD) {
                        player.sendPacket(SystemMessageId.OTHER_PARTY_IS_FROZEN);
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                    } else if (target instanceof Agathion) {
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                    } else {
                        if (this._isShiftAction) {
                            target.onActionShift(player);
                        } else {
                            target.onAction(player);
                        }

                    }
                }
            }
        }
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
