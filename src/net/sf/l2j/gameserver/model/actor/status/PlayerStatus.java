package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.Formulas;

public class PlayerStatus extends PlayableStatus {
    private double _currentCp = 0.0D;

    public PlayerStatus(Player activeChar) {
        super(activeChar);
    }

    public final void reduceCp(int value) {
        if (getCurrentCp() > value) {
            setCurrentCp(getCurrentCp() - value);
        } else {
            setCurrentCp(0.0D);
        }
    }

    public final void reduceHp(double value, Creature attacker) {
        reduceHp(value, attacker, true, false, false, false);
    }

    public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
        reduceHp(value, attacker, awake, isDOT, isHPConsumption, false);
    }

    public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP) {
        if (getActiveChar().isDead())
            return;
        if (getActiveChar().isInvul()) {
            if (attacker != getActiveChar())
                return;
            if (!isDOT && !isHPConsumption)
                return;
        }
        if (!isHPConsumption) {
            getActiveChar().stopEffectsOnDamage(awake);
            getActiveChar().forceStandUp();
            if (!isDOT)
                if (getActiveChar().isStunned() && Rnd.get(10) == 0)
                    getActiveChar().stopStunning(true);
        }
        if (attacker != null && attacker != getActiveChar()) {
            Player attackerPlayer = attacker.getActingPlayer();
            if (attackerPlayer != null)
                if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
                    return;
            if (getActiveChar().isInDuel()) {
                Duel.DuelState playerState = getActiveChar().getDuelState();
                if (playerState == Duel.DuelState.DEAD || playerState == Duel.DuelState.WINNER)
                    return;
                if (attackerPlayer == null || attackerPlayer.getDuelId() != getActiveChar().getDuelId() || playerState != Duel.DuelState.DUELLING)
                    getActiveChar().setDuelState(Duel.DuelState.INTERRUPTED);
            }
            int fullValue = (int) value;
            int tDmg = 0;
            Summon summon = getActiveChar().getSummon();
            if (summon != null && summon instanceof net.sf.l2j.gameserver.model.actor.instance.Servitor && MathUtil.checkIfInRange(900, getActiveChar(), summon, true)) {
                tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0D, null, null) / 100;
                tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
                if (tDmg > 0) {
                    summon.reduceCurrentHp(tDmg, attacker, null);
                    value -= tDmg;
                    fullValue = (int) value;
                }
            }
            if (!ignoreCP && attacker instanceof Playable)
                if (getCurrentCp() >= value) {
                    setCurrentCp(getCurrentCp() - value);
                    value = 0.0D;
                } else {
                    value -= getCurrentCp();
                    setCurrentCp(0.0D, false);
                }
            if (fullValue > 0 && !isDOT) {
                getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(attacker).addNumber(fullValue));
                if (tDmg > 0 && attackerPlayer != null)
                    attackerPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(fullValue).addNumber(tDmg));
            }
        }
        if (value > 0.0D) {
            value = getCurrentHp() - value;
            if (value <= 0.0D)
                if (getActiveChar().isInDuel()) {
                    if (getActiveChar().getDuelState() == Duel.DuelState.DUELLING) {
                        getActiveChar().disableAllSkills();
                        stopHpMpRegeneration();
                        if (attacker != null) {
                            attacker.getAI().setIntention(IntentionType.ACTIVE);
                            attacker.sendPacket(ActionFailed.STATIC_PACKET);
                        }
                        DuelManager.getInstance().onPlayerDefeat(getActiveChar());
                    }
                    value = 1.0D;
                } else {
                    value = 0.0D;
                }
            setCurrentHp(value);
        }
        if (getActiveChar().getCurrentHp() < 0.5D && getActiveChar().isMortal()) {
            getActiveChar().abortAttack();
            getActiveChar().abortCast();
            if (getActiveChar().isInOlympiadMode()) {
                stopHpMpRegeneration();
                getActiveChar().setIsDead(true);
                Summon summon = getActiveChar().getSummon();
                if (summon != null)
                    summon.getAI().setIntention(IntentionType.IDLE, null);
                return;
            }
            getActiveChar().doDie(attacker);
            if (!Config.DISABLE_TUTORIAL) {
                QuestState qs = getActiveChar().getQuestState("Tutorial");
                if (qs != null)
                    qs.getQuest().notifyEvent("CE30", null, getActiveChar());
            }
        }
    }

    public final void setCurrentHp(double newHp, boolean broadcastPacket) {
        super.setCurrentHp(newHp, broadcastPacket);
        if (!Config.DISABLE_TUTORIAL && getCurrentHp() <= getActiveChar().getStat().getMaxHp() * 0.3D) {
            QuestState qs = getActiveChar().getQuestState("Tutorial");
            if (qs != null)
                qs.getQuest().notifyEvent("CE45", null, getActiveChar());
        }
    }

    public final double getCurrentCp() {
        return this._currentCp;
    }

    public final void setCurrentCp(double newCp) {
        setCurrentCp(newCp, true);
    }

    public final void setCurrentCp(double newCp, boolean broadcastPacket) {
        int maxCp = getActiveChar().getStat().getMaxCp();
        synchronized (this) {
            if (getActiveChar().isDead())
                return;
            if (newCp < 0.0D)
                newCp = 0.0D;
            if (newCp >= maxCp) {
                this._currentCp = maxCp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive & 0xFFFFFFFB);
                if (this._flagsRegenActive == 0)
                    stopHpMpRegeneration();
            } else {
                this._currentCp = newCp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 0x4);
                startHpMpRegeneration();
            }
        }
        if (broadcastPacket)
            getActiveChar().broadcastStatusUpdate();
    }

    protected void doRegeneration() {
        PlayerStat pcStat = getActiveChar().getStat();
        if (getCurrentCp() < pcStat.getMaxCp())
            setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()), false);
        if (getCurrentHp() < pcStat.getMaxHp())
            setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
        if (getCurrentMp() < pcStat.getMaxMp())
            setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
        getActiveChar().broadcastStatusUpdate();
    }

    public Player getActiveChar() {
        return (Player) super.getActiveChar();
    }
}
