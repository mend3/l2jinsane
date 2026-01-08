package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SiegeFlag extends Npc {
    private final Clan _clan;

    public SiegeFlag(Player player, int objectId, NpcTemplate template) {
        super(objectId, template);
        this._clan = player.getClan();
        if (this._clan != null) {
            this._clan.setFlag(this);
        }

    }

    public boolean isAutoAttackable(Creature attacker) {
        return true;
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this._clan != null) {
                this._clan.setFlag(null);
            }

            return true;
        }
    }

    public void onForcedAttack(Player player) {
        this.onAction(player);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public boolean hasRandomAnimation() {
        return false;
    }

    public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill) {
        if (this._clan != null && this.isScriptValue(0)) {
            this._clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
            this.setScriptValue(1);
            ThreadPool.schedule(() -> this.setScriptValue(0), 30000L);
        }

        super.reduceCurrentHp(damage, attacker, skill);
    }

    public void addFuncsToNewCharacter() {
    }
}
