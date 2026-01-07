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
    private double _currentHp = 0.0D;
    private double _currentMp = 0.0D;
    private Future<?> _regTask;

    public CreatureStatus(Creature activeChar) {
        this._activeChar = activeChar;
    }

    public final void addStatusListener(Creature object) {
        if (object == getActiveChar())
            return;
        this._statusListener.add(object);
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
        reduceHp(value, attacker, true, false, false);
    }

    public void reduceHp(double value, Creature attacker, boolean isHpConsumption) {
        reduceHp(value, attacker, true, false, isHpConsumption);
    }

    public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
        if (getActiveChar().isDead())
            return;
        if (getActiveChar().isInvul()) {
            if (attacker != getActiveChar())
                return;
            if (!isDOT && !isHPConsumption)
                return;
        }
        if (attacker != null) {
            Player attackerPlayer = attacker.getActingPlayer();
            if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
                return;
        }
        if (!isDOT && !isHPConsumption) {
            getActiveChar().stopEffectsOnDamage(awake);
            if (getActiveChar().isStunned() && Rnd.get(10) == 0)
                getActiveChar().stopStunning(true);
            if (getActiveChar().isImmobileUntilAttacked())
                getActiveChar().stopImmobileUntilAttacked(null);
        }
        if (value > 0.0D)
            setCurrentHp(Math.max(getCurrentHp() - value, 0.0D));
        if (getActiveChar().getCurrentHp() < 0.5D && getActiveChar().isMortal()) {
            getActiveChar().abortAttack();
            getActiveChar().abortCast();
            getActiveChar().doDie(attacker);
        }
    }

    public void reduceMp(double value) {
        setCurrentMp(Math.max(getCurrentMp() - value, 0.0D));
    }

    public final synchronized void startHpMpRegeneration() {
        if (this._regTask == null && !getActiveChar().isDead()) {
            int period = Formulas.getRegeneratePeriod(getActiveChar());
            this._regTask = ThreadPool.scheduleAtFixedRate(() -> doRegeneration(), period, period);
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
        return 0.0D;
    }

    public void setCurrentCp(double newCp) {
    }

    public final double getCurrentHp() {
        return this._currentHp;
    }

    public final void setCurrentHp(double newHp) {
        setCurrentHp(newHp, true);
    }

    public void setCurrentHp(double newHp, boolean broadcastPacket) {
        double maxHp = getActiveChar().getMaxHp();
        synchronized (this) {
            if (getActiveChar().isDead())
                return;
            if (newHp >= maxHp) {
                this._currentHp = maxHp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive & 0xFFFFFFFE);
                if (this._flagsRegenActive == 0)
                    stopHpMpRegeneration();
            } else {
                this._currentHp = newHp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 0x1);
                startHpMpRegeneration();
            }
        }
        if (broadcastPacket)
            getActiveChar().broadcastStatusUpdate();
    }

    public final void setCurrentHpMp(double newHp, double newMp) {
        setCurrentHp(newHp, false);
        setCurrentMp(newMp, true);
    }

    public final double getCurrentMp() {
        return this._currentMp;
    }

    public final void setCurrentMp(double newMp) {
        setCurrentMp(newMp, true);
    }

    public final void setCurrentMp(double newMp, boolean broadcastPacket) {
        int maxMp = getActiveChar().getStat().getMaxMp();
        synchronized (this) {
            if (getActiveChar().isDead())
                return;
            if (newMp >= maxMp) {
                this._currentMp = maxMp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive & 0xFFFFFFFD);
                if (this._flagsRegenActive == 0)
                    stopHpMpRegeneration();
            } else {
                this._currentMp = newMp;
                this._flagsRegenActive = (byte) (this._flagsRegenActive | 0x2);
                startHpMpRegeneration();
            }
        }
        if (broadcastPacket)
            getActiveChar().broadcastStatusUpdate();
    }

    protected void doRegeneration() {
        CreatureStat charstat = getActiveChar().getStat();
        if (getCurrentHp() < charstat.getMaxHp())
            setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
        if (getCurrentMp() < charstat.getMaxMp())
            setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
        getActiveChar().broadcastStatusUpdate();
    }

    public Creature getActiveChar() {
        return this._activeChar;
    }
}
