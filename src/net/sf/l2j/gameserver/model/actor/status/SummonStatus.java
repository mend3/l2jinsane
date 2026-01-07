package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SummonStatus extends PlayableStatus {
    public SummonStatus(Summon activeChar) {
        super(activeChar);
    }

    public void reduceHp(double value, Creature attacker) {
        reduceHp(value, attacker, true, false, false);
    }

    public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
        if (getActiveChar().isDead())
            return;
        Player owner = getActiveChar().getOwner();
        if (attacker != null) {
            Player attackerPlayer = attacker.getActingPlayer();
            if (attackerPlayer != null && (owner == null || owner.getDuelId() != attackerPlayer.getDuelId()))
                attackerPlayer.setDuelState(Duel.DuelState.INTERRUPTED);
        }
        super.reduceHp(value, attacker, awake, isDOT, isHPConsumption);
        if (attacker != null) {
            if (!isDOT && owner != null)
                owner.sendPacket(SystemMessage.getSystemMessage((getActiveChar() instanceof net.sf.l2j.gameserver.model.actor.instance.Servitor) ? SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1 : SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1).addCharName(attacker).addNumber((int) value));
            getActiveChar().getAI().notifyEvent(AiEventType.ATTACKED, attacker);
        }
    }

    public Summon getActiveChar() {
        return (Summon) super.getActiveChar();
    }
}
