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
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.Formulas;

public class PlayerStatus extends PlayableStatus {
    private double _currentCp = 0.0F;

    public PlayerStatus(Player activeChar) {
        super(activeChar);
    }

    public final void reduceCp(int value) {
        if (this.getCurrentCp() > (double) value) {
            this.setCurrentCp(this.getCurrentCp() - (double) value);
        } else {
            this.setCurrentCp(0.0F);
        }

    }

    public final void reduceHp(double value, Creature attacker) {
        this.reduceHp(value, attacker, true, false, false, false);
    }

    public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
        this.reduceHp(value, attacker, awake, isDOT, isHPConsumption, false);
    }

    public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP) {
        if (!this.getActiveChar().isDead()) {
            if (this.getActiveChar().isInvul()) {
                if (attacker != this.getActiveChar()) {
                    return;
                }

                if (!isDOT && !isHPConsumption) {
                    return;
                }
            }

            if (!isHPConsumption) {
                this.getActiveChar().stopEffectsOnDamage(awake);
                this.getActiveChar().forceStandUp();
                if (!isDOT && this.getActiveChar().isStunned() && Rnd.get(10) == 0) {
                    this.getActiveChar().stopStunning(true);
                }
            }

            if (attacker != null && attacker != this.getActiveChar()) {
                Player attackerPlayer = attacker.getActingPlayer();
                if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage()) {
                    return;
                }

                if (this.getActiveChar().isInDuel()) {
                    Duel.DuelState playerState = this.getActiveChar().getDuelState();
                    if (playerState == Duel.DuelState.DEAD || playerState == Duel.DuelState.WINNER) {
                        return;
                    }

                    if (attackerPlayer == null || attackerPlayer.getDuelId() != this.getActiveChar().getDuelId() || playerState != Duel.DuelState.DUELLING) {
                        this.getActiveChar().setDuelState(Duel.DuelState.INTERRUPTED);
                    }
                }

                int fullValue = (int) value;
                int tDmg = 0;
                Summon summon = this.getActiveChar().getSummon();
                if (summon instanceof Servitor && MathUtil.checkIfInRange(900, this.getActiveChar(), summon, true)) {
                    tDmg = (int) value * (int) this.getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0F, null, null) / 100;
                    tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
                    if (tDmg > 0) {
                        summon.reduceCurrentHp(tDmg, attacker, null);
                        value -= tDmg;
                        fullValue = (int) value;
                    }
                }

                if (!ignoreCP && attacker instanceof Playable) {
                    if (this.getCurrentCp() >= value) {
                        this.setCurrentCp(this.getCurrentCp() - value);
                        value = 0.0F;
                    } else {
                        value -= this.getCurrentCp();
                        this.setCurrentCp(0.0F, false);
                    }
                }

                if (fullValue > 0 && !isDOT) {
                    this.getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(attacker).addNumber(fullValue));
                    if (tDmg > 0 && attackerPlayer != null) {
                        attackerPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(fullValue).addNumber(tDmg));
                    }
                }
            }

            if (value > (double) 0.0F) {
                value = this.getCurrentHp() - value;
                if (value <= (double) 0.0F) {
                    if (this.getActiveChar().isInDuel()) {
                        if (this.getActiveChar().getDuelState() == Duel.DuelState.DUELLING) {
                            this.getActiveChar().disableAllSkills();
                            this.stopHpMpRegeneration();
                            if (attacker != null) {
                                attacker.getAI().setIntention(IntentionType.ACTIVE);
                                attacker.sendPacket(ActionFailed.STATIC_PACKET);
                            }

                            DuelManager.getInstance().onPlayerDefeat(this.getActiveChar());
                        }

                        value = 1.0F;
                    } else {
                        value = 0.0F;
                    }
                }

                this.setCurrentHp(value);
            }

            if (this.getActiveChar().getCurrentHp() < (double) 0.5F && this.getActiveChar().isMortal()) {
                this.getActiveChar().abortAttack();
                this.getActiveChar().abortCast();
                if (this.getActiveChar().isInOlympiadMode()) {
                    this.stopHpMpRegeneration();
                    this.getActiveChar().setIsDead(true);
                    Summon summon = this.getActiveChar().getSummon();
                    if (summon != null) {
                        summon.getAI().setIntention(IntentionType.IDLE, null);
                    }

                    return;
                }

                this.getActiveChar().doDie(attacker);
                if (!Config.DISABLE_TUTORIAL) {
                    QuestState qs = this.getActiveChar().getQuestState("Tutorial");
                    if (qs != null) {
                        qs.getQuest().notifyEvent("CE30", null, this.getActiveChar());
                    }
                }
            }

        }
    }

    public final void setCurrentHp(double newHp, boolean broadcastPacket) {
        super.setCurrentHp(newHp, broadcastPacket);
        if (!Config.DISABLE_TUTORIAL && this.getCurrentHp() <= (double) this.getActiveChar().getStat().getMaxHp() * 0.3) {
            QuestState qs = this.getActiveChar().getQuestState("Tutorial");
            if (qs != null) {
                qs.getQuest().notifyEvent("CE45", null, this.getActiveChar());
            }
        }

    }

    public final double getCurrentCp() {
        return this._currentCp;
    }

    public final void setCurrentCp(double newCp) {
        this.setCurrentCp(newCp, true);
    }

    public final void setCurrentCp(double newCp, boolean broadcastPacket) {
        int maxCp = this.getActiveChar().getStat().getMaxCp();
        synchronized (this) {
            if (this.getActiveChar().isDead()) {
                return;
            }

            if (newCp < (double) 0.0F) {
                newCp = 0.0F;
            }

            if (newCp >= (double) maxCp) {
                this._currentCp = maxCp;
                this._flagsRegenActive &= -5;
                if (this._flagsRegenActive == 0) {
                    this.stopHpMpRegeneration();
                }
            } else {
                this._currentCp = newCp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 4);
                this.startHpMpRegeneration();
            }
        }

        if (broadcastPacket) {
            this.getActiveChar().broadcastStatusUpdate();
        }

    }

    protected void doRegeneration() {
        PlayerStat pcStat = this.getActiveChar().getStat();
        if (this.getCurrentCp() < (double) pcStat.getMaxCp()) {
            this.setCurrentCp(this.getCurrentCp() + Formulas.calcCpRegen(this.getActiveChar()), false);
        }

        if (this.getCurrentHp() < (double) pcStat.getMaxHp()) {
            this.setCurrentHp(this.getCurrentHp() + Formulas.calcHpRegen(this.getActiveChar()), false);
        }

        if (this.getCurrentMp() < (double) pcStat.getMaxMp()) {
            this.setCurrentMp(this.getCurrentMp() + Formulas.calcMpRegen(this.getActiveChar()), false);
        }

        this.getActiveChar().broadcastStatusUpdate();
    }

    public Player getActiveChar() {
        return (Player) super.getActiveChar();
    }
}
