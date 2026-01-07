package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

public final class HolyThing extends Folk {
    public HolyThing(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public boolean isAttackable() {
        return false;
    }

    public void onForcedAttack(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill) {
    }

    public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
    }
}
