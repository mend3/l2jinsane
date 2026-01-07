package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.SiegeGuardAI;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

public final class SiegeGuard extends Attackable {
    public SiegeGuard(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null)
            synchronized (this) {
                if (this._ai == null)
                    this._ai = new SiegeGuardAI(this);
                return this._ai;
            }
        return ai;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return (attacker != null && attacker.getActingPlayer() != null && getCastle() != null && getCastle().getSiege().isInProgress() && !getCastle().getSiege().checkSides(attacker.getActingPlayer().getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER));
    }

    public boolean hasRandomAnimation() {
        return false;
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player)) {
            if (!isAlikeDead() && Math.abs(player.getZ() - getZ()) < 600)
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

    public void addDamageHate(Creature attacker, int damage, int aggro) {
        if (attacker instanceof SiegeGuard)
            return;
        super.addDamageHate(attacker, damage, aggro);
    }

    public void reduceHate(Creature target, int amount) {
        stopHating(target);
        setTarget(null);
        getAI().setIntention(IntentionType.ACTIVE);
    }

    public boolean isGuard() {
        return true;
    }

    public int getDriftRange() {
        return 20;
    }
}
