package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Duel;

public class NpcStatus extends CreatureStatus {
    public NpcStatus(Npc activeChar) {
        super(activeChar);
    }

    public void reduceHp(double value, Creature attacker) {
        this.reduceHp(value, attacker, true, false, false);
    }

    public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
        if (!this.getActiveChar().isDead()) {
            if (attacker != null) {
                Player attackerPlayer = attacker.getActingPlayer();
                if (attackerPlayer != null && attackerPlayer.isInDuel()) {
                    attackerPlayer.setDuelState(Duel.DuelState.INTERRUPTED);
                }
            }

            super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
        }
    }

    public Npc getActiveChar() {
        return (Npc) super.getActiveChar();
    }
}
