package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.stat.CreatureStat;
import net.sf.l2j.gameserver.skills.Formulas;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class CreatureStatus {
    protected static final byte REGEN_FLAG_CP = 4;
    private static final byte REGEN_FLAG_HP = 1;
    private static final byte REGEN_FLAG_MP = 2;
    private final Creature _activeChar;
    private final Set<Creature> _statusListener = ConcurrentHashMap.newKeySet();
    protected byte _flagsRegenActive = 0;
    private double _currentHp = 0.0F;
    private double _currentMp = 0.0F;
    private Future<?> _regTask;

    public CreatureStatus(Creature activeChar) {
        this._activeChar = activeChar;
    }

    public final void addStatusListener(Creature object) {
        if (object != this.getActiveChar()) {
            this._statusListener.add(object);
        }
    }

    public final void removeStatusListener(Creature object) {
        this._statusListener.remove(object);
    }

    public final Set<Creature> getStatusListener() {
        return this._statusListener;
    }

    public void reduceCp(int value) {
    }

    public void reduceHp(double value, Creature attacker) {
        this.reduceHp(value, attacker, true, false, false);
    }

    public void reduceHp(double value, Creature attacker, boolean isHpConsumption) {
        this.reduceHp(value, attacker, true, false, isHpConsumption);
    }

    public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
        if (!this.getActiveChar().isDead()) {
            if (this.getActiveChar().isInvul()) {
                if (attacker != this.getActiveChar()) {
                    return;
                }

                if (!isDOT && !isHPConsumption) {
                    return;
                }
            }

            if (attacker != null) {
                Player attackerPlayer = attacker.getActingPlayer();
                if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage()) {
                    return;
                }
            }

            if (!isDOT && !isHPConsumption) {
                this.getActiveChar().stopEffectsOnDamage(awake);
                if (this.getActiveChar().isStunned() && Rnd.get(10) == 0) {
                    this.getActiveChar().stopStunning(true);
                }

                if (this.getActiveChar().isImmobileUntilAttacked()) {
                    this.getActiveChar().stopImmobileUntilAttacked(null);
                }
            }

            if (value > (double) 0.0F) {
                this.setCurrentHp(Math.max(this.getCurrentHp() - value, 0.0F));
            }

            if (this.getActiveChar().getCurrentHp() < (double) 0.5F && this.getActiveChar().isMortal()) {
                this.getActiveChar().abortAttack();
                this.getActiveChar().abortCast();
                this.getActiveChar().doDie(attacker);
            }

        }
    }

    public void reduceMp(double value) {
        this.setCurrentMp(Math.max(this.getCurrentMp() - value, 0.0F));
    }

    public final synchronized void startHpMpRegeneration() {
        if (this._regTask == null && !this.getActiveChar().isDead()) {
            int period = Formulas.getRegeneratePeriod(this.getActiveChar());
            this._regTask = ThreadPool.scheduleAtFixedRate(() -> this.doRegeneration(), period, period);
        }

    }

    public final synchronized void stopHpMpRegeneration() {
        if (this._regTask != null) {
            this._regTask.cancel(false);
            this._regTask = null;
            this._flagsRegenActive = 0;
        }

    }

    public double getCurrentCp() {
        return 0.0F;
    }

    public void setCurrentCp(double newCp) {
    }

    public final double getCurrentHp() {
        return this._currentHp;
    }

    public final void setCurrentHp(double newHp) {
        this.setCurrentHp(newHp, true);
    }

    public void setCurrentHp(double newHp, boolean broadcastPacket) {
        double maxHp = this.getActiveChar().getMaxHp();
        synchronized (this) {
            if (this.getActiveChar().isDead()) {
                return;
            }

            if (newHp >= maxHp) {
                this._currentHp = maxHp;
                this._flagsRegenActive &= -2;
                if (this._flagsRegenActive == 0) {
                    this.stopHpMpRegeneration();
                }
            } else {
                this._currentHp = newHp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 1);
                this.startHpMpRegeneration();
            }
        }

        if (broadcastPacket) {
            this.getActiveChar().broadcastStatusUpdate();
        }

    }

    public final void setCurrentHpMp(double newHp, double newMp) {
        this.setCurrentHp(newHp, false);
        this.setCurrentMp(newMp, true);
    }

    public final double getCurrentMp() {
        return this._currentMp;
    }

    public final void setCurrentMp(double newMp) {
        this.setCurrentMp(newMp, true);
    }

    public final void setCurrentMp(double newMp, boolean broadcastPacket) {
        int maxMp = this.getActiveChar().getStat().getMaxMp();
        synchronized (this) {
            if (this.getActiveChar().isDead()) {
                return;
            }

            if (newMp >= (double) maxMp) {
                this._currentMp = maxMp;
                this._flagsRegenActive &= -3;
                if (this._flagsRegenActive == 0) {
                    this.stopHpMpRegeneration();
                }
            } else {
                this._currentMp = newMp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 2);
                this.startHpMpRegeneration();
            }
        }

        if (broadcastPacket) {
            this.getActiveChar().broadcastStatusUpdate();
        }

    }

    protected void doRegeneration() {
        CreatureStat charstat = this.getActiveChar().getStat();
        if (this.getCurrentHp() < (double) charstat.getMaxHp()) {
            this.setCurrentHp(this.getCurrentHp() + Formulas.calcHpRegen(this.getActiveChar()), false);
        }

        if (this.getCurrentMp() < (double) charstat.getMaxMp()) {
            this.setCurrentMp(this.getCurrentMp() + Formulas.calcMpRegen(this.getActiveChar()), false);
        }

        this.getActiveChar().broadcastStatusUpdate();
    }

    public Creature getActiveChar() {
        return this._activeChar;
    }
}
