/**/
package net.sf.l2j.gameserver.model.actor;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.*;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.*;
import net.sf.l2j.gameserver.events.bossevent.BossEvent;
import net.sf.l2j.gameserver.events.bossevent.BossEvent.EventState;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.instance.*;
import net.sf.l2j.gameserver.model.actor.stat.CreatureStat;
import net.sf.l2j.gameserver.model.actor.status.CreatureStatus;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.skills.effects.EffectChanceSkillTrigger;
import net.sf.l2j.gameserver.skills.funcs.*;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.MovementTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public abstract class Creature extends WorldObject {
    private final Calculator[] _calculators;
    private final byte[] _zones;
    private final Map<Integer, Long> _disabledSkills;
    protected boolean _isTeleporting = false;
    protected boolean _showSummonAnimation = false;
    protected boolean _isInvul = false;
    protected String _title;
    protected FusionSkill _fusionSkill;
    protected byte _zoneValidateCounter;
    protected CharEffectList _effects;
    protected MoveData _move;
    protected CreatureAI _ai;
    protected Future<?> _skillCast;
    protected Future<?> _skillCast2;
    TeamType _team;
    private volatile boolean _isCastingNow = false;
    private volatile boolean _isCastingSimultaneouslyNow = false;
    private L2Skill _lastSkillCast;
    private L2Skill _lastSimultaneousSkillCast;
    private boolean _isImmobilized = false;
    private boolean _isOverloaded = false;
    private boolean _isParalyzed = false;
    private boolean _isDead = false;
    private boolean _isRunning = false;
    private boolean _isMortal = true;
    private boolean _isNoRndWalk = false;
    private boolean _AIdisabled = false;
    private CreatureStat _stat;
    private CreatureStatus _status;
    private CreatureTemplate _template;
    private double _hpUpdateIncCheck = 0.0F;
    private double _hpUpdateDecCheck = 0.0F;
    private double _hpUpdateInterval = 0.0F;
    private boolean _champion = false;
    private ChanceSkillList _chanceSkills;
    private boolean _isBuffProtected;
    private int _AbnormalEffects;
    private boolean _allSkillsDisabled;
    private WorldObject _target;
    private long _attackEndTime;
    private long _disableBowAttackEndTime;
    private long _castInterruptTime;
    private boolean inArenaEvent;
    private boolean _ArenaAttack;
    private boolean _ArenaProtection;
    private boolean _Arena9x9;
    private boolean _Arena4x4;
    private boolean _Arena2x2;
    private boolean _isStopMov;
    private boolean _ArenaObserv;

    public Creature(int objectId, CreatureTemplate template) {
        super(objectId);
        this._zones = new byte[ZoneId.VALUES.length];
        this._zoneValidateCounter = 4;
        this._isBuffProtected = false;
        this._effects = new CharEffectList(this);
        this._disabledSkills = new ConcurrentHashMap<>();
        this._team = TeamType.NONE;
        this.inArenaEvent = false;
        this._isStopMov = false;
        this.initCharStat();
        this.initCharStatus();
        this._template = template;
        this._calculators = new Calculator[Stats.NUM_STATS];
        this.addFuncsToNewCharacter();
    }

    public static boolean isInsidePeaceZone(Creature attacker, WorldObject target) {
        if (target == null) {
            return false;
        } else if (!(target instanceof Npc) && !(attacker instanceof Npc)) {
            if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getAccessLevel().allowPeaceAttack()) {
                return false;
            } else if (Config.KARMA_PLAYER_CAN_BE_KILLED_IN_PZ && target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0) {
                return false;
            } else if (target instanceof Creature) {
                return target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE);
            } else {
                return MapRegionData.getTown(target.getX(), target.getY(), target.getZ()) != null || attacker.isInsideZone(ZoneId.PEACE);
            }
        } else {
            return false;
        }
    }

    public void addFuncsToNewCharacter() {
        this.addStatFunc(FuncPAtkMod.getInstance());
        this.addStatFunc(FuncMAtkMod.getInstance());
        this.addStatFunc(FuncPDefMod.getInstance());
        this.addStatFunc(FuncMDefMod.getInstance());
        this.addStatFunc(FuncMaxHpMul.getInstance());
        this.addStatFunc(FuncMaxMpMul.getInstance());
        this.addStatFunc(FuncAtkAccuracy.getInstance());
        this.addStatFunc(FuncAtkEvasion.getInstance());
        this.addStatFunc(FuncPAtkSpeed.getInstance());
        this.addStatFunc(FuncMAtkSpeed.getInstance());
        this.addStatFunc(FuncMoveSpeed.getInstance());
        this.addStatFunc(FuncAtkCritical.getInstance());
        this.addStatFunc(FuncMAtkCritical.getInstance());
    }

    protected void initCharStatusUpdateValues() {
        this._hpUpdateInterval = (double) this.getMaxHp() / (double) 352.0F;
        this._hpUpdateIncCheck = this.getMaxHp();
        this._hpUpdateDecCheck = (double) this.getMaxHp() - this._hpUpdateInterval;
    }

    public void onDecay() {
        this.decayMe();
    }

    public void onTeleported() {
        if (this.isTeleporting()) {
            this.setIsTeleporting(false);
            this.setRegion(World.getInstance().getRegion(this.getPosition()));
        }
    }

    public Inventory getInventory() {
        return null;
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        return true;
    }

    public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        return true;
    }

    public boolean isInsideZone(ZoneId zone) {
        return zone == ZoneId.PVP ? this._zones[ZoneId.PVP.getId()] > 0 && this._zones[ZoneId.PEACE.getId()] == 0 : this._zones[zone.getId()] > 0;
    }

    public void setInsideZone(ZoneId zone, boolean state) {
        if (state) {
            ++this._zones[zone.getId()];
        } else {
            --this._zones[zone.getId()];
            if (this._zones[zone.getId()] < 0) {
                this._zones[zone.getId()] = 0;
            }
        }

    }

    public boolean isGM() {
        return false;
    }

    public void broadcastPacket(L2GameServerPacket packet) {
        this.broadcastPacket(packet, true);
    }

    public void broadcastPacket(L2GameServerPacket packet, boolean selfToo) {
        for (Player player : this.getKnownType(Player.class)) {
            player.sendPacket(packet);
        }

    }

    public void broadcastPacketInRadius(L2GameServerPacket packet, int radius) {
        if (radius < 0) {
            radius = 600;
        }

        for (Player player : this.getKnownTypeInRadius(Player.class, radius)) {
            player.sendPacket(packet);
        }

    }

    protected boolean needHpUpdate(int barPixels) {
        double currentHp = this.getCurrentHp();
        if (!(currentHp <= (double) 1.0F) && this.getMaxHp() >= barPixels) {
            if (!(currentHp <= this._hpUpdateDecCheck) && !(currentHp >= this._hpUpdateIncCheck)) {
                return false;
            } else {
                if (currentHp == (double) this.getMaxHp()) {
                    this._hpUpdateIncCheck = currentHp + (double) 1.0F;
                    this._hpUpdateDecCheck = currentHp - this._hpUpdateInterval;
                } else {
                    double doubleMulti = currentHp / this._hpUpdateInterval;
                    int intMulti = (int) doubleMulti;
                    this._hpUpdateDecCheck = this._hpUpdateInterval * (double) (doubleMulti < (double) intMulti ? intMulti-- : intMulti);
                    this._hpUpdateIncCheck = this._hpUpdateDecCheck + this._hpUpdateInterval;
                }

                return true;
            }
        } else {
            return true;
        }
    }

    public void broadcastStatusUpdate() {
        if (!this.getStatus().getStatusListener().isEmpty()) {
            if (this.needHpUpdate(352)) {
                StatusUpdate su = new StatusUpdate(this);
                su.addAttribute(9, (int) this.getCurrentHp());

                for (Creature temp : this.getStatus().getStatusListener()) {
                    if (temp != null) {
                        temp.sendPacket(su);
                    }
                }

            }
        }
    }

    public void sendPacket(L2GameServerPacket mov) {
    }

    public void sendMessage(String text) {
    }

    public void instantTeleportTo(int x, int y, int z, int randomOffset) {
        this.stopMove(null);
        this.abortAttack();
        this.abortCast();
        this.setTarget(null);
        this.getAI().setIntention(IntentionType.ACTIVE);
        if (randomOffset > 0) {
            x += Rnd.get(-randomOffset, randomOffset);
            y += Rnd.get(-randomOffset, randomOffset);
        }

        z += 5;
        this.broadcastPacket(new TeleportToLocation(this, x, y, z, true));
        this.getPosition().set(x, y, z);
        this.refreshKnownlist();
    }

    public void instantTeleportTo(Location loc, int randomOffset) {
        this.instantTeleportTo(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
    }

    public void teleportTo(int x, int y, int z, int randomOffset) {
        this.stopMove(null);
        this.abortAttack();
        this.abortCast();
        this.setIsTeleporting(true);
        this.setTarget(null);
        this.getAI().setIntention(IntentionType.ACTIVE);
        if (randomOffset > 0) {
            x += Rnd.get(-randomOffset, randomOffset);
            y += Rnd.get(-randomOffset, randomOffset);
        }

        z += 5;
        this.broadcastPacket(new TeleportToLocation(this, x, y, z, false));
        this.setRegion(null);
        this.getPosition().set(x, y, z);
        if (!(this instanceof Player) || ((Player) this).getClient() != null && ((Player) this).getClient().isDetached()) {
            this.onTeleported();
        }

    }

    public void teleportTo(Location loc, int randomOffset) {
        this.teleportTo(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
    }

    public void teleportTo(MapRegionData.TeleportType type) {
        this.teleportTo(MapRegionData.getInstance().getLocationToTeleport(this, type), 20);
    }

    public void doAttack(Creature target) {
        if (target != null && !this.isAttackingDisabled()) {
            if (!this.isAlikeDead()) {
                if (this instanceof Npc && target.isAlikeDead() || !this.getKnownType(Creature.class).contains(target)) {
                    this.getAI().setIntention(IntentionType.ACTIVE);
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                if (this instanceof Player && target.isDead()) {
                    this.getAI().setIntention(IntentionType.ACTIVE);
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }
            }

            Player player = this.getActingPlayer();
            if (player != null && target instanceof Player && !EventListener.canAttack(player, (Player) target)) {
                this.getAI().setIntention(IntentionType.ACTIVE);
                this.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (player != null && player.isInObserverMode()) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
                this.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (isInsidePeaceZone(this, target)) {
                this.getAI().setIntention(IntentionType.ACTIVE);
                this.sendPacket(ActionFailed.STATIC_PACKET);
            } else {
                this.stopEffectsOnAction();
                Weapon weaponItem = this.getActiveWeaponItem();
                WeaponType weaponItemType = this.getAttackType();
                if (weaponItemType == WeaponType.FISHINGROD) {
                    this.getAI().setIntention(IntentionType.IDLE);
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                } else if (!GeoEngine.getInstance().canSeeTarget(this, target)) {
                    this.getAI().setIntention(IntentionType.ACTIVE);
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                } else {
                    long time = System.currentTimeMillis();
                    if (weaponItemType == WeaponType.BOW) {
                        if (this instanceof Player) {
                            if (!this.checkAndEquipArrows()) {
                                this.getAI().setIntention(IntentionType.IDLE);
                                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                return;
                            }

                            if (this._disableBowAttackEndTime > time) {
                                ThreadPool.schedule(() -> this.getAI().notifyEvent(AiEventType.READY_TO_ACT), 100L);
                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                return;
                            }

                            int mpConsume = weaponItem.getMpConsume();
                            if (mpConsume > 0) {
                                if (this.getCurrentMp() < (double) mpConsume) {
                                    this.getAI().setIntention(IntentionType.IDLE);
                                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                    return;
                                }

                                this.getStatus().reduceMp(mpConsume);
                            }
                        } else if (this instanceof Npc && this._disableBowAttackEndTime > time) {
                            return;
                        }
                    }

                    this.rechargeShots(true, false);
                    int timeAtk = this.calculateTimeBetweenAttacks(target, weaponItemType);
                    this._attackEndTime = time + (long) timeAtk - 100L;
                    this._disableBowAttackEndTime = time + 50L;
                    Attack attack = new Attack(this, this.isChargedShot(ShotType.SOULSHOT), weaponItem != null ? weaponItem.getCrystalType().getId() : 0);
                    this.getPosition().setHeading(MathUtil.calculateHeadingFrom(this, target));
                    boolean hitted;
                    switch (weaponItemType) {
                        case BOW:
                            hitted = this.doAttackHitByBow(attack, target, timeAtk, weaponItem);
                            break;
                        case POLE:
                            hitted = this.doAttackHitByPole(attack, target, timeAtk / 2);
                            break;
                        case DUAL:
                        case DUALFIST:
                            hitted = this.doAttackHitByDual(attack, target, timeAtk / 2);
                            break;
                        case FIST:
                            if (this.getSecondaryWeaponItem() != null && this.getSecondaryWeaponItem() instanceof Armor) {
                                hitted = this.doAttackHitSimple(attack, target, timeAtk / 2);
                            } else {
                                hitted = this.doAttackHitByDual(attack, target, timeAtk / 2);
                            }
                            break;
                        default:
                            hitted = this.doAttackHitSimple(attack, target, timeAtk / 2);
                    }

                    this.getAI().startAttackStance();
                    if (player != null && player.getSummon() != target) {
                        player.updatePvPStatus(target);
                    }

                    if (!hitted) {
                        this.abortAttack();
                    } else {
                        if (this instanceof Attackable) {
                            Player victim = target.getActingPlayer();
                            if (victim != null) {
                                Npc mob = (Npc) this;
                                List<Quest> scripts = mob.getTemplate().getEventQuests(ScriptEventType.ON_ATTACK_ACT);
                                if (scripts != null) {
                                    for (Quest quest : scripts) {
                                        quest.notifyAttackAct(mob, victim);
                                    }
                                }
                            }
                        }

                        this.setChargedShot(ShotType.SOULSHOT, false);
                        if (player != null) {
                            if (player.isCursedWeaponEquipped()) {
                                if (!target.isInvul()) {
                                    target.setCurrentCp(0.0F);
                                }
                            } else if (player.isHero() && target instanceof Player && ((Player) target).isCursedWeaponEquipped()) {
                                target.setCurrentCp(0.0F);
                            }
                        }
                    }

                    if (attack.hasHits()) {
                        this.broadcastPacket(attack);
                    }

                    if (player != null && !target.isAutoAttackable(player)) {
                        this.getAI().setIntention(IntentionType.IDLE);
                    } else {
                        ThreadPool.schedule(() -> this.getAI().notifyEvent(AiEventType.READY_TO_ACT), timeAtk);
                    }
                }
            }
        } else {
            this.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    private boolean doAttackHitByBow(Attack attack, Creature target, int sAtk, Weapon weapon) {
        int damage1 = 0;
        byte shld1 = 0;
        boolean crit1 = false;
        boolean miss1 = Formulas.calcHitMiss(this, target);
        if (!Config.INFINITY_ARROWS) {
            this.reduceArrowCount();
        }

        this._move = null;
        if (!miss1) {
            shld1 = Formulas.calcShldUse(this, target, null);
            crit1 = Formulas.calcCrit(this.getStat().getCriticalHit(target, null));
            damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
        }

        int reuse = weapon.getReuseDelay();
        if (reuse != 0) {
            reuse = reuse * 345 / this.getStat().getPAtkSpd();
        }

        if (this instanceof Player) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
            this.sendPacket(new SetupGauge(GaugeColor.RED, sAtk + reuse));
        }

        ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
        this._disableBowAttackEndTime += sAtk + reuse;
        attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
        return !miss1;
    }

    private boolean doAttackHitByDual(Attack attack, Creature target, int sAtk) {
        int damage1 = 0;
        int damage2 = 0;
        byte shld1 = 0;
        byte shld2 = 0;
        boolean crit1 = false;
        boolean crit2 = false;
        boolean miss1 = Formulas.calcHitMiss(this, target);
        boolean miss2 = Formulas.calcHitMiss(this, target);
        if (!miss1) {
            shld1 = Formulas.calcShldUse(this, target, null);
            crit1 = Formulas.calcCrit(this.getStat().getCriticalHit(target, null));
            damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
            damage1 /= 2;
        }

        if (!miss2) {
            shld2 = Formulas.calcShldUse(this, target, null);
            crit2 = Formulas.calcCrit(this.getStat().getCriticalHit(target, null));
            damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, attack.soulshot);
            damage2 /= 2;
        }

        ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
        ThreadPool.schedule(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
        attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));
        return !miss1 || !miss2;
    }

    private boolean doAttackHitByPole(Attack attack, Creature target, int sAtk) {
        int maxRadius = this.getPhysicalAttackRange();
        int maxAngleDiff = (int) this.getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120.0F, null, null);
        int attackRandomCountMax = (int) this.getStat().calcStat(Stats.ATTACK_COUNT_MAX, 0.0F, null, null) - 1;
        int attackcount = 0;
        boolean hitted = this.doAttackHitSimple(attack, target, 100.0F, sAtk);
        double attackpercent = 85.0F;

        for (Creature obj : this.getKnownType(Creature.class)) {
            if (obj != target && !obj.isAlikeDead()) {
                if (this instanceof Player) {
                    if (obj instanceof Pet && ((Pet) obj).getOwner() == this) {
                        continue;
                    }
                } else if (this instanceof Attackable && (obj instanceof Player && this.getTarget() instanceof Attackable || obj instanceof Attackable && !this.isConfused())) {
                    continue;
                }

                if (MathUtil.checkIfInRange(maxRadius, this, obj, false) && Math.abs(obj.getZ() - this.getZ()) <= 650 && this.isFacing(obj, maxAngleDiff) && (obj == this.getAI().getTarget() || obj.isAutoAttackable(this))) {
                    ++attackcount;
                    if (attackcount > attackRandomCountMax) {
                        break;
                    }

                    hitted |= this.doAttackHitSimple(attack, obj, attackpercent, sAtk);
                    attackpercent /= 1.15;
                }
            }
        }

        return hitted;
    }

    private boolean doAttackHitSimple(Attack attack, Creature target, int sAtk) {
        return this.doAttackHitSimple(attack, target, 100.0F, sAtk);
    }

    private boolean doAttackHitSimple(Attack attack, Creature target, double attackpercent, int sAtk) {
        int damage1 = 0;
        byte shld1 = 0;
        boolean crit1 = false;
        boolean miss1 = Formulas.calcHitMiss(this, target);
        if (!miss1) {
            shld1 = Formulas.calcShldUse(this, target, null);
            crit1 = Formulas.calcCrit(this.getStat().getCriticalHit(target, null));
            damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
            if (attackpercent != (double) 100.0F) {
                damage1 = (int) ((double) damage1 * attackpercent / (double) 100.0F);
            }
        }

        ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
        attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
        return !miss1;
    }

    public void doCast(L2Skill skill) {
        this.beginCast(skill, false);
    }

    public void doSimultaneousCast(L2Skill skill) {
        this.beginCast(skill, true);
    }

    private void beginCast(L2Skill skill, boolean simultaneously) {
        if (!this.checkDoCastConditions(skill)) {
            if (simultaneously) {
                this.setIsCastingSimultaneouslyNow(false);
            } else {
                this.setIsCastingNow(false);
            }

            if (this instanceof Player) {
                this.getAI().setIntention(IntentionType.ACTIVE);
            }

        } else {
            if (skill.isSimultaneousCast() && !simultaneously) {
                simultaneously = true;
            }

            this.stopEffectsOnAction();
            this.rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
            Creature target = null;
            WorldObject[] targets = skill.getTargetList(this);
            boolean doit = false;
            Object var6;
            switch (skill.getTargetType()) {
                case TARGET_AREA_SUMMON:
                    var6 = this.getSummon();
                    break;
                case TARGET_AURA:
                case TARGET_FRONT_AURA:
                case TARGET_BEHIND_AURA:
                case TARGET_AURA_UNDEAD:
                case TARGET_GROUND:
                    var6 = this;
                    break;
                case TARGET_SELF:
                case TARGET_CORPSE_ALLY:
                case TARGET_PET:
                case TARGET_SUMMON:
                case TARGET_OWNER_PET:
                case TARGET_PARTY:
                case TARGET_CLAN:
                case TARGET_ALLY:
                    doit = true;
                default:
                    if (targets.length == 0) {
                        if (simultaneously) {
                            this.setIsCastingSimultaneouslyNow(false);
                        } else {
                            this.setIsCastingNow(false);
                        }

                        if (this instanceof Player) {
                            this.sendPacket(ActionFailed.STATIC_PACKET);
                            this.getAI().setIntention(IntentionType.ACTIVE);
                        }

                        return;
                    }

                    switch (skill.getSkillType()) {
                        case BUFF:
                        case HEAL:
                        case COMBATPOINTHEAL:
                        case MANAHEAL:
                        case SEED:
                        case REFLECT:
                            doit = true;
                        default:
                            var6 = doit ? (Creature) targets[0] : (Creature) this.getTarget();
                    }
            }

            this.beginCast(skill, simultaneously, (Creature) var6, targets);
        }
    }

    private void beginCast(L2Skill skill, boolean simultaneously, Creature target, WorldObject[] targets) {
        if (target == null) {
            if (simultaneously) {
                this.setIsCastingSimultaneouslyNow(false);
            } else {
                this.setIsCastingNow(false);
            }

            if (this instanceof Player) {
                this.sendPacket(ActionFailed.STATIC_PACKET);
                this.getAI().setIntention(IntentionType.ACTIVE);
            }

        } else {
            int hitTime = skill.getHitTime();
            int coolTime = skill.getCoolTime();
            boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;
            if (!effectWhileCasting) {
                hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
                if (coolTime > 0) {
                    coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
                }
            }

            if (skill.isMagic() && !effectWhileCasting && (this.isChargedShot(ShotType.SPIRITSHOT) || this.isChargedShot(ShotType.BLESSED_SPIRITSHOT))) {
                hitTime = (int) (0.7 * (double) hitTime);
                coolTime = (int) (0.7 * (double) coolTime);
            }

            if (skill.isStaticHitTime()) {
                hitTime = skill.getHitTime();
                coolTime = skill.getCoolTime();
            } else if (skill.getHitTime() >= 500 && hitTime < 500) {
                hitTime = 500;
            }

            if (simultaneously) {
                if (this.isCastingSimultaneouslyNow()) {
                    ThreadPool.schedule(() -> this.doSimultaneousCast(skill), 100L);
                    return;
                }

                this.setIsCastingSimultaneouslyNow(true);
                this.setLastSimultaneousSkillCast(skill);
            } else {
                this.setIsCastingNow(true);
                this._castInterruptTime = System.currentTimeMillis() + (long) hitTime - 200L;
                this.setLastSkillCast(skill);
            }

            int reuseDelay = skill.getReuseDelay();
            if (!skill.isStaticReuse()) {
                reuseDelay = (int) ((double) reuseDelay * this.calcStat(skill.isMagic() ? Stats.MAGIC_REUSE_RATE : Stats.P_REUSE, 1.0F, null, null));
                reuseDelay = (int) ((double) reuseDelay * ((double) 333.0F / (double) (skill.isMagic() ? this.getMAtkSpd() : this.getPAtkSpd())));
            }

            boolean skillMastery = Formulas.calcSkillMastery(this, skill);
            if (reuseDelay > 30000 && !skillMastery) {
                this.addTimeStamp(skill, reuseDelay);
            }

            int initmpcons = this.getStat().getMpInitialConsume(skill);
            if (initmpcons > 0) {
                this.getStatus().reduceMp(initmpcons);
                StatusUpdate su = new StatusUpdate(this);
                su.addAttribute(11, (int) this.getCurrentMp());
                this.sendPacket(su);
            }

            if (reuseDelay > 10) {
                if (skillMastery) {
                    reuseDelay = 100;
                    if (this.getActingPlayer() != null) {
                        this.getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
                    }
                }

                this.disableSkill(skill, reuseDelay);
            }

            if (target != this) {
                this.getPosition().setHeading(MathUtil.calculateHeadingFrom(this, target));
            }

            if (effectWhileCasting) {
                if (skill.getItemConsumeId() > 0 && !this.destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true)) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                    if (simultaneously) {
                        this.setIsCastingSimultaneouslyNow(false);
                    } else {
                        this.setIsCastingNow(false);
                    }

                    if (this instanceof Player) {
                        this.getAI().setIntention(IntentionType.ACTIVE);
                    }

                    return;
                }

                if (skill.getSkillType() == L2SkillType.FUSION) {
                    this.startFusionSkill(target, skill);
                } else {
                    this.callSkill(skill, targets);
                }
            }

            int displayId = skill.getId();
            int level = skill.getLevel();
            if (level < 1) {
                level = 1;
            }

            if (!skill.isToggle()) {
                if (!skill.isPotion()) {
                    this.broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, false));
                    this.broadcastPacket(new MagicSkillLaunched(this, displayId, level, targets != null && targets.length != 0 ? targets : new WorldObject[]{target}));
                } else {
                    this.broadcastPacket(new MagicSkillUse(this, target, displayId, level, 0, 0));
                }
            }

            if (this instanceof Playable) {
                if (this instanceof Player && skill.getId() != 1312) {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
                    sm.addSkillName(skill);
                    this.sendPacket(sm);
                }

                if (!effectWhileCasting && skill.getItemConsumeId() > 0 && !this.destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true)) {
                    this.getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                    this.abortCast();
                    return;
                }

                if (this instanceof Player && skill.getFlyType() != null) {
                    ThreadPool.schedule(new FlyToLocationTask(this, target, skill), 50L);
                }
            }

            MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously);
            if (hitTime > 410) {
                if (this instanceof Player && !effectWhileCasting) {
                    this.sendPacket(new SetupGauge(GaugeColor.BLUE, hitTime));
                }

                if (effectWhileCasting) {
                    mut.phase = 2;
                }

                if (simultaneously) {
                    Future<?> future = this._skillCast2;
                    if (future != null) {
                        future.cancel(true);
                        this._skillCast2 = null;
                    }

                    this._skillCast2 = ThreadPool.schedule(mut, hitTime - 400);
                } else {
                    Future<?> future = this._skillCast;
                    if (future != null) {
                        future.cancel(true);
                        this._skillCast = null;
                    }

                    this._skillCast = ThreadPool.schedule(mut, hitTime - 400);
                }
            } else {
                mut.hitTime = 0;
                this.onMagicLaunchedTimer(mut);
            }

        }
    }

    protected boolean checkDoCastConditions(L2Skill skill) {
        if (skill != null && !this.isSkillDisabled(skill)) {
            if (this.getCurrentMp() < (double) (this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill))) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
                this.sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            } else if (this.getCurrentHp() <= (double) skill.getHpConsume()) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
                this.sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            } else if (skill.isPotion() || (!skill.isMagic() || !this.isMuted()) && (skill.isMagic() || !this.isPhysicalMuted())) {
                if (!skill.getWeaponDependancy(this)) {
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                } else {
                    if (skill.getItemConsumeId() > 0 && this.getInventory() != null) {
                        ItemInstance requiredItems = this.getInventory().getItemByItemId(skill.getItemConsumeId());
                        if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume()) {
                            if (skill.getSkillType() == L2SkillType.SUMMON) {
                                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
                                sm.addItemName(skill.getItemConsumeId());
                                sm.addNumber(skill.getItemConsume());
                                this.sendPacket(sm);
                                return false;
                            }

                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NUMBER_INCORRECT));
                            return false;
                        }
                    }

                    return true;
                }
            } else {
                this.sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        } else {
            this.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }
    }

    public void addTimeStamp(L2Skill skill, long reuse) {
    }

    public void startFusionSkill(Creature target, L2Skill skill) {
        if (skill.getSkillType() == L2SkillType.FUSION) {
            if (this._fusionSkill == null) {
                this._fusionSkill = new FusionSkill(this, target, skill);
            }

        }
    }

    public boolean doDie(Creature killer) {
        synchronized (this) {
            if (this.isDead()) {
                return false;
            }

            this.setCurrentHp(0.0F);
            this.setIsDead(true);
        }

        this.setTarget(null);
        this.stopMove(null);
        this.getStatus().stopHpMpRegeneration();
        this.stopAllEffectsExceptThoseThatLastThroughDeath();
        this.calculateRewards(killer);
        if (BossEvent.getInstance().getState() == EventState.FIGHTING && BossEvent.getInstance().bossSpawn.getNpc() == this) {
            BossEvent.getInstance().finishEvent();
            BossEvent.getInstance().bossKilled = true;
            if (killer instanceof Player) {
                Player lastAttacker = killer.getActingPlayer();
                BossEvent.getInstance().setLastAttacker(lastAttacker);
                LOGGER.info("Boss Event Finished. Last Attacker : " + lastAttacker.getName());
                LOGGER.info("Players rewarded: " + BossEvent.getInstance().eventPlayers.size());
                if (Config.BOSS_EVENT_REWARD_LAST_ATTACKER && lastAttacker.getBossEventDamage() > Config.BOSS_EVENT_MIN_DAMAGE_TO_OBTAIN_REWARD) {
                    BossEvent.getInstance().reward(lastAttacker, Config.BOSS_EVENT_LAST_ATTACKER_REWARDS);
                    lastAttacker.sendPacket(new CreatureSay(0, 18, "[Boss Event]", "Congratulations, you was the last attacker! So you will receive wonderful rewards."));
                }
            }
        }

        this.broadcastStatusUpdate();
        if (this.hasAI()) {
            this.getAI().notifyEvent(AiEventType.DEAD, null);
        }

        WorldRegion region = this.getRegion();
        if (region != null) {
            region.onDeath(this);
        }

        return true;
    }

    public void deleteMe() {
        if (this.hasAI()) {
            this.getAI().stopAITask();
        }

    }

    public void detachAI() {
        this._ai = null;
    }

    protected void calculateRewards(Creature killer) {
    }

    public void doRevive() {
        if (this.isDead() && !this.isTeleporting()) {
            this.setIsDead(false);
            this._status.setCurrentHp((double) this.getMaxHp() * Config.RESPAWN_RESTORE_HP);
            this.broadcastPacket(new Revive(this));
            WorldRegion region = this.getRegion();
            if (region != null) {
                region.onRevive(this);
            }

        }
    }

    public void doRevive(double revivePower) {
        this.doRevive();
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null) {
            synchronized (this) {
                if (this._ai == null) {
                    this._ai = new CreatureAI(this);
                }

                return this._ai;
            }
        } else {
            return ai;
        }
    }

    public void setAI(CreatureAI newAI) {
        CreatureAI oldAI = this.getAI();
        if (oldAI != null && oldAI != newAI && oldAI instanceof AttackableAI) {
            oldAI.stopAITask();
        }

        this._ai = newAI;
    }

    public boolean hasAI() {
        return this._ai != null;
    }

    public boolean isRaidBoss() {
        return false;
    }

    public boolean isRaidRelated() {
        return false;
    }

    public boolean isMinion() {
        return false;
    }

    public final L2Skill getLastSimultaneousSkillCast() {
        return this._lastSimultaneousSkillCast;
    }

    public void setLastSimultaneousSkillCast(L2Skill skill) {
        this._lastSimultaneousSkillCast = skill;
    }

    public final L2Skill getLastSkillCast() {
        return this._lastSkillCast;
    }

    public void setLastSkillCast(L2Skill skill) {
        this._lastSkillCast = skill;
    }

    public final boolean isNoRndWalk() {
        return this._isNoRndWalk;
    }

    public final void setIsNoRndWalk(boolean value) {
        this._isNoRndWalk = value;
    }

    public final boolean isAfraid() {
        return this.isAffected(L2EffectFlag.FEAR);
    }

    public final boolean isConfused() {
        return this.isAffected(L2EffectFlag.CONFUSED);
    }

    public final boolean isMuted() {
        return this.isAffected(L2EffectFlag.MUTED);
    }

    public final boolean isPhysicalMuted() {
        return this.isAffected(L2EffectFlag.PHYSICAL_MUTED);
    }

    public final boolean isRooted() {
        return this.isAffected(L2EffectFlag.ROOTED);
    }

    public final boolean isSleeping() {
        return this.isAffected(L2EffectFlag.SLEEP);
    }

    public final boolean isStunned() {
        return this.isAffected(L2EffectFlag.STUNNED);
    }

    public final boolean isBetrayed() {
        return this.isAffected(L2EffectFlag.BETRAYED);
    }

    public final boolean isImmobileUntilAttacked() {
        return this.isAffected(L2EffectFlag.MEDITATING);
    }

    public final boolean isAllSkillsDisabled() {
        return this._allSkillsDisabled || this.isStunned() || this.isImmobileUntilAttacked() || this.isSleeping() || this.isParalyzed();
    }

    public boolean isAttackingDisabled() {
        return this.isFlying() || this.isStunned() || this.isImmobileUntilAttacked() || this.isSleeping() || this._attackEndTime > System.currentTimeMillis() || this.isParalyzed() || this.isAlikeDead() || this.isCoreAIDisabled();
    }

    public final Calculator[] getCalculators() {
        return this._calculators;
    }

    public boolean isImmobilized() {
        return this._isImmobilized;
    }

    public void setIsImmobilized(boolean value) {
        this._isImmobilized = value;
    }

    public boolean isAlikeDead() {
        return this._isDead;
    }

    public final boolean isDead() {
        return this._isDead;
    }

    public final void setIsDead(boolean value) {
        this._isDead = value;
    }

    public boolean isMovementDisabled() {
        return this.isStunned() || this.isImmobileUntilAttacked() || this.isRooted() || this.isSleeping() || this.isOverloaded() || this.isParalyzed() || this.isImmobilized() || this.isAlikeDead() || this.isTeleporting();
    }

    public boolean isOutOfControl() {
        return this.isConfused() || this.isAfraid() || this.isParalyzed() || this.isStunned() || this.isSleeping();
    }

    public boolean cantAttack() {
        return this.isStunned() || this.isImmobileUntilAttacked() || this.isAfraid() || this.isSleeping() || this.isParalyzed() || this.isAlikeDead() || this.isTeleporting();
    }

    public final boolean isOverloaded() {
        return this._isOverloaded;
    }

    public final void setIsOverloaded(boolean value) {
        this._isOverloaded = value;
    }

    public final boolean isParalyzed() {
        return this._isParalyzed || this.isAffected(L2EffectFlag.PARALYZED);
    }

    public final void setIsParalyzed(boolean value) {
        this._isParalyzed = value;
    }

    public Summon getSummon() {
        return null;
    }

    public boolean isSeated() {
        return false;
    }

    public boolean isRiding() {
        return false;
    }

    public boolean isFlying() {
        return false;
    }

    public final boolean isRunning() {
        return this._isRunning;
    }

    public final void setIsRunning(boolean value) {
        this._isRunning = value;
        if (this.getMoveSpeed() != 0) {
            this.broadcastPacket(new ChangeMoveType(this));
        }

        if (this instanceof Player) {
            ((Player) this).broadcastUserInfo();
        } else if (this instanceof Summon) {
            this.broadcastStatusUpdate();
        } else if (this instanceof Npc) {
            for (Player player : this.getKnownType(Player.class)) {
                if (this.getMoveSpeed() == 0) {
                    player.sendPacket(new ServerObjectInfo((Npc) this, player));
                } else {
                    player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) this, player));
                }
            }
        }

    }

    public final void setRunning() {
        if (!this.isRunning()) {
            this.setIsRunning(true);
        }

    }

    public final boolean isTeleporting() {
        return this._isTeleporting;
    }

    public final void setIsTeleporting(boolean value) {
        this._isTeleporting = value;
    }

    public void setIsInvul(boolean b) {
        this._isInvul = b;
    }

    public boolean isInvul() {
        return this._isInvul || this._isTeleporting;
    }

    public void setIsMortal(boolean b) {
        this._isMortal = b;
    }

    public boolean isMortal() {
        return this._isMortal;
    }

    public boolean isUndead() {
        return false;
    }

    public void initCharStat() {
        this._stat = new CreatureStat(this);
    }

    public CreatureStat getStat() {
        return this._stat;
    }

    public final void setStat(CreatureStat value) {
        this._stat = value;
    }

    public void initCharStatus() {
        this._status = new CreatureStatus(this);
    }

    public CreatureStatus getStatus() {
        return this._status;
    }

    public final void setStatus(CreatureStatus value) {
        this._status = value;
    }

    public CreatureTemplate getTemplate() {
        return this._template;
    }

    protected final void setTemplate(CreatureTemplate template) {
        this._template = template;
    }

    public final String getTitle() {
        return this._title;
    }

    public void setTitle(String value) {
        if (value == null) {
            this._title = "";
        } else if (value.length() > 16) {
            this._title = value.substring(0, 15);
        } else {
            this._title = value;
        }

    }

    public final void setWalking() {
        if (this.isRunning()) {
            this.setIsRunning(false);
        }

    }

    public void addEffect(L2Effect newEffect) {
        this._effects.queueEffect(newEffect, false);
    }

    public final void removeEffect(L2Effect effect) {
        this._effects.queueEffect(effect, true);
    }

    public final void startAbnormalEffect(AbnormalEffect mask) {
        this._AbnormalEffects |= mask.getMask();
        this.updateAbnormalEffect();
    }

    public final void startAbnormalEffect(int mask) {
        this._AbnormalEffects |= mask;
        this.updateAbnormalEffect();
    }

    public final void stopAbnormalEffect(AbnormalEffect mask) {
        this._AbnormalEffects &= ~mask.getMask();
        this.updateAbnormalEffect();
    }

    public final void stopAbnormalEffect(int mask) {
        this._AbnormalEffects &= ~mask;
        this.updateAbnormalEffect();
    }

    public void stopAllEffects() {
        this._effects.stopAllEffects();
    }

    public void stopAllEffectsExceptThoseThatLastThroughDeath() {
        this._effects.stopAllEffectsExceptThoseThatLastThroughDeath();
    }

    public final void startConfused() {
        this.getAI().notifyEvent(AiEventType.CONFUSED);
        this.updateAbnormalEffect();
    }

    public final void stopConfused(L2Effect effect) {
        if (effect == null) {
            this.stopEffects(L2EffectType.CONFUSION);
        } else {
            this.removeEffect(effect);
        }

        if (!(this instanceof Player)) {
            this.getAI().notifyEvent(AiEventType.THINK);
        }

        this.updateAbnormalEffect();
    }

    public final void startFakeDeath() {
        if (this instanceof Player) {
            ((Player) this).setIsFakeDeath(true);
            this.abortAttack();
            this.abortCast();
            this.stopMove(null);
            this.getAI().notifyEvent(AiEventType.FAKE_DEATH);
            this.broadcastPacket(new ChangeWaitType(this, 2));
        }
    }

    public final void stopFakeDeath(boolean removeEffects) {
        if (this instanceof Player player) {
            if (removeEffects) {
                this.stopEffects(L2EffectType.FAKE_DEATH);
            }

            player.setIsFakeDeath(false);
            player.setRecentFakeDeath();
            this.broadcastPacket(new ChangeWaitType(this, 3));
            this.broadcastPacket(new Revive(this));
            ThreadPool.schedule(() -> this.setIsParalyzed(false), (int) (2000.0F / this.getStat().getMovementSpeedMultiplier()));
            this.setIsParalyzed(true);
        }
    }

    public final void startFear() {
        this.abortAttack();
        this.abortCast();
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.AFRAID);
        this.updateAbnormalEffect();
    }

    public final void stopFear(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.FEAR);
        }

        this.updateAbnormalEffect();
    }

    public final void startImmobileUntilAttacked() {
        this.abortAttack();
        this.abortCast();
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.SLEEPING);
        this.updateAbnormalEffect();
    }

    public final void stopImmobileUntilAttacked(L2Effect effect) {
        if (effect == null) {
            this.stopEffects(L2EffectType.IMMOBILEUNTILATTACKED);
        } else {
            this.removeEffect(effect);
            this.stopSkillEffects(effect.getSkill().getId());
        }

        this.getAI().notifyEvent(AiEventType.THINK, null);
        this.updateAbnormalEffect();
    }

    public final void startMuted() {
        this.abortCast();
        this.getAI().notifyEvent(AiEventType.MUTED);
        this.updateAbnormalEffect();
    }

    public final void stopMuted(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.MUTE);
        }

        this.updateAbnormalEffect();
    }

    public final void startParalyze() {
        this.abortAttack();
        this.abortCast();
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.PARALYZED);
    }

    public final void stopParalyze() {
        if (!(this instanceof Player)) {
            this.getAI().notifyEvent(AiEventType.THINK);
        }

    }

    public final void startPhysicalMuted() {
        this.getAI().notifyEvent(AiEventType.MUTED);
        this.updateAbnormalEffect();
    }

    public final void stopPhysicalMuted(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.PHYSICAL_MUTE);
        }

        this.updateAbnormalEffect();
    }

    public final void startRooted() {
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.ROOTED);
        this.updateAbnormalEffect();
    }

    public final void stopRooting(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.ROOT);
        }

        if (!(this instanceof Player)) {
            this.getAI().notifyEvent(AiEventType.THINK);
        }

        this.updateAbnormalEffect();
    }

    public final void startSleeping() {
        this.abortAttack();
        this.abortCast();
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.SLEEPING);
        this.updateAbnormalEffect();
    }

    public final void stopSleeping(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.SLEEP);
        }

        if (!(this instanceof Player)) {
            this.getAI().notifyEvent(AiEventType.THINK);
        }

        this.updateAbnormalEffect();
    }

    public final void startStunning() {
        this.abortAttack();
        this.abortCast();
        this.stopMove(null);
        this.getAI().notifyEvent(AiEventType.STUNNED);
        if (!(this instanceof Summon)) {
            this.getAI().setIntention(IntentionType.IDLE);
        }

        this.updateAbnormalEffect();
    }

    public final void stopStunning(boolean removeEffects) {
        if (removeEffects) {
            this.stopEffects(L2EffectType.STUN);
        }

        if (!(this instanceof Player)) {
            this.getAI().notifyEvent(AiEventType.THINK);
        }

        this.updateAbnormalEffect();
    }

    public final void stopSkillEffects(int skillId) {
        this._effects.stopSkillEffects(skillId);
    }

    public final void stopSkillEffects(L2SkillType skillType, int negateLvl) {
        this._effects.stopSkillEffects(skillType, negateLvl);
    }

    public final void stopSkillEffects(L2SkillType skillType) {
        this._effects.stopSkillEffects(skillType, -1);
    }

    public final void stopEffects(L2EffectType type) {
        this._effects.stopEffects(type);
    }

    public final void stopEffectsOnAction() {
        this._effects.stopEffectsOnAction();
    }

    public final void stopEffectsOnDamage(boolean awake) {
        this._effects.stopEffectsOnDamage(awake);
    }

    public abstract void updateAbnormalEffect();

    public final void updateEffectIcons() {
        this.updateEffectIcons(false);
    }

    public void updateEffectIcons(boolean partyOnly) {
    }

    public int getAbnormalEffect() {
        int ae = this._AbnormalEffects;
        if (this.isStunned()) {
            ae |= AbnormalEffect.STUN.getMask();
        }

        if (this.isRooted()) {
            ae |= AbnormalEffect.ROOT.getMask();
        }

        if (this.isSleeping()) {
            ae |= AbnormalEffect.SLEEP.getMask();
        }

        if (this.isConfused()) {
            ae |= AbnormalEffect.FEAR.getMask();
        }

        if (this.isAfraid()) {
            ae |= AbnormalEffect.FEAR.getMask();
        }

        if (this.isMuted()) {
            ae |= AbnormalEffect.MUTED.getMask();
        }

        if (this.isPhysicalMuted()) {
            ae |= AbnormalEffect.MUTED.getMask();
        }

        if (this.isImmobileUntilAttacked()) {
            ae |= AbnormalEffect.FLOATING_ROOT.getMask();
        }

        return ae;
    }

    public final L2Effect[] getAllEffects() {
        return this._effects.getAllEffects();
    }

    public final L2Effect getFirstEffect(int skillId) {
        return this._effects.getFirstEffect(skillId);
    }

    public final L2Effect getFirstEffect(L2Skill skill) {
        return this._effects.getFirstEffect(skill);
    }

    public final L2Effect getFirstEffect(L2EffectType tp) {
        return this._effects.getFirstEffect(tp);
    }

    public final void addStatFunc(Func f) {
        if (f != null) {
            int stat = f.stat.ordinal();
            synchronized (this._calculators) {
                if (this._calculators[stat] == null) {
                    this._calculators[stat] = new Calculator();
                }

                this._calculators[stat].addFunc(f);
            }
        }
    }

    public final void addStatFuncs(List<Func> funcs) {
        List<Stats> modifiedStats = new ArrayList<>();

        for (Func f : funcs) {
            modifiedStats.add(f.stat);
            this.addStatFunc(f);
        }

        this.broadcastModifiedStats(modifiedStats);
    }

    public final void removeStatsByOwner(Object owner) {
        List<Stats> modifiedStats = null;
        int i = 0;
        synchronized (this._calculators) {
            for (Calculator calc : this._calculators) {
                if (calc != null) {
                    if (modifiedStats != null) {
                        modifiedStats.addAll(calc.removeOwner(owner));
                    } else {
                        modifiedStats = calc.removeOwner(owner);
                    }

                    if (calc.size() == 0) {
                        this._calculators[i] = null;
                    }
                }

                ++i;
            }

            if (owner instanceof L2Effect) {
                if (!((L2Effect) owner).preventExitUpdate) {
                    this.broadcastModifiedStats(modifiedStats);
                }
            } else {
                this.broadcastModifiedStats(modifiedStats);
            }

        }
    }

    private void broadcastModifiedStats(List<Stats> stats) {
        if (stats != null && !stats.isEmpty()) {
            boolean broadcastFull = false;
            StatusUpdate su = null;
            if (this instanceof Summon && ((Summon) this).getOwner() != null) {
                ((Summon) this).updateAndBroadcastStatusAndInfos(1);
            } else {
                for (Stats stat : stats) {
                    if (stat == Stats.POWER_ATTACK_SPEED) {
                        if (su == null) {
                            su = new StatusUpdate(this);
                        }

                        su.addAttribute(18, this.getPAtkSpd());
                    } else if (stat == Stats.MAGIC_ATTACK_SPEED) {
                        if (su == null) {
                            su = new StatusUpdate(this);
                        }

                        su.addAttribute(24, this.getMAtkSpd());
                    } else if (stat == Stats.MAX_HP && this instanceof Attackable) {
                        if (su == null) {
                            su = new StatusUpdate(this);
                        }

                        su.addAttribute(10, this.getMaxHp());
                    } else if (stat == Stats.RUN_SPEED) {
                        broadcastFull = true;
                    }
                }
            }

            if (this instanceof Player) {
                if (broadcastFull) {
                    ((Player) this).updateAndBroadcastStatus(2);
                } else {
                    ((Player) this).updateAndBroadcastStatus(1);
                    if (su != null) {
                        this.broadcastPacket(su);
                    }
                }
            } else if (this instanceof Npc) {
                if (broadcastFull) {
                    for (Player player : this.getKnownType(Player.class)) {
                        if (this.getMoveSpeed() == 0) {
                            player.sendPacket(new ServerObjectInfo((Npc) this, player));
                        } else {
                            player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) this, player));
                        }
                    }
                } else if (su != null) {
                    this.broadcastPacket(su);
                }
            } else if (su != null) {
                this.broadcastPacket(su);
            }

        }
    }

    public final int getXdestination() {
        MoveData m = this._move;
        return m != null ? m._xDestination : this.getX();
    }

    public final int getYdestination() {
        MoveData m = this._move;
        return m != null ? m._yDestination : this.getY();
    }

    public final int getZdestination() {
        MoveData m = this._move;
        return m != null ? m._zDestination : this.getZ();
    }

    public boolean isInCombat() {
        return this.hasAI() && AttackStanceTaskManager.getInstance().isInAttackStance(this);
    }

    public final boolean isMoving() {
        return this._move != null;
    }

    public final boolean isOnGeodataPath() {
        MoveData m = this._move;
        if (m == null) {
            return false;
        } else if (m.onGeodataPathIndex == -1) {
            return false;
        } else {
            return m.onGeodataPathIndex != m.geoPath.size() - 1;
        }
    }

    public final boolean isCastingNow() {
        return this._isCastingNow;
    }

    public void setIsCastingNow(boolean value) {
        this._isCastingNow = value;
    }

    public final boolean isCastingSimultaneouslyNow() {
        return this._isCastingSimultaneouslyNow;
    }

    public void setIsCastingSimultaneouslyNow(boolean value) {
        this._isCastingSimultaneouslyNow = value;
    }

    public final boolean canAbortCast() {
        return this._castInterruptTime > System.currentTimeMillis();
    }

    public boolean isAttackingNow() {
        return this._attackEndTime > System.currentTimeMillis();
    }

    public final void abortAttack() {
        if (this.isAttackingNow()) {
            this.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public final void abortCast() {
        if (this.isCastingNow() || this.isCastingSimultaneouslyNow()) {
            Future<?> future = this._skillCast;
            if (future != null) {
                future.cancel(true);
                this._skillCast = null;
            }

            future = this._skillCast2;
            if (future != null) {
                future.cancel(true);
                this._skillCast2 = null;
            }

            if (this.getFusionSkill() != null) {
                this.getFusionSkill().onCastAbort();
            }

            L2Effect mog = this.getFirstEffect(L2EffectType.SIGNET_GROUND);
            if (mog != null) {
                mog.exit();
            }

            if (this._allSkillsDisabled) {
                this.enableAllSkills();
            }

            this.setIsCastingNow(false);
            this.setIsCastingSimultaneouslyNow(false);
            this._castInterruptTime = 0L;
            if (this instanceof Playable) {
                this.getAI().notifyEvent(AiEventType.FINISH_CASTING);
            }

            this.broadcastPacket(new MagicSkillCanceled(this.getObjectId()));
            this.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public boolean updatePosition() {
        MoveData m = this._move;
        if (m == null) {
            return true;
        } else if (!this.isVisible()) {
            this._move = null;
            return true;
        } else {
            if (m._moveTimestamp == 0L) {
                m._moveTimestamp = m._moveStartTime;
                m._xAccurate = this.getX();
                m._yAccurate = this.getY();
            }

            long time = System.currentTimeMillis();
            if (m._moveTimestamp > time) {
                return false;
            } else {
                int xPrev = this.getX();
                int yPrev = this.getY();
                int zPrev = this.getZ();
                double dx = (double) m._xDestination - m._xAccurate;
                double dy = (double) m._yDestination - m._yAccurate;
                boolean isFloating = this.isFlying() || this.isInsideZone(ZoneId.WATER);
                double dz;
                if (!isFloating && !m.disregardingGeodata && Rnd.get(10) == 0 && GeoEngine.getInstance().hasGeo(xPrev, yPrev)) {
                    short geoHeight = GeoEngine.getInstance().getHeight(xPrev, yPrev, zPrev);
                    dz = m._zDestination - geoHeight;
                    if (this instanceof Player && Math.abs(((Player) this).getClientZ() - geoHeight) > 200 && Math.abs(((Player) this).getClientZ() - geoHeight) < 1500) {
                        dz = m._zDestination - zPrev;
                    } else if (this.isInCombat() && Math.abs(dz) > (double) 200.0F && dx * dx + dy * dy < (double) 40000.0F) {
                        dz = m._zDestination - zPrev;
                    } else {
                        zPrev = geoHeight;
                    }
                } else {
                    dz = m._zDestination - zPrev;
                }

                double delta = dx * dx + dy * dy;
                if (delta < (double) 10000.0F && dz * dz > (double) 2500.0F && !isFloating) {
                    delta = Math.sqrt(delta);
                } else {
                    delta = Math.sqrt(delta + dz * dz);
                }

                double distFraction = Double.MAX_VALUE;
                if (delta > (double) 1.0F) {
                    double distPassed = this.getStat().getMoveSpeed() * (float) (time - m._moveTimestamp) / 1000.0F;
                    distFraction = distPassed / delta;
                }

                if (distFraction > (double) 1.0F) {
                    this.setXYZ(m._xDestination, m._yDestination, m._zDestination);
                } else {
                    m._xAccurate += dx * distFraction;
                    m._yAccurate += dy * distFraction;
                    this.setXYZ((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) (dz * distFraction + (double) 0.5F));
                }

                this.revalidateZone(false);
                m._moveTimestamp = time;
                return distFraction > (double) 1.0F;
            }
        }
    }

    public void revalidateZone(boolean force) {
        if (this.getRegion() != null) {
            if (force) {
                this._zoneValidateCounter = 4;
            } else {
                --this._zoneValidateCounter;
                if (this._zoneValidateCounter >= 0) {
                    return;
                }

                this._zoneValidateCounter = 4;
            }

            this.getRegion().revalidateZones(this);
        }
    }

    public void stopMove(SpawnLocation loc) {
        this._move = null;
        if (loc != null) {
            this.setXYZ(loc);
            this.revalidateZone(true);
        }

        this.broadcastPacket(new StopMove(this));
    }

    public boolean isShowSummonAnimation() {
        return this._showSummonAnimation;
    }

    public void setShowSummonAnimation(boolean showSummonAnimation) {
        this._showSummonAnimation = showSummonAnimation;
    }

    public final int getTargetId() {
        return this._target != null ? this._target.getObjectId() : -1;
    }

    public final WorldObject getTarget() {
        return this._target;
    }

    public void setTarget(WorldObject object) {
        if (object != null && !object.isVisible()) {
            object = null;
        }

        this._target = object;
    }

    public void moveToLocation(int x, int y, int z, int offset) {
        double speed = this.getStat().getMoveSpeed();
        if (!(speed <= (double) 0.0F) && !this.isMovementDisabled()) {
            int curX = this.getX();
            int curY = this.getY();
            int curZ = this.getZ();
            double dx = x - curX;
            double dy = y - curY;
            double dz = z - curZ;
            double distance = Math.sqrt(dx * dx + dy * dy);
            boolean verticalMovementOnly = this.isFlying() && distance == (double) 0.0F && dz != (double) 0.0F;
            if (verticalMovementOnly) {
                distance = Math.abs(dz);
            }

            if (this.isInsideZone(ZoneId.WATER) && distance > (double) 700.0F) {
                double divider = (double) 700.0F / distance;
                x = curX + (int) (divider * dx);
                y = curY + (int) (divider * dy);
                z = curZ + (int) (divider * dz);
                dx = x - curX;
                dy = y - curY;
                dz = z - curZ;
                distance = Math.sqrt(dx * dx + dy * dy);
            }

            double sin;
            double cos;
            if (offset <= 0 && !(distance < (double) 1.0F)) {
                sin = dy / distance;
                cos = dx / distance;
            } else {
                offset = (int) ((double) offset - Math.abs(dz));
                if (offset < 5) {
                    offset = 5;
                }

                if (distance < (double) 1.0F || distance - (double) offset <= (double) 0.0F) {
                    this.getAI().notifyEvent(AiEventType.ARRIVED);
                    return;
                }

                sin = dy / distance;
                cos = dx / distance;
                distance -= offset - 5;
                x = curX + (int) (distance * cos);
                y = curY + (int) (distance * sin);
            }

            MoveData newMd = new MoveData();
            newMd.onGeodataPathIndex = -1;
            newMd.disregardingGeodata = false;
            if (!this.isFlying() && (!this.isInsideZone(ZoneId.WATER) || this.isInsideZone(ZoneId.SIEGE)) && !(this instanceof Walker)) {
                boolean isInBoat = this instanceof Player && ((Player) this).getBoat() != null;
                if (isInBoat) {
                    newMd.disregardingGeodata = true;
                }

                double originalDistance = distance;
                int originalX = x;
                int originalY = y;
                int originalZ = z;
                int gtx = x - -131072 >> 4;
                int gty = y - -262144 >> 4;
                if (!(this instanceof Attackable) || !((Attackable) this).isReturningToSpawnPoint() || this instanceof Player && (!isInBoat || !(distance > (double) 1500.0F)) || this instanceof Summon && this.getAI().getDesire().getIntention() != IntentionType.FOLLOW || this.isAfraid() || this instanceof RiftInvader) {
                    if (this.isOnGeodataPath()) {
                        try {
                            if (gtx == this._move.geoPathGtx && gty == this._move.geoPathGty) {
                                return;
                            }

                            this._move.onGeodataPathIndex = -1;
                        } catch (NullPointerException ignored) {
                        }
                    }

                    if (curX < -131072 || curX > 229376 || curY < -262144 || curY > 262144) {
                        this.getAI().setIntention(IntentionType.IDLE);
                        if (this instanceof Player) {
                            ((Player) this).logout(false);
                        } else {
                            if (this instanceof Summon) {
                                return;
                            }

                            this.onDecay();
                        }

                        return;
                    }

                    Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(curX, curY, curZ, x, y, z);
                    x = destiny.getX();
                    y = destiny.getY();
                    z = destiny.getZ();
                    dx = x - curX;
                    dy = y - curY;
                    dz = z - curZ;
                    distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
                }

                if (originalDistance - distance > (double) 30.0F && distance < (double) 2000.0F && !this.isAfraid() && (this instanceof Playable && !isInBoat || this.isMinion() || this.isInCombat())) {
                    newMd.geoPath = GeoEngine.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, this instanceof Playable);
                    if (newMd.geoPath != null && newMd.geoPath.size() >= 2) {
                        newMd.onGeodataPathIndex = 0;
                        newMd.geoPathGtx = gtx;
                        newMd.geoPathGty = gty;
                        newMd.geoPathAccurateTx = originalX;
                        newMd.geoPathAccurateTy = originalY;
                        x = newMd.geoPath.get(newMd.onGeodataPathIndex).getX();
                        y = newMd.geoPath.get(newMd.onGeodataPathIndex).getY();
                        z = newMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
                        dx = x - curX;
                        dy = y - curY;
                        dz = z - curZ;
                        distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
                        sin = dy / distance;
                        cos = dx / distance;
                    } else {
                        if (this instanceof Player || !(this instanceof Playable) && !this.isMinion() && Math.abs(z - curZ) > 140 || this instanceof Summon && !((Summon) this).getFollowStatus()) {
                            return;
                        }

                        newMd.disregardingGeodata = true;
                        x = originalX;
                        y = originalY;
                        z = originalZ;
                        distance = originalDistance;
                    }
                }

                if (distance < (double) 1.0F) {
                    if (this instanceof Summon) {
                        ((Summon) this).setFollowStatus(false);
                    }

                    this.getAI().setIntention(IntentionType.IDLE);
                    return;
                }
            }

            if ((this.isFlying() || this.isInsideZone(ZoneId.WATER)) && !verticalMovementOnly) {
                Math.sqrt(distance * distance + dz * dz);
            }

            newMd._xDestination = x;
            newMd._yDestination = y;
            newMd._zDestination = z;
            newMd._heading = 0;
            newMd._moveStartTime = System.currentTimeMillis();
            this._move = newMd;
            if (!verticalMovementOnly) {
                this.getPosition().setHeading(MathUtil.calculateHeadingFrom(cos, sin));
            }

            MovementTaskManager.getInstance().add(this);
        }
    }

    public boolean moveToNextRoutePoint() {
        if (!this.isOnGeodataPath()) {
            this._move = null;
            return false;
        } else if (!(this.getStat().getMoveSpeed() <= 0.0F) && !this.isMovementDisabled()) {
            MoveData oldMd = this._move;
            MoveData newMd = new MoveData();
            newMd.onGeodataPathIndex = oldMd.onGeodataPathIndex + 1;
            newMd.geoPath = oldMd.geoPath;
            newMd.geoPathGtx = oldMd.geoPathGtx;
            newMd.geoPathGty = oldMd.geoPathGty;
            newMd.geoPathAccurateTx = oldMd.geoPathAccurateTx;
            newMd.geoPathAccurateTy = oldMd.geoPathAccurateTy;
            if (oldMd.onGeodataPathIndex == oldMd.geoPath.size() - 2) {
                newMd._xDestination = oldMd.geoPathAccurateTx;
                newMd._yDestination = oldMd.geoPathAccurateTy;
                newMd._zDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
            } else {
                newMd._xDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getX();
                newMd._yDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getY();
                newMd._zDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
            }

            newMd._heading = 0;
            newMd._moveStartTime = System.currentTimeMillis();
            this._move = newMd;
            double dx = this._move._xDestination - super.getX();
            double dy = this._move._yDestination - super.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance != (double) 0.0F) {
                this.getPosition().setHeading(MathUtil.calculateHeadingFrom(dx, dy));
            }

            MovementTaskManager.getInstance().add(this);
            this.broadcastPacket(new MoveToLocation(this));
            return true;
        } else {
            this._move = null;
            return false;
        }
    }

    public boolean validateMovementHeading(int heading) {
        MoveData m = this._move;
        if (m == null) {
            return true;
        } else {
            boolean result = true;
            if (m._heading != heading) {
                result = m._heading == 0;
                m._heading = heading;
            }

            return result;
        }
    }

    public final double getDistanceSq(WorldObject object) {
        return this.getDistanceSq(object.getX(), object.getY(), object.getZ());
    }

    public final double getDistanceSq(int x, int y, int z) {
        double dx = x - this.getX();
        double dy = y - this.getY();
        double dz = z - this.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public final double getPlanDistanceSq(int x, int y) {
        double dx = x - this.getX();
        double dy = y - this.getY();
        return dx * dx + dy * dy;
    }

    public final boolean isInsideRadius(WorldObject object, int radius, boolean checkZ, boolean strictCheck) {
        return this.isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
    }

    public final boolean isInsideRadius(Location loc, int radius, boolean checkZ, boolean strictCheck) {
        return this.isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZ, strictCheck);
    }

    public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck) {
        return this.isInsideRadius(x, y, 0, radius, false, strictCheck);
    }

    public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck) {
        double dx = x - this.getX();
        double dy = y - this.getY();
        double dz = z - this.getZ();
        if (strictCheck) {
            if (checkZ) {
                return dx * dx + dy * dy + dz * dz < (double) (radius * radius);
            } else {
                return dx * dx + dy * dy < (double) (radius * radius);
            }
        } else if (checkZ) {
            return dx * dx + dy * dy + dz * dz <= (double) (radius * radius);
        } else {
            return dx * dx + dy * dy <= (double) (radius * radius);
        }
    }

    protected boolean checkAndEquipArrows() {
        return true;
    }

    public void addExpAndSp(long addToExp, int addToSp) {
    }

    public abstract ItemInstance getActiveWeaponInstance();

    public abstract Weapon getActiveWeaponItem();

    public abstract ItemInstance getSecondaryWeaponInstance();

    public abstract Item getSecondaryWeaponItem();

    public WeaponType getAttackType() {
        Weapon weapon = this.getActiveWeaponItem();
        return weapon == null ? WeaponType.NONE : weapon.getItemType();
    }

    protected void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld) {
        if (!this.isCastingNow() && !this.cantAttack()) {
            if (target != null && !this.isAlikeDead()) {
                if ((!(this instanceof Npc) || !target.isAlikeDead()) && !target.isDead() && (this.getKnownType(Creature.class).contains(target) || this instanceof Door)) {
                    if (miss) {
                        if (target.hasAI()) {
                            target.getAI().notifyEvent(AiEventType.EVADED, this);
                        }

                        if (target.getChanceSkills() != null) {
                            target.getChanceSkills().onEvadedHit(this);
                        }

                        if (target instanceof Player) {
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(this));
                        }
                    }

                    this.sendDamageMessage(target, damage, false, crit, miss);
                    if (!Config.RAID_DISABLE_CURSE && target.isRaidRelated() && this.getLevel() > target.getLevel() + 8) {
                        L2Skill skill = FrequentSkill.RAID_CURSE2.getSkill();
                        if (skill != null) {
                            this.broadcastPacket(new MagicSkillUse(this, this, skill.getId(), skill.getLevel(), 300, 0));
                            skill.getEffects(this, this);
                        }

                        damage = 0;
                    }

                    if (!miss && damage > 0) {
                        if (target instanceof Player) {
                            target.getAI().startAttackStance();
                        }

                        boolean isBow = this.getAttackType() == WeaponType.BOW;
                        int reflectedDamage = 0;
                        if (!isBow && !target.isInvul() && (!target.isRaidRelated() || this.getActingPlayer() == null || this.getActingPlayer().getLevel() <= target.getLevel() + 8)) {
                            double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0F, null, null);
                            if (reflectPercent > (double) 0.0F) {
                                reflectedDamage = (int) (reflectPercent / (double) 100.0F * (double) damage);
                                int currentHp = (int) this.getCurrentHp();
                                if (reflectedDamage >= currentHp) {
                                    reflectedDamage = currentHp - 1;
                                }
                            }
                        }

                        target.reduceCurrentHp(damage, this, null);
                        if (reflectedDamage > 0) {
                            this.reduceCurrentHp(reflectedDamage, target, true, false, null);
                        }

                        if (!isBow) {
                            double absorbPercent = this.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0F, null, null);
                            if (absorbPercent > (double) 0.0F) {
                                int maxCanAbsorb = (int) ((double) this.getMaxHp() - this.getCurrentHp());
                                int absorbDamage = (int) (absorbPercent / (double) 100.0F * (double) damage);
                                if (absorbDamage > maxCanAbsorb) {
                                    absorbDamage = maxCanAbsorb;
                                }

                                if (absorbDamage > 0) {
                                    this.setCurrentHp(this.getCurrentHp() + (double) absorbDamage);
                                }
                            }
                        }

                        Formulas.calcCastBreak(target, damage);
                        if (this._chanceSkills != null) {
                            this._chanceSkills.onHit(target, false, crit);
                            if (reflectedDamage > 0) {
                                this._chanceSkills.onHit(target, true, false);
                            }
                        }

                        if (target.getChanceSkills() != null) {
                            target.getChanceSkills().onHit(this, true, crit);
                        }
                    }

                    Weapon activeWeapon = this.getActiveWeaponItem();
                    if (activeWeapon != null) {
                        activeWeapon.getSkillEffects(this, target, crit);
                    }

                } else {
                    this.getAI().notifyEvent(AiEventType.CANCEL);
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                }
            } else {
                this.getAI().notifyEvent(AiEventType.CANCEL);
            }
        }
    }

    public void breakAttack() {
        if (this.isAttackingNow()) {
            this.abortAttack();
            if (this instanceof Player) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
            }
        }

    }

    public void breakCast() {
        if (this.isCastingNow() && this.canAbortCast() && this.getLastSkillCast() != null && this.getLastSkillCast().isMagic()) {
            this.abortCast();
            if (this instanceof Player) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
            }
        }

    }

    protected void reduceArrowCount() {
    }

    public void onForcedAttack(Player player) {
        if (isInsidePeaceZone(player, this)) {
            player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget() instanceof Playable) {
                Player target = player.getTarget().getActingPlayer();
                if (target == null || target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId())) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }
            }

            if (player.getTarget() != null && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack()) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (player.isConfused()) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (!GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            }
        }
    }

    public boolean canInteract(Player player) {
        if (!player.isCastingNow() && !player.isCastingSimultaneouslyNow()) {
            if (!player.isDead() && !player.isFakeDeath()) {
                if (player.isSitting()) {
                    return false;
                } else if (!player.isInStoreMode() && !player.isProcessingTransaction()) {
                    return this.isInsideRadius(player, 150, true, false);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isInActiveRegion() {
        WorldRegion region = this.getRegion();
        return region != null && region.isActive();
    }

    public boolean isInParty() {
        return false;
    }

    public Party getParty() {
        return null;
    }

    public int calculateTimeBetweenAttacks(Creature target, WeaponType weaponType) {
        switch (weaponType) {
            case BOW -> {
                return 517500 / this.getStat().getPAtkSpd();
            }
            default -> {
                return Formulas.calcPAtkSpd(this, target, this.getStat().getPAtkSpd());
            }
        }
    }

    public ChanceSkillList getChanceSkills() {
        return this._chanceSkills;
    }

    public void removeChanceSkill(int id) {
        if (this._chanceSkills != null) {
            for (IChanceSkillTrigger trigger : this._chanceSkills.keySet()) {
                if (trigger instanceof L2Skill && ((L2Skill) trigger).getId() == id) {
                    this._chanceSkills.remove(trigger);
                }
            }

        }
    }

    public void addChanceTrigger(IChanceSkillTrigger trigger) {
        if (this._chanceSkills == null) {
            this._chanceSkills = new ChanceSkillList(this);
        }

        this._chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
    }

    public void removeChanceEffect(EffectChanceSkillTrigger effect) {
        if (this._chanceSkills != null) {
            this._chanceSkills.remove(effect);
        }
    }

    public void onStartChanceEffect() {
        if (this._chanceSkills != null) {
            this._chanceSkills.onStart();
        }
    }

    public void onActionTimeChanceEffect() {
        if (this._chanceSkills != null) {
            this._chanceSkills.onActionTime();
        }
    }

    public void onExitChanceEffect() {
        if (this._chanceSkills != null) {
            this._chanceSkills.onExit();
        }
    }

    public Map<Integer, L2Skill> getSkills() {
        return Collections.emptyMap();
    }

    public int getSkillLevel(int skillId) {
        L2Skill skill = this.getSkills().get(skillId);
        return skill == null ? 0 : skill.getLevel();
    }

    public L2Skill getSkill(int skillId) {
        return this.getSkills().get(skillId);
    }

    public boolean hasSkill(int skillId) {
        return this.getSkills().containsKey(skillId);
    }

    public int getBuffCount() {
        return this._effects.getBuffCount();
    }

    public int getDanceCount() {
        return this._effects.getDanceCount();
    }

    public void onMagicLaunchedTimer(MagicUseTask mut) {
        L2Skill skill = mut.skill;
        WorldObject[] targets = mut.targets;
        if (skill != null && targets != null) {
            if (targets.length == 0) {
                switch (skill.getTargetType()) {
                    case TARGET_AURA:
                    case TARGET_FRONT_AURA:
                    case TARGET_BEHIND_AURA:
                    case TARGET_AURA_UNDEAD:
                        break;
                    default:
                        this.abortCast();
                        return;
                }
            }

            int escapeRange = 0;
            if (skill.getEffectRange() > escapeRange) {
                escapeRange = skill.getEffectRange();
            } else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80) {
                escapeRange = skill.getSkillRadius();
            }

            if (targets.length > 0 && escapeRange > 0) {
                int _skiprange = 0;
                int _skipgeo = 0;
                int _skippeace = 0;
                List<Creature> targetList = new ArrayList<>(targets.length);

                for (WorldObject target : targets) {
                    if (target instanceof Creature) {
                        if (!MathUtil.checkIfInRange(escapeRange, this, target, true)) {
                            ++_skiprange;
                        } else if (skill.getSkillRadius() > 0 && skill.isOffensive() && !GeoEngine.getInstance().canSeeTarget(this, target)) {
                            ++_skipgeo;
                        } else if (skill.isOffensive() && isInsidePeaceZone(this, target)) {
                            ++_skippeace;
                        } else {
                            targetList.add((Creature) target);
                        }
                    }
                }

                if (targetList.isEmpty()) {
                    if (this instanceof Player) {
                        if (_skiprange > 0) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
                        } else if (_skipgeo > 0) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
                        } else if (_skippeace > 0) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
                        }
                    }

                    this.abortCast();
                    return;
                }

                mut.targets = targetList.toArray(new Creature[targetList.size()]);
            }

            if ((!mut.simultaneously || this.isCastingSimultaneouslyNow()) && (mut.simultaneously || this.isCastingNow()) && (!this.isAlikeDead() || skill.isPotion())) {
                mut.phase = 2;
                if (mut.hitTime == 0) {
                    this.onMagicHitTimer(mut);
                } else {
                    this._skillCast = ThreadPool.schedule(mut, 400L);
                }

            } else {
                this.getAI().notifyEvent(AiEventType.CANCEL);
            }
        } else {
            this.abortCast();
        }
    }

    public void onMagicHitTimer(MagicUseTask mut) {
        L2Skill skill = mut.skill;
        WorldObject[] targets = mut.targets;
        if (skill != null && targets != null) {
            if (this.getFusionSkill() != null) {
                if (mut.simultaneously) {
                    this._skillCast2 = null;
                    this.setIsCastingSimultaneouslyNow(false);
                } else {
                    this._skillCast = null;
                    this.setIsCastingNow(false);
                }

                this.getFusionSkill().onCastAbort();
                this.notifyQuestEventSkillFinished(skill, targets[0]);
            } else {
                L2Effect mog = this.getFirstEffect(L2EffectType.SIGNET_GROUND);
                if (mog != null) {
                    if (mut.simultaneously) {
                        this._skillCast2 = null;
                        this.setIsCastingSimultaneouslyNow(false);
                    } else {
                        this._skillCast = null;
                        this.setIsCastingNow(false);
                    }

                    mog.exit();
                    this.notifyQuestEventSkillFinished(skill, targets[0]);
                } else {
                    for (WorldObject tgt : targets) {
                        if (tgt instanceof Playable) {
                            if (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SEED) {
                                ((Creature) tgt).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
                            }

                            if (this instanceof Player && tgt instanceof Summon) {
                                ((Summon) tgt).updateAndBroadcastStatus(1);
                            }
                        }
                    }

                    StatusUpdate su = new StatusUpdate(this);
                    boolean isSendStatus = false;
                    double mpConsume = this.getStat().getMpConsume(skill);
                    if (mpConsume > (double) 0.0F) {
                        if (mpConsume > this.getCurrentMp()) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
                            this.abortCast();
                            return;
                        }

                        this.getStatus().reduceMp(mpConsume);
                        su.addAttribute(11, (int) this.getCurrentMp());
                        isSendStatus = true;
                    }

                    double hpConsume = skill.getHpConsume();
                    if (hpConsume > (double) 0.0F) {
                        if (hpConsume > this.getCurrentHp()) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
                            this.abortCast();
                            return;
                        }

                        this.getStatus().reduceHp(hpConsume, this, true);
                        su.addAttribute(9, (int) this.getCurrentHp());
                        isSendStatus = true;
                    }

                    if (isSendStatus) {
                        this.sendPacket(su);
                    }

                    if (this instanceof Player) {
                        int charges = ((Player) this).getCharges();
                        if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges()) {
                            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                            sm.addSkillName(skill);
                            this.sendPacket(sm);
                            this.abortCast();
                            return;
                        }

                        if (skill.getNumCharges() > 0) {
                            if (skill.getMaxCharges() > 0) {
                                ((Player) this).increaseCharges(skill.getNumCharges(), skill.getMaxCharges());
                            } else {
                                ((Player) this).decreaseCharges(skill.getNumCharges());
                            }
                        }
                    }

                    this.callSkill(mut.skill, mut.targets);
                    mut.phase = 3;
                    if (mut.hitTime != 0 && mut.coolTime != 0) {
                        if (mut.simultaneously) {
                            this._skillCast2 = ThreadPool.schedule(mut, mut.coolTime);
                        } else {
                            this._skillCast = ThreadPool.schedule(mut, mut.coolTime);
                        }
                    } else {
                        this.onMagicFinalizer(mut);
                    }

                }
            }
        } else {
            this.abortCast();
        }
    }

    public void onMagicFinalizer(MagicUseTask mut) {
        if (mut.simultaneously) {
            this._skillCast2 = null;
            this.setIsCastingSimultaneouslyNow(false);
        } else {
            this._skillCast = null;
            this._castInterruptTime = 0L;
            this.setIsCastingNow(false);
            this.setIsCastingSimultaneouslyNow(false);
            L2Skill skill = mut.skill;
            WorldObject target = mut.targets.length > 0 ? mut.targets[0] : null;
            if (skill.nextActionIsAttack() && this.getTarget() instanceof Creature && this.getTarget() != this && this.getTarget() == target && this.getTarget().isAutoAttackable(this) && (this.getAI().getNextIntention() == null || this.getAI().getNextIntention().getIntention() != IntentionType.MOVE_TO)) {
                this.getAI().setIntention(IntentionType.ATTACK, target);
            }

            if (skill.isOffensive() && skill.getSkillType() != L2SkillType.UNLOCK && skill.getSkillType() != L2SkillType.DELUXE_KEY_UNLOCK) {
                this.getAI().startAttackStance();
            }

            this.getAI().notifyEvent(AiEventType.FINISH_CASTING);
            this.notifyQuestEventSkillFinished(skill, target);
            if (this instanceof Playable) {
                boolean isPlayer = this instanceof Player;
                Player player = this.getActingPlayer();
                if (isPlayer) {
                    player.setCurrentSkill(null, false, false);
                    SkillUseHolder queuedSkill = player.getQueuedSkill();
                    if (queuedSkill.getSkill() != null) {
                        ThreadPool.execute(new QueuedMagicUseTask(player, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
                        player.setQueuedSkill(null, false, false);
                    }
                } else {
                    player.setCurrentPetSkill(null, false, false);
                }
            }

        }
    }

    protected void notifyQuestEventSkillFinished(L2Skill skill, WorldObject target) {
    }

    public Map<Integer, Long> getDisabledSkills() {
        return this._disabledSkills;
    }

    public void enableSkill(L2Skill skill) {
        if (skill != null) {
            this._disabledSkills.remove(skill.getReuseHashCode());
        }
    }

    public void disableSkill(L2Skill skill, long delay) {
        if (skill != null) {
            this._disabledSkills.put(skill.getReuseHashCode(), delay > 10L ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
        }
    }

    public boolean isSkillDisabled(L2Skill skill) {
        if (this._disabledSkills.isEmpty()) {
            return false;
        } else if (skill != null && !this.isAllSkillsDisabled()) {
            int hashCode = skill.getReuseHashCode();
            Long timeStamp = this._disabledSkills.get(hashCode);
            if (timeStamp == null) {
                return false;
            } else if (timeStamp < System.currentTimeMillis()) {
                this._disabledSkills.remove(hashCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void disableAllSkills() {
        this._allSkillsDisabled = true;
    }

    public void enableAllSkills() {
        this._allSkillsDisabled = false;
    }

    public void callSkill(L2Skill skill, WorldObject[] targets) {
        try {
            if (skill.isToggle() && this.getFirstEffect(skill.getId()) != null) {
                return;
            }

            for (WorldObject trg : targets) {
                if (trg instanceof Creature target) {
                    if (this instanceof Playable) {
                        if (!Config.RAID_DISABLE_CURSE) {
                            boolean isVictimTargetBoss = false;
                            if (!skill.isOffensive()) {
                                WorldObject victimTarget = target.hasAI() ? target.getAI().getTarget() : null;
                                if (victimTarget != null) {
                                    isVictimTargetBoss = victimTarget instanceof Creature && ((Creature) victimTarget).isRaidRelated() && this.getLevel() > ((Creature) victimTarget).getLevel() + 8;
                                }
                            }

                            if (target.isRaidRelated() && this.getLevel() > target.getLevel() + 8 || isVictimTargetBoss) {
                                L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
                                if (curse != null) {
                                    this.broadcastPacket(new MagicSkillUse(this, this, curse.getId(), curse.getLevel(), 300, 0));
                                    curse.getEffects(this, this);
                                }

                                return;
                            }
                        }

                        if (skill.isOverhit() && target instanceof Monster) {
                            ((Monster) target).overhitEnabled(true);
                        }
                    }

                    switch (skill.getSkillType()) {
                        case COMMON_CRAFT:
                        case DWARVEN_CRAFT:
                            break;
                        default:
                            if (this.getActiveWeaponItem() != null && !target.isDead() && this instanceof Player && !this.getActiveWeaponItem().getSkillEffects(this, target, skill).isEmpty()) {
                                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED).addSkillName(skill));
                            }

                            if (this._chanceSkills != null) {
                                this._chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
                            }

                            if (target.getChanceSkills() != null) {
                                target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
                            }
                    }
                }
            }

            ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
            if (handler != null) {
                handler.useSkill(this, skill, targets);
            } else {
                skill.useSkill(this, targets);
            }

            Player player = this.getActingPlayer();
            if (player != null) {
                for (WorldObject target : targets) {
                    if (target instanceof Creature) {
                        if (skill.isOffensive()) {
                            if (target instanceof Playable) {
                                if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME) {
                                    ((Creature) target).getAI().startAttackStance();
                                    if (player.getSummon() != target) {
                                        player.updatePvPStatus((Creature) target);
                                    }
                                }
                            } else if (target instanceof Attackable && skill.getId() != 51) {
                                ((Attackable) target).addAttacker(this);
                            }

                            if (((Creature) target).hasAI()) {
                                switch (skill.getSkillType()) {
                                    case AGGREDUCE:
                                    case AGGREDUCE_CHAR:
                                    case AGGREMOVE:
                                        break;
                                    default:
                                        ((Creature) target).getAI().notifyEvent(AiEventType.ATTACKED, this);
                                }
                            }
                        } else if (target instanceof Player) {
                            if (!target.equals(this) && !target.equals(player) && (((Player) target).getPvpFlag() > 0 || ((Player) target).getKarma() > 0)) {
                                player.updatePvPStatus();
                            }
                        } else if (target instanceof Attackable && !((Attackable) target).isGuard()) {
                            switch (skill.getSkillType()) {
                                case SUMMON:
                                case BEAST_FEED:
                                case UNLOCK:
                                case UNLOCK_SPECIAL:
                                case DELUXE_KEY_UNLOCK:
                                    break;
                                default:
                                    player.updatePvPStatus();
                            }
                        }

                        switch (skill.getTargetType()) {
                            case TARGET_CORPSE_MOB:
                            case TARGET_AREA_CORPSE_MOB:
                                if (((Creature) target).isDead()) {
                                    ((Npc) target).endDecayTask();
                                }
                        }
                    }
                }

                for (Npc npcMob : player.getKnownTypeInRadius(Npc.class, 1000)) {
                    List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
                    if (scripts != null) {
                        for (Quest quest : scripts) {
                            quest.notifySkillSee(npcMob, player, skill, targets, this instanceof Summon);
                        }
                    }
                }
            }

            if (skill.isOffensive()) {
                switch (skill.getSkillType()) {
                    case AGGREDUCE:
                    case AGGREDUCE_CHAR:
                    case AGGREMOVE:
                        return;
                    default:
                        for (WorldObject target : targets) {
                            if (target instanceof Creature && ((Creature) target).hasAI()) {
                                ((Creature) target).getAI().notifyEvent(AiEventType.ATTACKED, this);
                            }
                        }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't call skill {}.", e, skill == null ? "not found" : skill.getId());
        }

    }

    public boolean isBehind(Creature target) {
        if (target == null) {
            return false;
        } else {
            double maxAngleDiff = 60.0F;
            double angleChar = MathUtil.calculateAngleFrom(this, target);
            double angleTarget = MathUtil.convertHeadingToDegree(target.getHeading());
            double angleDiff = angleChar - angleTarget;
            if (angleDiff <= (double) -300.0F) {
                angleDiff += 360.0F;
            }

            if (angleDiff >= (double) 300.0F) {
                angleDiff -= 360.0F;
            }

            return Math.abs(angleDiff) <= (double) 60.0F;
        }
    }

    public boolean isBehindTarget() {
        WorldObject target = this.getTarget();
        return target instanceof Creature ? this.isBehind((Creature) target) : false;
    }

    public boolean isInFrontOf(Creature target) {
        if (target == null) {
            return false;
        } else {
            double maxAngleDiff = 60.0F;
            double angleTarget = MathUtil.calculateAngleFrom(target, this);
            double angleChar = MathUtil.convertHeadingToDegree(target.getHeading());
            double angleDiff = angleChar - angleTarget;
            if (angleDiff <= (double) -300.0F) {
                angleDiff += 360.0F;
            }

            if (angleDiff >= (double) 300.0F) {
                angleDiff -= 360.0F;
            }

            return Math.abs(angleDiff) <= (double) 60.0F;
        }
    }

    public boolean isFacing(WorldObject target, int maxAngle) {
        if (target == null) {
            return false;
        } else {
            double maxAngleDiff = maxAngle / 2;
            double angleTarget = MathUtil.calculateAngleFrom(this, target);
            double angleChar = MathUtil.convertHeadingToDegree(this.getHeading());
            double angleDiff = angleChar - angleTarget;
            if (angleDiff <= (double) -360.0F + maxAngleDiff) {
                angleDiff += 360.0F;
            }

            if (angleDiff >= (double) 360.0F - maxAngleDiff) {
                angleDiff -= 360.0F;
            }

            return Math.abs(angleDiff) <= maxAngleDiff;
        }
    }

    public boolean isInFrontOfTarget() {
        WorldObject target = this.getTarget();
        return target instanceof Creature ? this.isInFrontOf((Creature) target) : false;
    }

    public double getLevelMod() {
        return ((double) 89.0F + (double) this.getLevel()) / (double) 100.0F;
    }

    public final void setSkillCast(Future<?> newSkillCast) {
        this._skillCast = newSkillCast;
    }

    public final int getRandomDamage(Creature target) {
        Weapon weaponItem = this.getActiveWeaponItem();
        return weaponItem == null ? 5 + (int) Math.sqrt(this.getLevel()) : weaponItem.getRandomDamage();
    }

    public String toString() {
        return "mob " + this.getObjectId();
    }

    public long getAttackEndTime() {
        return this._attackEndTime;
    }

    public abstract int getLevel();

    public final double calcStat(Stats stat, double init, Creature target, L2Skill skill) {
        return this.getStat().calcStat(stat, init, target, skill);
    }

    public final int getCON() {
        return this.getStat().getCON();
    }

    public final int getDEX() {
        return this.getStat().getDEX();
    }

    public final int getINT() {
        return this.getStat().getINT();
    }

    public final int getMEN() {
        return this.getStat().getMEN();
    }

    public final int getSTR() {
        return this.getStat().getSTR();
    }

    public final int getWIT() {
        return this.getStat().getWIT();
    }

    public final int getAccuracy() {
        return this.getStat().getAccuracy();
    }

    public final int getCriticalHit(Creature target, L2Skill skill) {
        return this.getStat().getCriticalHit(target, skill);
    }

    public final int getEvasionRate(Creature target) {
        return this.getStat().getEvasionRate(target);
    }

    public final int getMDef(Creature target, L2Skill skill) {
        return this.getStat().getMDef(target, skill);
    }

    public final int getPDef(Creature target) {
        return this.getStat().getPDef(target);
    }

    public final int getShldDef() {
        return this.getStat().getShldDef();
    }

    public final int getPhysicalAttackRange() {
        return this.getStat().getPhysicalAttackRange();
    }

    public final int getPAtk(Creature target) {
        return this.getStat().getPAtk(target);
    }

    public final int getPAtkSpd() {
        int _patkspd = this.getStat().getPAtkSpd();
        return Config.MAX_PATK_SPEED > 0 && _patkspd > Config.MAX_PATK_SPEED ? Config.MAX_PATK_SPEED : _patkspd;
    }

    public final int getMAtk(Creature target, L2Skill skill) {
        return this.getStat().getMAtk(target, skill);
    }

    public final int getMAtkSpd() {
        int _matkspd = this.getStat().getMAtkSpd();
        return Config.MAX_MATK_SPEED > 0 && _matkspd > Config.MAX_MATK_SPEED ? Config.MAX_MATK_SPEED : _matkspd;
    }

    public final int getMCriticalHit(Creature target, L2Skill skill) {
        return this.getStat().getMCriticalHit(target, skill);
    }

    public final int getMaxMp() {
        return this.getStat().getMaxMp();
    }

    public int getMaxHp() {
        return this.getStat().getMaxHp();
    }

    public final int getMaxCp() {
        return this.getStat().getMaxCp();
    }

    public final double getPAtkAnimals(Creature target) {
        return this.getStat().getPAtkAnimals(target);
    }

    public final double getPAtkDragons(Creature target) {
        return this.getStat().getPAtkDragons(target);
    }

    public final double getPAtkInsects(Creature target) {
        return this.getStat().getPAtkInsects(target);
    }

    public final double getPAtkMonsters(Creature target) {
        return this.getStat().getPAtkMonsters(target);
    }

    public final double getPAtkPlants(Creature target) {
        return this.getStat().getPAtkPlants(target);
    }

    public final double getPAtkGiants(Creature target) {
        return this.getStat().getPAtkGiants(target);
    }

    public final double getPAtkMagicCreatures(Creature target) {
        return this.getStat().getPAtkMagicCreatures(target);
    }

    public final double getPDefAnimals(Creature target) {
        return this.getStat().getPDefAnimals(target);
    }

    public final double getPDefDragons(Creature target) {
        return this.getStat().getPDefDragons(target);
    }

    public final double getPDefInsects(Creature target) {
        return this.getStat().getPDefInsects(target);
    }

    public final double getPDefMonsters(Creature target) {
        return this.getStat().getPDefMonsters(target);
    }

    public final double getPDefPlants(Creature target) {
        return this.getStat().getPDefPlants(target);
    }

    public final double getPDefGiants(Creature target) {
        return this.getStat().getPDefGiants(target);
    }

    public final double getPDefMagicCreatures(Creature target) {
        return this.getStat().getPDefMagicCreatures(target);
    }

    public final int getMoveSpeed() {
        return (int) this.getStat().getMoveSpeed();
    }

    public void addStatusListener(Creature object) {
        this.getStatus().addStatusListener(object);
    }

    public void reduceCurrentHp(double i, Creature attacker, L2Skill skill) {
        this.reduceCurrentHp(i, attacker, true, false, skill);
    }

    public void reduceCurrentHpByDOT(double i, Creature attacker, L2Skill skill) {
        this.reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
    }

    public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (this.isChampion() && Config.CHAMPION_HP != 0) {
            this.getStatus().reduceHp(i / (double) Config.CHAMPION_HP, attacker, awake, isDOT, false);
        } else {
            this.getStatus().reduceHp(i, attacker, awake, isDOT, false);
        }

    }

    public void reduceCurrentMp(double i) {
        this.getStatus().reduceMp(i);
    }

    public void removeStatusListener(Creature object) {
        this.getStatus().removeStatusListener(object);
    }

    protected void stopHpMpRegeneration() {
        this.getStatus().stopHpMpRegeneration();
    }

    public final double getCurrentCp() {
        return this.getStatus().getCurrentCp();
    }

    public final void setCurrentCp(double newCp) {
        this.getStatus().setCurrentCp(newCp);
    }

    public final double getCurrentHp() {
        return this.getStatus().getCurrentHp();
    }

    public final void setCurrentHp(double newHp) {
        this.getStatus().setCurrentHp(newHp);
    }

    public final void setCurrentHpMp(double newHp, double newMp) {
        this.getStatus().setCurrentHpMp(newHp, newMp);
    }

    public final double getCurrentMp() {
        return this.getStatus().getCurrentMp();
    }

    public final void setCurrentMp(double newMp) {
        this.getStatus().setCurrentMp(newMp);
    }

    public boolean isChampion() {
        return this._champion;
    }

    public void setChampion(boolean champ) {
        this._champion = champ;
    }

    public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
    }

    public FusionSkill getFusionSkill() {
        return this._fusionSkill;
    }

    public void setFusionSkill(FusionSkill fb) {
        this._fusionSkill = fb;
    }

    public int getAttackElementValue(byte attackAttribute) {
        return this.getStat().getAttackElementValue(attackAttribute);
    }

    public double getDefenseElementValue(byte defenseAttribute) {
        return this.getStat().getDefenseElementValue(defenseAttribute);
    }

    public boolean isAffected(L2EffectFlag flag) {
        return this._effects.isAffected(flag);
    }

    public int getMaxBuffCount() {
        return Config.MAX_BUFFS_AMOUNT + this.getSkillLevel(1405);
    }

    public final double getRandomDamageMultiplier() {
        Weapon activeWeapon = this.getActiveWeaponItem();
        int random;
        if (activeWeapon != null) {
            random = activeWeapon.getRandomDamage();
        } else {
            random = 5 + (int) Math.sqrt(this.getLevel());
        }

        return (double) 1.0F + (double) Rnd.get(0 - random, random) / (double) 100.0F;
    }

    public void disableCoreAI(boolean val) {
        this._AIdisabled = val;
    }

    public boolean isCoreAIDisabled() {
        return this._AIdisabled;
    }

    public boolean isInArena() {
        return false;
    }

    public double getCollisionRadius() {
        return this.getTemplate().getCollisionRadius();
    }

    public double getCollisionHeight() {
        return this.getTemplate().getCollisionHeight();
    }

    public final void setRegion(WorldRegion newRegion) {
        if (this.getRegion() != null) {
            if (newRegion == null) {
                this.getRegion().removeFromZones(this);
            } else if (newRegion != this.getRegion()) {
                this.getRegion().revalidateZones(this);
            }
        }

        super.setRegion(newRegion);
        this.revalidateZone(true);
    }

    public void removeKnownObject(WorldObject object) {
        if (object == this.getTarget()) {
            this.setTarget(null);
        }

    }

    public TeamType getTeam() {
        return this._team;
    }

    public void setTeam(TeamType team) {
        this._team = team;
    }

    public final void setIsBuffProtected(boolean value) {
        this._isBuffProtected = value;
    }

    public boolean isBuffProtected() {
        return this._isBuffProtected;
    }

    public final void dispelSkillEffect(int skillId, int skillLevel) {
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
        if (skill != null) {
            if (!skill.isStayAfterDeath() && !skill.isDebuff()) {
                this._effects.stopSkillEffects(skill.getId());
            }
        }
    }

    public boolean isInArenaEvent() {
        return this.inArenaEvent;
    }

    public void setInArenaEvent(boolean val) {
        this.inArenaEvent = val;
    }

    public boolean isArenaAttack() {
        return this._ArenaAttack;
    }

    public void setArenaAttack(boolean comm) {
        this._ArenaAttack = comm;
    }

    public boolean isArenaProtection() {
        return this._ArenaProtection;
    }

    public void setArenaProtection(boolean comm) {
        this._ArenaProtection = comm;
    }

    public void sendChatMessage(int objectId, int messageType, String charName, String text) {
        this.sendPacket(new CreatureSay(objectId, messageType, charName, text));
    }

    public boolean isArena9x9() {
        return this._Arena9x9;
    }

    public void setArena9x9(boolean comm) {
        this._Arena9x9 = comm;
    }

    public boolean isArena4x4() {
        return this._Arena4x4;
    }

    public void setArena4x4(boolean comm) {
        this._Arena4x4 = comm;
    }

    public boolean isArena2x2() {
        return this._Arena2x2;
    }

    public void setArena2x2(boolean comm) {
        this._Arena2x2 = comm;
    }

    public boolean isArenaObserv() {
        return this._ArenaObserv;
    }

    public void setArenaObserv(boolean comm) {
        this._ArenaObserv = comm;
    }

    public boolean isStopArena() {
        return this._isStopMov;
    }

    public void setStopArena(boolean value) {
        this._isStopMov = value;
    }

    private static class QueuedMagicUseTask implements Runnable {
        private final Player _player;
        private final L2Skill _skill;
        private final boolean _isCtrlPressed;
        private final boolean _isShiftPressed;

        public QueuedMagicUseTask(Player player, L2Skill skill, boolean isCtrlPressed, boolean isShiftPressed) {
            this._player = player;
            this._skill = skill;
            this._isCtrlPressed = isCtrlPressed;
            this._isShiftPressed = isShiftPressed;
        }

        public void run() {
            try {
                this._player.useMagic(this._skill, this._isCtrlPressed, this._isShiftPressed);
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't process magic use for {}, skillId {}.", e, this._player == null ? "noname" : this._player.getName(), this._skill == null ? "not found" : this._skill.getId());
            }

        }
    }

    public static class MoveData {
        public long _moveStartTime;
        public long _moveTimestamp;
        public int _xDestination;
        public int _yDestination;
        public int _zDestination;
        public double _xAccurate;
        public double _yAccurate;
        public double _zAccurate;
        public int _heading;
        public boolean disregardingGeodata;
        public int onGeodataPathIndex;
        public List<Location> geoPath;
        public int geoPathAccurateTx;
        public int geoPathAccurateTy;
        public int geoPathGtx;
        public int geoPathGty;
    }

    class HitTask implements Runnable {
        Creature _hitTarget;
        int _damage;
        boolean _crit;
        boolean _miss;
        byte _shld;
        boolean _soulshot;

        public HitTask(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld) {
            this._hitTarget = target;
            this._damage = damage;
            this._crit = crit;
            this._shld = shld;
            this._miss = miss;
            this._soulshot = soulshot;
        }

        public void run() {
            Creature.this.onHitTimer(this._hitTarget, this._damage, this._crit, this._miss, this._soulshot, this._shld);
        }
    }

    class MagicUseTask implements Runnable {
        WorldObject[] targets;
        L2Skill skill;
        int hitTime;
        int coolTime;
        int phase;
        boolean simultaneously;

        public MagicUseTask(WorldObject[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous) {
            this.targets = tgts;
            this.skill = s;
            this.phase = 1;
            this.hitTime = hit;
            this.coolTime = coolT;
            this.simultaneously = simultaneous;
        }

        public void run() {
            try {
                switch (this.phase) {
                    case 1 -> Creature.this.onMagicLaunchedTimer(this);
                    case 2 -> Creature.this.onMagicHitTimer(this);
                    case 3 -> Creature.this.onMagicFinalizer(this);
                }
            } catch (Exception e) {
                WorldObject.LOGGER.error("Failed executing MagicUseTask on phase {} for skill {}.", e, this.phase, this.skill == null ? "not found" : this.skill.getName());
                if (this.simultaneously) {
                    Creature.this.setIsCastingSimultaneouslyNow(false);
                } else {
                    Creature.this.setIsCastingNow(false);
                }
            }

        }
    }

    private class FlyToLocationTask implements Runnable {
        private final WorldObject _tgt;
        private final Creature _actor;
        private final L2Skill _skill;

        public FlyToLocationTask(Creature actor, WorldObject target, L2Skill skill) {
            this._actor = actor;
            this._tgt = target;
            this._skill = skill;
        }

        public void run() {
            Creature.this.broadcastPacket(new FlyToLocation(this._actor, this._tgt, FlyType.valueOf(this._skill.getFlyType())));
            Creature.this.setXYZ(this._tgt.getX(), this._tgt.getY(), this._tgt.getZ());
        }
    }
}