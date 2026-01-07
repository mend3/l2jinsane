package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

public final class MutedFolk extends Folk {
    public MutedFolk(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else if (!canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void onActionShift(Player player) {
        if (player.isGM())
            sendNpcInfos(player);
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player)) {
            if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else if (!canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }
}
