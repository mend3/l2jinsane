package net.sf.l2j.gameserver.model.actor;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.scripting.QuestState;

public abstract class Playable extends Creature {
    public Playable(int objectId, CreatureTemplate template) {
        super(objectId, template);
    }

    public void initCharStat() {
        setStat(new PlayableStat(this));
    }

    public PlayableStat getStat() {
        return (PlayableStat) super.getStat();
    }

    public void initCharStatus() {
        setStatus(new PlayableStatus(this));
    }

    public PlayableStatus getStatus() {
        return (PlayableStatus) super.getStatus();
    }

    public void onActionShift(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player) && player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public boolean doDie(Creature killer) {
        synchronized (this) {
            if (isDead())
                return false;
            setCurrentHp(0.0D);
            setIsDead(true);
        }
        EngineModsManager.onKill(killer, this, killer instanceof Summon);
        EngineModsManager.onDeath(this);
        setTarget(null);
        stopMove(null);
        getStatus().stopHpMpRegeneration();
        if (isPhoenixBlessed()) {
            if (getCharmOfLuck())
                stopCharmOfLuck(null);
            if (isNoblesseBlessed())
                stopNoblesseBlessing(null);
        } else if (isNoblesseBlessed()) {
            stopNoblesseBlessing(null);
            if (getCharmOfLuck())
                stopCharmOfLuck(null);
        } else if (getActingPlayer() != null) {
            if (TvTEventManager.getInstance().getActiveEvent() == null || !TvTEventManager.getInstance().getActiveEvent().isInEvent(getActingPlayer()))
                stopAllEffectsExceptThoseThatLastThroughDeath();
            if (CtfEventManager.getInstance().getActiveEvent() == null || !CtfEventManager.getInstance().getActiveEvent().isInEvent(getActingPlayer()))
                stopAllEffectsExceptThoseThatLastThroughDeath();
            if (DmEventManager.getInstance().getActiveEvent() == null || !DmEventManager.getInstance().getActiveEvent().isInEvent(getActingPlayer()))
                stopAllEffectsExceptThoseThatLastThroughDeath();
        }
        broadcastStatusUpdate();
        getAI().notifyEvent(AiEventType.DEAD);
        WorldRegion region = getRegion();
        if (region != null)
            region.onDeath(this);
        Player actingPlayer = getActingPlayer();
        for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
            qs.getQuest().notifyDeath((killer == null) ? this : killer, actingPlayer);
        if (killer != null) {
            Player player = killer.getActingPlayer();
            if (player != null)
                player.onKillUpdatePvPKarma(this);
        }
        return true;
    }

    public void doRevive() {
        if (!isDead() || isTeleporting())
            return;
        setIsDead(false);
        if (isPhoenixBlessed()) {
            stopPhoenixBlessing(null);
            getStatus().setCurrentHp(getMaxHp());
            getStatus().setCurrentMp(getMaxMp());
        } else {
            getStatus().setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
        }
        broadcastPacket(new Revive(this));
        WorldRegion region = getRegion();
        if (region != null)
            region.onRevive(this);
    }

    public boolean checkIfPvP(Playable target) {
        if (target == null || target == this)
            return false;
        Player player = getActingPlayer();
        if (player == null || player.getKarma() != 0)
            return false;
        Player targetPlayer = target.getActingPlayer();
        if (targetPlayer == null || targetPlayer == this)
            return false;
        return targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() != 0;
    }

    public boolean isAttackable() {
        return true;
    }

    public void sendPacket(SystemMessageId id) {
    }

    public final boolean isNoblesseBlessed() {
        return this._effects.isAffected(L2EffectFlag.NOBLESS_BLESSING);
    }

    public final void stopNoblesseBlessing(L2Effect effect) {
        if (effect == null) {
            stopEffects(L2EffectType.NOBLESSE_BLESSING);
        } else {
            removeEffect(effect);
        }
        updateAbnormalEffect();
    }

    public final boolean isPhoenixBlessed() {
        return this._effects.isAffected(L2EffectFlag.PHOENIX_BLESSING);
    }

    public final void stopPhoenixBlessing(L2Effect effect) {
        if (effect == null) {
            stopEffects(L2EffectType.PHOENIX_BLESSING);
        } else {
            removeEffect(effect);
        }
        updateAbnormalEffect();
    }

    public boolean isSilentMoving() {
        return this._effects.isAffected(L2EffectFlag.SILENT_MOVE);
    }

    public final boolean getProtectionBlessing() {
        return this._effects.isAffected(L2EffectFlag.PROTECTION_BLESSING);
    }

    public void stopProtectionBlessing(L2Effect effect) {
        if (effect == null) {
            stopEffects(L2EffectType.PROTECTION_BLESSING);
        } else {
            removeEffect(effect);
        }
        updateAbnormalEffect();
    }

    public final boolean getCharmOfLuck() {
        return this._effects.isAffected(L2EffectFlag.CHARM_OF_LUCK);
    }

    public final void stopCharmOfLuck(L2Effect effect) {
        if (effect == null) {
            stopEffects(L2EffectType.CHARM_OF_LUCK);
        } else {
            removeEffect(effect);
        }
        updateAbnormalEffect();
    }

    public void updateEffectIcons(boolean partyOnly) {
        this._effects.updateEffectIcons(partyOnly);
    }

    public void broadcastRelationsChanges() {
    }

    public boolean isInArena() {
        return (isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE));
    }

    public abstract void doPickupItem(WorldObject paramWorldObject);

    public abstract int getKarma();

    public abstract byte getPvpFlag();

    public abstract boolean useMagic(L2Skill paramL2Skill, boolean paramBoolean1, boolean paramBoolean2);
}
