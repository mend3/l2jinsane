/**/
package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.SkillBalanceManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.SkillChangeType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.*;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public abstract class L2Skill implements IChanceSkillTrigger {
    public static final int SKILL_LUCKY = 194;
    public static final int SKILL_EXPERTISE = 239;
    public static final int SKILL_SHADOW_SENSE = 294;
    public static final int SKILL_CREATE_COMMON = 1320;
    public static final int SKILL_CREATE_DWARVEN = 172;
    public static final int SKILL_CRYSTALLIZE = 248;
    public static final int SKILL_DIVINE_INSPIRATION = 1405;
    public static final int SKILL_NPC_RACE = 4416;
    public static final int COND_BEHIND = 8;
    public static final int COND_CRIT = 16;
    protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
    private static final WorldObject[] _emptyTargetList = new WorldObject[0];
    private final int _id;
    private final int _level;
    private final String _name;
    private final SkillOpType _operateType;
    private final boolean _magic;
    private final int _mpConsume;
    private final int _mpInitialConsume;
    private final int _hpConsume;
    private final int _targetConsume;
    private final int _targetConsumeId;
    private final int _itemConsume;
    private final int _itemConsumeId;
    private final int _castRange;
    private final int _effectRange;
    private final int _abnormalLvl;
    private final int _effectAbnormalLvl;
    private final int _coolTime;
    private final int _reuseDelay;
    private final int _equipDelay;
    private final int _buffDuration;
    private final SkillTargetType _targetType;
    private final double _power;
    private final int _magicLevel;
    private final int _negateLvl;
    private final int[] _negateId;
    private final L2SkillType[] _negateStats;
    private final int _maxNegatedEffects;
    private final int _levelDepend;
    private final int _skillRadius;
    private final L2SkillType _skillType;
    private final L2SkillType _effectType;
    private final int _effectId;
    private final int _effectPower;
    private final int _effectLvl;
    private final boolean _ispotion;
    private final byte _element;
    private final boolean _ignoreResists;
    private final boolean _staticReuse;
    private final boolean _staticHitTime;
    private final int _reuseHashCode;
    private final Stats _stat;
    private final int _condition;
    private final int _conditionValue;
    private final boolean _overhit;
    private final boolean _killByDOT;
    private final boolean _isSuicideAttack;
    private final boolean _isSiegeSummonSkill;
    private final int _weaponsAllowed;
    private final boolean _nextActionIsAttack;
    private final int _minPledgeClass;
    private final boolean _isOffensive;
    private final int _maxCharges;
    private final int _numCharges;
    private final int _triggeredId;
    private final int _triggeredLevel;
    private final String _chanceType;
    private final String _flyType;
    private final int _flyRadius;
    private final float _flyCourse;
    private final int _feed;
    private final boolean _isHeroSkill;
    private final int _baseCritRate;
    private final int _lethalEffect1;
    private final int _lethalEffect2;
    private final boolean _directHpDmg;
    private final boolean _isDance;
    private final int _nextDanceCost;
    private final float _sSBoost;
    private final int _aggroPoints;
    private final String _attribute;
    private final boolean _isDebuff;
    private final boolean _stayAfterDeath;
    private final boolean _removedOnAnyActionExceptMove;
    private final boolean _removedOnDamage;
    private final boolean _canBeReflected;
    private final boolean _canBeDispeled;
    private final boolean _isClanSkill;
    private final boolean _ignoreShield;
    private final boolean _simultaneousCast;
    protected ChanceCondition _chanceCondition = null;
    protected List<Condition> _preCondition;
    protected List<Condition> _itemPreCondition;
    protected List<FuncTemplate> _funcTemplates;
    protected List<EffectTemplate> _effectTemplates;
    protected List<EffectTemplate> _effectTemplatesSelf;
    private int _hitTime;
    private L2ExtractableSkill _extractableItems = null;

    protected L2Skill(StatSet set) {
        this._id = set.getInteger("skill_id");
        this._level = set.getInteger("level");
        this._name = set.getString("name");
        this._operateType = set.getEnum("operateType", SkillOpType.class);
        this._magic = set.getBool("isMagic", false);
        this._ispotion = set.getBool("isPotion", false);
        this._mpConsume = set.getInteger("mpConsume", 0);
        this._mpInitialConsume = set.getInteger("mpInitialConsume", 0);
        this._hpConsume = set.getInteger("hpConsume", 0);
        this._targetConsume = set.getInteger("targetConsumeCount", 0);
        this._targetConsumeId = set.getInteger("targetConsumeId", 0);
        this._itemConsume = set.getInteger("itemConsumeCount", 0);
        this._itemConsumeId = set.getInteger("itemConsumeId", 0);
        this._castRange = set.getInteger("castRange", 0);
        this._effectRange = set.getInteger("effectRange", -1);
        this._abnormalLvl = set.getInteger("abnormalLvl", -1);
        this._effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1);
        this._negateLvl = set.getInteger("negateLvl", -1);
        this._hitTime = set.getInteger("hitTime", 0);
        this._coolTime = set.getInteger("coolTime", 0);
        this._reuseDelay = set.getInteger("reuseDelay", 0);
        this._equipDelay = set.getInteger("equipDelay", 0);
        this._buffDuration = set.getInteger("buffDuration", 0);
        this._skillRadius = set.getInteger("skillRadius", 80);
        this._targetType = set.getEnum("target", SkillTargetType.class);
        this._power = set.getFloat("power", 0.0F);
        this._attribute = set.getString("attribute", "");
        String str = set.getString("negateStats", "");
        if (str.isEmpty()) {
            this._negateStats = new L2SkillType[0];
        } else {
            String[] stats = str.split(" ");
            L2SkillType[] array = new L2SkillType[stats.length];

            for (int i = 0; i < stats.length; ++i) {
                L2SkillType type = null;

                try {
                    type = Enum.valueOf(L2SkillType.class, stats[i]);
                } catch (Exception var15) {
                    int var10002 = this._id;
                    throw new IllegalArgumentException("SkillId: " + var10002 + "Enum value of type " + L2SkillType.class.getName() + " required, but found: " + stats[i]);
                }

                array[i] = type;
            }

            this._negateStats = array;
        }

        String negateId = set.getString("negateId", null);
        if (negateId != null) {
            String[] valuesSplit = negateId.split(",");
            this._negateId = new int[valuesSplit.length];

            for (int i = 0; i < valuesSplit.length; ++i) {
                this._negateId[i] = Integer.parseInt(valuesSplit[i]);
            }
        } else {
            this._negateId = new int[0];
        }

        this._maxNegatedEffects = set.getInteger("maxNegated", 0);
        this._magicLevel = set.getInteger("magicLvl", 0);
        this._levelDepend = set.getInteger("lvlDepend", 0);
        this._ignoreResists = set.getBool("ignoreResists", false);
        this._staticReuse = set.getBool("staticReuse", false);
        this._staticHitTime = set.getBool("staticHitTime", false);
        String reuseHash = set.getString("sharedReuse", null);
        if (reuseHash != null) {
            try {
                String[] valuesSplit = reuseHash.split("-");
                this._reuseHashCode = SkillTable.getSkillHashCode(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]));
            } catch (Exception var14) {
                throw new IllegalArgumentException("SkillId: " + this._id + " invalid sharedReuse value: " + reuseHash + ", \"skillId-skillLvl\" required");
            }
        } else {
            this._reuseHashCode = SkillTable.getSkillHashCode(this._id, this._level);
        }

        this._stat = set.getEnum("stat", Stats.class, null);
        this._ignoreShield = set.getBool("ignoreShld", false);
        this._skillType = set.getEnum("skillType", L2SkillType.class);
        this._effectType = set.getEnum("effectType", L2SkillType.class, null);
        this._effectId = set.getInteger("effectId", 0);
        this._effectPower = set.getInteger("effectPower", 0);
        this._effectLvl = set.getInteger("effectLevel", 0);
        this._element = set.getByte("element", (byte) -1);
        this._condition = set.getInteger("condition", 0);
        this._conditionValue = set.getInteger("conditionValue", 0);
        this._overhit = set.getBool("overHit", false);
        this._killByDOT = set.getBool("killByDOT", false);
        this._isSuicideAttack = set.getBool("isSuicideAttack", false);
        this._isSiegeSummonSkill = set.getBool("isSiegeSummonSkill", false);
        String weaponsAllowedString = set.getString("weaponsAllowed", null);
        if (weaponsAllowedString != null) {
            int mask = 0;
            StringTokenizer st = new StringTokenizer(weaponsAllowedString, ",");

            while (st.hasMoreTokens()) {
                int old = mask;
                String item = st.nextToken();

                for (WeaponType wt : WeaponType.values()) {
                    if (wt.name().equals(item)) {
                        mask |= wt.mask();
                        break;
                    }
                }

                for (ArmorType at : ArmorType.values()) {
                    if (at.name().equals(item)) {
                        mask |= at.mask();
                        break;
                    }
                }

                if (old == mask) {
                    _log.info("[weaponsAllowed] Unknown item type name: " + item);
                }
            }

            this._weaponsAllowed = mask;
        } else {
            this._weaponsAllowed = 0;
        }

        this._nextActionIsAttack = set.getBool("nextActionAttack", false);
        this._minPledgeClass = set.getInteger("minPledgeClass", 0);
        this._triggeredId = set.getInteger("triggeredId", 0);
        this._triggeredLevel = set.getInteger("triggeredLevel", 0);
        this._chanceType = set.getString("chanceType", "");
        if (!this._chanceType.isEmpty()) {
            this._chanceCondition = ChanceCondition.parse(set);
        }

        this._isDebuff = set.getBool("isDebuff", false);
        this._isOffensive = set.getBool("offensive", this.isSkillTypeOffensive());
        this._maxCharges = set.getInteger("maxCharges", 0);
        this._numCharges = set.getInteger("numCharges", 0);
        this._isHeroSkill = SkillTable.isHeroSkill(this._id);
        this._baseCritRate = set.getInteger("baseCritRate", this._skillType != L2SkillType.PDAM && this._skillType != L2SkillType.BLOW ? -1 : 0);
        this._lethalEffect1 = set.getInteger("lethal1", 0);
        this._lethalEffect2 = set.getInteger("lethal2", 0);
        this._directHpDmg = set.getBool("dmgDirectlyToHp", false);
        this._isDance = set.getBool("isDance", false);
        this._nextDanceCost = set.getInteger("nextDanceCost", 0);
        this._sSBoost = set.getFloat("SSBoost", 0.0F);
        this._aggroPoints = set.getInteger("aggroPoints", 0);
        this._stayAfterDeath = set.getBool("stayAfterDeath", false);
        this._removedOnAnyActionExceptMove = set.getBool("removedOnAnyActionExceptMove", false);
        this._removedOnDamage = set.getBool("removedOnDamage", this._skillType == L2SkillType.SLEEP);
        this._flyType = set.getString("flyType", null);
        this._flyRadius = set.getInteger("flyRadius", 0);
        this._flyCourse = set.getFloat("flyCourse", 0.0F);
        this._feed = set.getInteger("feed", 0);
        this._canBeReflected = set.getBool("canBeReflected", true);
        this._canBeDispeled = set.getBool("canBeDispeled", true);
        this._isClanSkill = set.getBool("isClanSkill", false);
        this._simultaneousCast = set.getBool("simultaneousCast", false);
        String capsuled_items = set.getString("capsuled_items_skill", null);
        if (capsuled_items != null) {
            if (capsuled_items.isEmpty()) {
                _log.warning("Empty extractable data for skill: " + this._id);
            }

            this._extractableItems = this.parseExtractableSkill(this._id, this._level, capsuled_items);
        }

    }

    public static final boolean checkForAreaOffensiveSkills(Creature caster, Creature target, L2Skill skill, boolean sourceInArena) {
        if (target != null && !target.isDead() && target != caster) {
            Player player = caster.getActingPlayer();
            Player targetPlayer = target.getActingPlayer();
            if (player != null && targetPlayer != null) {
                if (targetPlayer == caster || targetPlayer == player) {
                    return false;
                }

                if (targetPlayer.isInObserverMode()) {
                    return false;
                }

                if (skill.isOffensive() && player.getSiegeState() > 0 && player.isInsideZone(ZoneId.SIEGE) && player.getSiegeState() == targetPlayer.getSiegeState()) {
                    return false;
                }

                if (target.isInsideZone(ZoneId.PEACE)) {
                    return false;
                }

                if (player.isInParty() && targetPlayer.isInParty()) {
                    if (player.getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId()) {
                        return false;
                    }

                    if (player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel()) {
                        return false;
                    }
                }

                if (!sourceInArena && (!targetPlayer.isInsideZone(ZoneId.PVP) || targetPlayer.isInsideZone(ZoneId.SIEGE))) {
                    if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId()) {
                        return false;
                    }

                    if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId()) {
                        return false;
                    }

                    if (!player.checkPvpSkill(targetPlayer, skill)) {
                        return false;
                    }
                }
            } else if (target instanceof Attackable) {
                if (caster instanceof Attackable && !caster.isConfused()) {
                    return false;
                }

                if (skill.isOffensive() && !target.isAutoAttackable(caster)) {
                    return false;
                }
            }

            return GeoEngine.getInstance().canSeeTarget(caster, target);
        } else {
            return false;
        }
    }

    public static final boolean addSummon(Creature caster, Player owner, int radius, boolean isDead) {
        Summon summon = owner.getSummon();
        return summon == null ? false : addCharacter(caster, summon, radius, isDead);
    }

    public static final boolean addCharacter(Creature caster, Creature target, int radius, boolean isDead) {
        if (isDead != target.isDead()) {
            return false;
        } else {
            return radius <= 0 || MathUtil.checkIfInRange(radius, caster, target, true);
        }
    }

    public abstract void useSkill(Creature var1, WorldObject[] var2);

    public final boolean isPotion() {
        return this._ispotion;
    }

    public final int getConditionValue() {
        return this._conditionValue;
    }

    public final L2SkillType getSkillType() {
        return this._skillType;
    }

    public final byte getElement() {
        return this._element;
    }

    public final SkillTargetType getTargetType() {
        return this._targetType;
    }

    public final int getCondition() {
        return this._condition;
    }

    public final boolean isOverhit() {
        return this._overhit;
    }

    public final boolean killByDOT() {
        return this._killByDOT;
    }

    public final boolean isSuicideAttack() {
        return this._isSuicideAttack;
    }

    public final boolean isSiegeSummonSkill() {
        return this._isSiegeSummonSkill;
    }

    public final double getPower(Creature activeChar) {
        double power = this._power;
        if (activeChar == null) {
            return power;
        } else {
            switch (this._skillType) {
                case DEATHLINK:
                    power = this._power * Math.pow(1.7165 - activeChar.getCurrentHp() / (double) activeChar.getMaxHp(), 2.0F) * 0.577;
                case FATAL:
                    power = this._power + this._power * Math.pow(1.7165 - activeChar.getCurrentHp() / (double) activeChar.getMaxHp(), 3.5F) * 0.577;
                default:
                    int targetClassId = activeChar.getTarget() instanceof Player ? activeChar.getTarget().getActingPlayer().getClassId().getId() : -1;
                    SkillBalanceManager var10001 = SkillBalanceManager.getInstance();
                    int var10002 = this.getId();
                    return power * var10001.getSkillValue(var10002 + ";" + targetClassId, SkillChangeType.Power, activeChar.getTarget() instanceof Player ? activeChar.getTarget().getActingPlayer() : null);
            }
        }
    }

    public final double getPower() {
        return this._power;
    }

    public final L2SkillType[] getNegateStats() {
        return this._negateStats;
    }

    public final int getAbnormalLvl() {
        return this._abnormalLvl;
    }

    public final int getNegateLvl() {
        return this._negateLvl;
    }

    public final int[] getNegateId() {
        return this._negateId;
    }

    public final int getMagicLevel() {
        return this._magicLevel;
    }

    public final int getMaxNegatedEffects() {
        return this._maxNegatedEffects;
    }

    public final int getLevelDepend() {
        return this._levelDepend;
    }

    public final boolean ignoreResists() {
        return this._ignoreResists;
    }

    public int getTriggeredId() {
        return this._triggeredId;
    }

    public int getTriggeredLevel() {
        return this._triggeredLevel;
    }

    public boolean triggerAnotherSkill() {
        return this._triggeredId > 1;
    }

    public final boolean isRemovedOnAnyActionExceptMove() {
        return this._removedOnAnyActionExceptMove;
    }

    public final boolean isRemovedOnDamage() {
        return this._removedOnDamage;
    }

    public final double getEffectPower() {
        if (this._effectTemplates != null) {
            for (EffectTemplate et : this._effectTemplates) {
                if (et.effectPower > (double) 0.0F) {
                    return et.effectPower;
                }
            }
        }

        if (this._effectPower > 0) {
            return this._effectPower;
        } else {
            switch (this._skillType) {
                case PDAM:
                case MDAM:
                    return 20.0F;
                default:
                    return !(this._power <= (double) 0.0F) && !((double) 100.0F < this._power) ? this._power : (double) 20.0F;
            }
        }
    }

    public final int getEffectId() {
        return this._effectId;
    }

    public final int getEffectLvl() {
        return this._effectLvl;
    }

    public final int getEffectAbnormalLvl() {
        return this._effectAbnormalLvl;
    }

    public final L2SkillType getEffectType() {
        if (this._effectTemplates != null) {
            for (EffectTemplate et : this._effectTemplates) {
                if (et.effectType != null) {
                    return et.effectType;
                }
            }
        }

        if (this._effectType != null) {
            return this._effectType;
        } else {
            switch (this._skillType) {
                case PDAM -> {
                    return L2SkillType.STUN;
                }
                case MDAM -> {
                    return L2SkillType.PARALYZE;
                }
                default -> {
                    return this._skillType;
                }
            }
        }
    }

    public final boolean nextActionIsAttack() {
        return this._nextActionIsAttack;
    }

    public final int getBuffDuration() {
        return this._buffDuration;
    }

    public final int getCastRange() {
        return this._castRange;
    }

    public final int getEffectRange() {
        return this._effectRange;
    }

    public final int getHpConsume() {
        return this._hpConsume;
    }

    public final boolean isDebuff() {
        return this._isDebuff;
    }

    public final int getId() {
        return this._id;
    }

    public final Stats getStat() {
        return this._stat;
    }

    public final int getTargetConsumeId() {
        return this._targetConsumeId;
    }

    public final int getTargetConsume() {
        return this._targetConsume;
    }

    public final int getItemConsume() {
        return this._itemConsume;
    }

    public final int getItemConsumeId() {
        return this._itemConsumeId;
    }

    public final int getLevel() {
        return this._level;
    }

    public final boolean isMagic() {
        return this._magic;
    }

    public final boolean isStaticReuse() {
        return this._staticReuse;
    }

    public final boolean isStaticHitTime() {
        return this._staticHitTime;
    }

    public final int getMpConsume() {
        return this._mpConsume;
    }

    public final int getMpInitialConsume() {
        return this._mpInitialConsume;
    }

    public final String getName() {
        return this._name;
    }

    public final int getReuseDelay() {
        return (int) ((double) this._reuseDelay * SkillBalanceManager.getInstance().getSkillValue(this.getId() + ";-2", SkillChangeType.Reuse, null));
    }

    public final int getEquipDelay() {
        return this._equipDelay;
    }

    public final int getReuseHashCode() {
        return this._reuseHashCode;
    }

    public final int getHitTime() {
        return (int) ((double) this._hitTime * SkillBalanceManager.getInstance().getSkillValue(this.getId() + ";-2", SkillChangeType.CastTime, null));
    }

    public void setHitTime(int value) {
        this._hitTime = value;
    }

    public final int getCoolTime() {
        return this._coolTime;
    }

    public final int getSkillRadius() {
        return this._skillRadius;
    }

    public final boolean isActive() {
        return this._operateType == L2Skill.SkillOpType.OP_ACTIVE;
    }

    public final boolean isPassive() {
        return this._operateType == L2Skill.SkillOpType.OP_PASSIVE;
    }

    public final boolean isToggle() {
        return this._operateType == L2Skill.SkillOpType.OP_TOGGLE;
    }

    public boolean isChance() {
        return this._chanceCondition != null && this.isPassive();
    }

    public final boolean isDance() {
        return this._isDance;
    }

    public final int getNextDanceMpCost() {
        return this._nextDanceCost;
    }

    public final float getSSBoost() {
        return this._sSBoost;
    }

    public final int getAggroPoints() {
        return this._aggroPoints;
    }

    public final boolean useSoulShot() {
        switch (this._skillType) {
            case PDAM:
            case BLOW:
            case STUN:
            case CHARGEDAM:
                return true;
            case MDAM:
            default:
                return false;
        }
    }

    public final boolean useSpiritShot() {
        return this.isMagic();
    }

    public final int getWeaponsAllowed() {
        return this._weaponsAllowed;
    }

    public boolean isSimultaneousCast() {
        return this._simultaneousCast;
    }

    public int getMinPledgeClass() {
        return this._minPledgeClass;
    }

    public String getAttributeName() {
        return this._attribute;
    }

    public boolean ignoreShield() {
        return this._ignoreShield;
    }

    public boolean canBeReflected() {
        return this._canBeReflected;
    }

    public boolean canBeDispeled() {
        return this._canBeDispeled;
    }

    public boolean isClanSkill() {
        return this._isClanSkill;
    }

    public final String getFlyType() {
        return this._flyType;
    }

    public final int getFlyRadius() {
        return this._flyRadius;
    }

    public int getFeed() {
        return this._feed;
    }

    public final float getFlyCourse() {
        return this._flyCourse;
    }

    public final int getMaxCharges() {
        return this._maxCharges;
    }

    public boolean triggersChanceSkill() {
        return this._triggeredId > 0 && this.isChance();
    }

    public int getTriggeredChanceId() {
        return this._triggeredId;
    }

    public int getTriggeredChanceLevel() {
        return this._triggeredLevel;
    }

    public ChanceCondition getTriggeredChanceCondition() {
        return this._chanceCondition;
    }

    public final boolean isPvpSkill() {
        switch (this._skillType) {
            case STUN:
            case DOT:
            case BLEED:
            case POISON:
            case DEBUFF:
            case AGGDEBUFF:
            case ROOT:
            case FEAR:
            case SLEEP:
            case MDOT:
            case MUTE:
            case WEAKNESS:
            case PARALYZE:
            case CANCEL:
            case MAGE_BANE:
            case WARRIOR_BANE:
            case BETRAY:
            case AGGDAMAGE:
            case AGGREDUCE_CHAR:
            case MANADAM:
                return true;
            case CHARGEDAM:
            default:
                return false;
        }
    }

    public final boolean is7Signs() {
        return this._id > 4360 && this._id < 4367;
    }

    public final boolean isStayAfterDeath() {
        return this._stayAfterDeath;
    }

    public final boolean isOffensive() {
        return this._isOffensive;
    }

    public final boolean isHeroSkill() {
        return this._isHeroSkill;
    }

    public final int getNumCharges() {
        return this._numCharges;
    }

    public final int getBaseCritRate() {
        return this._baseCritRate;
    }

    public final int getLethalChance1() {
        return this._lethalEffect1;
    }

    public final int getLethalChance2() {
        return this._lethalEffect2;
    }

    public final boolean getDmgDirectlyToHP() {
        return this._directHpDmg;
    }

    public final boolean isSkillTypeOffensive() {
        switch (this._skillType) {
            case DEATHLINK:
            case FATAL:
            case PDAM:
            case MDAM:
            case BLOW:
            case STUN:
            case CHARGEDAM:
            case DOT:
            case BLEED:
            case POISON:
            case DEBUFF:
            case AGGDEBUFF:
            case ROOT:
            case FEAR:
            case SLEEP:
            case MDOT:
            case MUTE:
            case WEAKNESS:
            case PARALYZE:
            case CANCEL:
            case MAGE_BANE:
            case WARRIOR_BANE:
            case BETRAY:
            case AGGDAMAGE:
            case AGGREDUCE_CHAR:
            case MANADAM:
            case CPDAMPERCENT:
            case CONFUSION:
            case ERASE:
            case DRAIN:
            case DETECT_WEAKNESS:
            case SOULSHOT:
            case SPIRITSHOT:
            case SPOIL:
            case SWEEP:
            case DRAIN_SOUL:
            case AGGREDUCE:
            case AGGREMOVE:
            case DELUXE_KEY_UNLOCK:
            case SOW:
            case HARVEST:
            case INSTANT_JUMP:
                return true;
            default:
                return this.isDebuff();
        }
    }

    public final boolean getWeaponDependancy(Creature activeChar) {
        int weaponsAllowed = this.getWeaponsAllowed();
        if (weaponsAllowed == 0) {
            return true;
        } else {
            int mask = 0;
            Weapon weapon = activeChar.getActiveWeaponItem();
            if (weapon != null) {
                mask |= weapon.getItemType().mask();
            }

            Item shield = activeChar.getSecondaryWeaponItem();
            if (shield != null && shield instanceof Armor) {
                mask |= shield.getItemType().mask();
            }

            if ((mask & weaponsAllowed) != 0) {
                return true;
            } else {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(this));
                return false;
            }
        }
    }

    public boolean checkCondition(Creature activeChar, WorldObject target, boolean itemOrWeapon) {
        List<Condition> preCondition = itemOrWeapon ? this._itemPreCondition : this._preCondition;
        if (preCondition != null && !preCondition.isEmpty()) {
            Env env = new Env();
            env.setCharacter(activeChar);
            if (target instanceof Creature) {
                env.setTarget((Creature) target);
            }

            env.setSkill(this);

            for (Condition cond : preCondition) {
                if (!cond.test(env)) {
                    int msgId = cond.getMessageId();
                    if (msgId != 0) {
                        SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                        if (cond.isAddName()) {
                            sm.addSkillName(this._id);
                        }

                        activeChar.sendPacket(sm);
                    } else {
                        String msg = cond.getMessage();
                        if (msg != null) {
                            activeChar.sendMessage(msg);
                        }
                    }

                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public final WorldObject[] getTargetList(Creature activeChar, boolean onlyFirst) {
        WorldObject objTarget = activeChar.getTarget();
        if (objTarget instanceof Creature target) {
            return this.getTargetList(activeChar, onlyFirst, target);
        }

        return new WorldObject[]{};
    }

    public final WorldObject[] getTargetList(Creature activeChar, boolean onlyFirst, Creature target) {
        switch (this._targetType.ordinal()) {
            case 1:
            case 29:
                return new Creature[]{activeChar};
            case 2:
                boolean canTargetSelf = false;
                switch (this._skillType) {
                    case BUFF:
                    case HEAL:
                    case HOT:
                    case HEAL_PERCENT:
                    case MANARECHARGE:
                    case MANAHEAL:
                    case NEGATE:
                    case CANCEL_DEBUFF:
                    case REFLECT:
                    case COMBATPOINTHEAL:
                    case SEED:
                    case BALANCE_LIFE:
                        canTargetSelf = true;
                }

                if (target != null && !target.isDead() && (target != activeChar || canTargetSelf)) {
                    return new Creature[]{target};
                }

                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                return _emptyTargetList;
            case 3:
                if (onlyFirst) {
                    return new Creature[]{activeChar};
                }

                List<Creature> targetList = new ArrayList<>();
                targetList.add(activeChar);
                int radius = this._skillRadius;
                Player player = activeChar.getActingPlayer();
                if (activeChar instanceof Summon) {
                    if (addCharacter(activeChar, player, radius, false)) {
                        targetList.add(player);
                    }
                } else if (activeChar instanceof Player && addSummon(activeChar, player, radius, false)) {
                    targetList.add(player.getSummon());
                }

                Party party = activeChar.getParty();
                if (party != null) {
                    for (Player partyMember : party.getMembers()) {
                        if (partyMember != player) {
                            if (addCharacter(activeChar, partyMember, radius, false)) {
                                targetList.add(partyMember);
                            }

                            if (addSummon(activeChar, partyMember, radius, false)) {
                                targetList.add(partyMember.getSummon());
                            }
                        }
                    }
                }

                return targetList.toArray(new Creature[targetList.size()]);
            case 4:
                player = activeChar.getActingPlayer();
                if (player == null) {
                    return _emptyTargetList;
                } else {
                    if (!onlyFirst && !player.isInOlympiadMode()) {
                        targetList = new ArrayList<>();
                        targetList.add(player);
                        radius = this._skillRadius;
                        if (addSummon(activeChar, player, radius, false)) {
                            targetList.add(player.getSummon());
                        }

                        if (player.getClan() != null) {
                            for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius)) {
                                if ((obj.getAllyId() != 0 && obj.getAllyId() == player.getAllyId() || obj.getClan() != null && obj.getClanId() == player.getClanId()) && (!player.isInDuel() || player.getDuelId() == obj.getDuelId() && (!player.isInParty() || !obj.isInParty() || player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId())) && player.checkPvpSkill(obj, this)) {
                                    Summon summon = obj.getSummon();
                                    if (summon != null && !summon.isDead()) {
                                        targetList.add(summon);
                                    }

                                    if (!obj.isDead()) {
                                        targetList.add(obj);
                                    }
                                }
                            }
                        }

                        return targetList.toArray(new Creature[targetList.size()]);
                    }

                    return new Creature[]{activeChar};
                }
            case 5:
                targetList = new ArrayList<>();
                if (activeChar instanceof Playable) {
                    player = activeChar.getActingPlayer();
                    if (player == null) {
                        return _emptyTargetList;
                    }

                    if (onlyFirst || player.isInOlympiadMode()) {
                        return new Creature[]{activeChar};
                    }

                    targetList.add(player);
                    radius = this._skillRadius;
                    if (addSummon(activeChar, player, radius, false)) {
                        targetList.add(player.getSummon());
                    }

                    Clan clan = player.getClan();
                    if (clan != null) {
                        for (ClanMember member : clan.getMembers()) {
                            Player obj = member.getPlayerInstance();
                            if (obj != null && obj != player && (!player.isInDuel() || player.getDuelId() == obj.getDuelId() && (!player.isInParty() || !obj.isInParty() || player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId())) && player.checkPvpSkill(obj, this)) {
                                if (addSummon(activeChar, obj, radius, false)) {
                                    targetList.add(obj.getSummon());
                                }

                                if (addCharacter(activeChar, obj, radius, false)) {
                                    targetList.add(obj);
                                }
                            }
                        }
                    }
                } else if (activeChar instanceof Npc) {
                    targetList.add(activeChar);

                    for (Npc newTarget : activeChar.getKnownTypeInRadius(Npc.class, this._castRange)) {
                        if (!newTarget.isDead() && ArraysUtil.contains(((Npc) activeChar).getTemplate().getClans(), newTarget.getTemplate().getClans())) {
                            targetList.add(newTarget);
                        }
                    }
                }

                return targetList.toArray(new Creature[targetList.size()]);
            case 6:
                Creature var15 = activeChar.getSummon();
                if (var15 != null && !var15.isDead()) {
                    return new Creature[]{var15};
                }

                return _emptyTargetList;
            case 7:
            case 8:
            case 9:
                if ((target != null && target != activeChar && !target.isAlikeDead() || this._castRange < 0) && (target instanceof Attackable || target instanceof Playable)) {
                    boolean srcInArena = activeChar.isInArena();
                    targetList = new ArrayList<>();
                    Creature origin;
                    if (this._castRange >= 0) {
                        if (!checkForAreaOffensiveSkills(activeChar, target, this, srcInArena)) {
                            return _emptyTargetList;
                        }

                        if (onlyFirst) {
                            return new Creature[]{target};
                        }

                        origin = target;
                        targetList.add(target);
                    } else {
                        origin = activeChar;
                    }

                    for (Creature obj : activeChar.getKnownType(Creature.class)) {
                        if ((obj instanceof Attackable || obj instanceof Playable) && obj != origin && MathUtil.checkIfInRange(this._skillRadius, origin, obj, true)) {
                            switch (this._targetType.ordinal()) {
                                case 8:
                                    if (!obj.isInFrontOf(activeChar)) {
                                        continue;
                                    }
                                    break;
                                case 9:
                                    if (!obj.isBehind(activeChar)) {
                                        continue;
                                    }
                            }

                            if (checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena)) {
                                targetList.add(obj);
                            }
                        }
                    }

                    if (targetList.isEmpty()) {
                        return _emptyTargetList;
                    }

                    return targetList.toArray(new Creature[targetList.size()]);
                }

                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                return _emptyTargetList;
            case 10:
            case 11:
            case 12:
                targetList = new ArrayList<>();
                if (this._skillType == L2SkillType.DUMMY) {
                    if (onlyFirst) {
                        return new Creature[]{activeChar};
                    }

                    Player sourcePlayer = activeChar.getActingPlayer();
                    targetList.add(activeChar);

                    for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, this._skillRadius)) {
                        if (obj == activeChar || obj == sourcePlayer || obj instanceof Npc || obj instanceof Attackable) {
                            targetList.add(obj);
                        }
                    }
                } else {
                    boolean srcInArena = activeChar.isInArena();

                    for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, this._skillRadius)) {
                        if (obj instanceof Attackable || obj instanceof Playable) {
                            switch (this._targetType.ordinal()) {
                                case 11:
                                    if (!obj.isInFrontOf(activeChar)) {
                                        continue;
                                    }
                                    break;
                                case 12:
                                    if (!obj.isBehind(activeChar)) {
                                        continue;
                                    }
                            }

                            if (checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena)) {
                                if (onlyFirst) {
                                    return new Creature[]{obj};
                                }

                                targetList.add(obj);
                            }
                        }
                    }
                }

                return targetList.toArray(new Creature[targetList.size()]);
            case 13:
            default:
                activeChar.sendMessage("Target type of skill is not currently handled");
                return _emptyTargetList;
            case 14:
                if (!(target instanceof Npc) && !(target instanceof Servitor)) {
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                    return _emptyTargetList;
                } else {
                    if (target.isUndead() && !target.isDead()) {
                        return new Creature[]{target};
                    }

                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                    return _emptyTargetList;
                }
            case 15:
                targetList = new ArrayList<>();

                for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, this._skillRadius)) {
                    if ((obj instanceof Npc || obj instanceof Servitor) && !obj.isAlikeDead() && obj.isUndead() && GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
                        if (onlyFirst) {
                            return new Creature[]{obj};
                        }

                        targetList.add(obj);
                    }
                }

                if (targetList.isEmpty()) {
                    return _emptyTargetList;
                }

                return targetList.toArray(new Creature[targetList.size()]);
            case 16:
                player = activeChar.getActingPlayer();
                if (player == null) {
                    return _emptyTargetList;
                } else {
                    if (!onlyFirst && !player.isInOlympiadMode()) {
                        radius = this._skillRadius;
                        targetList = new ArrayList<>();
                        targetList.add(activeChar);
                        if (player.getClan() != null) {
                            boolean isInBossZone = player.isInsideZone(ZoneId.BOSS);

                            for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius)) {
                                if (obj.isDead() && (obj.getAllyId() != 0 && obj.getAllyId() == player.getAllyId() || obj.getClan() != null && obj.getClanId() == player.getClanId()) && (!player.isInDuel() || player.getDuelId() == obj.getDuelId() && (!player.isInParty() || !obj.isInParty() || player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId())) && (!obj.isInsideZone(ZoneId.SIEGE) || obj.isInSiege()) && isInBossZone == obj.isInsideZone(ZoneId.BOSS)) {
                                    targetList.add(obj);
                                }
                            }
                        }

                        return targetList.toArray(new Creature[targetList.size()]);
                    }

                    return new Creature[]{activeChar};
                }
            case 17:
                if (!(activeChar instanceof Player)) {
                    return _emptyTargetList;
                } else {
                    if (target != null && target.isDead()) {
                        Player targetPlayer;
                        if (target instanceof Player) {
                            targetPlayer = (Player) target;
                        } else {
                            targetPlayer = null;
                        }

                        Pet targetPet;
                        if (target instanceof Pet) {
                            targetPet = (Pet) target;
                        } else {
                            targetPet = null;
                        }

                        if (targetPlayer != null || targetPet != null) {
                            boolean condGood = true;
                            if (this._skillType == L2SkillType.RESURRECT) {
                                player = (Player) activeChar;
                                if (targetPlayer != null) {
                                    if (targetPlayer.isInsideZone(ZoneId.SIEGE) && !targetPlayer.isInSiege()) {
                                        condGood = false;
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
                                    }

                                    if (targetPlayer.isFestivalParticipant()) {
                                        condGood = false;
                                        activeChar.sendMessage("You may not resurrect participants in a festival.");
                                    }

                                    if (targetPlayer.isReviveRequested()) {
                                        if (targetPlayer.isRevivingPet()) {
                                            player.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
                                        } else {
                                            player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
                                        }

                                        condGood = false;
                                    }
                                } else if (targetPet != null && targetPet.getOwner() != player && targetPet.getOwner().isReviveRequested()) {
                                    if (targetPet.getOwner().isRevivingPet()) {
                                        player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
                                    } else {
                                        player.sendPacket(SystemMessageId.CANNOT_RES_PET2);
                                    }

                                    condGood = false;
                                }
                            }

                            if (condGood) {
                                return new Creature[]{target};
                            }
                        }
                    }

                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                    return _emptyTargetList;
                }
            case 18:
                if (activeChar instanceof Player) {
                    Creature var14 = activeChar.getSummon();
                    if (var14 != null && var14.isDead()) {
                        return new Creature[]{var14};
                    }
                }

                return _emptyTargetList;
            case 19:
                targetList = new ArrayList<>();
                targetList.add(target);

                for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, this._skillRadius)) {
                    if (obj != activeChar && GeoEngine.getInstance().canSeeTarget(target, obj)) {
                        if (this.getId() == 444) {
                            if (obj instanceof Attackable && obj.isDead()) {
                                targetList.add(obj);
                            }
                        } else if (!obj.isDead() && (obj instanceof Attackable || obj instanceof Playable)) {
                            boolean srcInArena = activeChar.isInArena();
                            if (checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena)) {
                                targetList.add(obj);
                            }
                        }
                    }
                }

                if (targetList.isEmpty()) {
                    return _emptyTargetList;
                }

                return targetList.toArray(new Creature[targetList.size()]);
            case 20:
                if (target instanceof Monster && target.isDead()) {
                    if (this._skillType == L2SkillType.DRAIN && !DecayTaskManager.getInstance().isCorpseActionAllowed((Monster) target)) {
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED));
                        return _emptyTargetList;
                    }

                    return new Creature[]{target};
                }

                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                return _emptyTargetList;
            case 21:
                if (!(target instanceof Door) && !(target instanceof Chest)) {
                    return _emptyTargetList;
                }

                return new Creature[]{target};
            case 22:
                if (!(target instanceof HolyThing)) {
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                    return _emptyTargetList;
                }

                return new Creature[]{target};
            case 23:
                if (target != null && (target == activeChar || activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId() || activeChar instanceof Player && target instanceof Summon && activeChar.getSummon() == target || activeChar instanceof Summon && target instanceof Player && activeChar == target.getSummon())) {
                    if (!target.isDead()) {
                        return new Creature[]{target};
                    }

                    return _emptyTargetList;
                }

                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                return _emptyTargetList;
            case 24:
                if (target != null && target != activeChar && activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()) {
                    if (!target.isDead()) {
                        if (target instanceof Player) {
                            switch (this.getId()) {
                                case 426:
                                    if (!((Player) target).isMageClass()) {
                                        return new Creature[]{target};
                                    }

                                    return _emptyTargetList;
                                case 427:
                                    if (((Player) target).isMageClass()) {
                                        return new Creature[]{target};
                                    }

                                    return _emptyTargetList;
                            }
                        }

                        return new Creature[]{target};
                    }

                    return _emptyTargetList;
                }

                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                return _emptyTargetList;
            case 25:
                Creature var13 = activeChar.getSummon();
                if (var13 != null && !var13.isDead() && var13 instanceof Servitor) {
                    return new Creature[]{var13};
                }

                return _emptyTargetList;
            case 26:
                Creature var12 = activeChar.getSummon();
                if (var12 != null && var12 instanceof Servitor && !var12.isDead()) {
                    if (onlyFirst) {
                        return new Creature[]{var12};
                    }

                    boolean srcInArena = activeChar.isInArena();
                    targetList = new ArrayList<>();

                    for (Creature obj : var12.getKnownType(Creature.class)) {
                        if (obj != null && obj != var12 && obj != activeChar && MathUtil.checkIfInRange(this._skillRadius, var12, obj, true) && (obj instanceof Attackable || obj instanceof Playable) && checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena)) {
                            targetList.add(obj);
                        }
                    }

                    if (targetList.isEmpty()) {
                        return _emptyTargetList;
                    }

                    return targetList.toArray(new Creature[targetList.size()]);
                }

                return _emptyTargetList;
            case 27:
                if (target instanceof Summon targetSummon) {
                    Player summonOwner = targetSummon.getActingPlayer();
                    if (activeChar instanceof Player && activeChar.getSummon() != targetSummon && !targetSummon.isDead() && (summonOwner.getPvpFlag() != 0 || summonOwner.getKarma() > 0) || summonOwner.isInsideZone(ZoneId.PVP) && activeChar.isInsideZone(ZoneId.PVP) || summonOwner.isInDuel() && ((Player) activeChar).isInDuel() && summonOwner.getDuelId() == ((Player) activeChar).getDuelId()) {
                        return new Creature[]{targetSummon};
                    }
                }

                return _emptyTargetList;
            case 28:
                if (activeChar instanceof Summon) {
                    target = activeChar.getActingPlayer();
                    if (target != null && !target.isDead()) {
                        return new Creature[]{target};
                    }
                }

                return _emptyTargetList;
        }
    }

    public final WorldObject[] getTargetList(Creature activeChar) {
        return this.getTargetList(activeChar, false);
    }

    public final WorldObject getFirstOfTargetList(Creature activeChar) {
        WorldObject[] targets = this.getTargetList(activeChar, true);
        return targets.length == 0 ? null : targets[0];
    }

    public final List<Func> getStatFuncs(Creature player) {
        if (this._funcTemplates == null) {
            return Collections.emptyList();
        } else if (!(player instanceof Playable) && !(player instanceof Attackable)) {
            return Collections.emptyList();
        } else {
            List<Func> funcs = new ArrayList<>(this._funcTemplates.size());
            Env env = new Env();
            env.setCharacter(player);
            env.setSkill(this);

            for (FuncTemplate t : this._funcTemplates) {
                Func f = t.getFunc(env, this);
                if (f != null) {
                    funcs.add(f);
                }
            }

            return funcs;
        }
    }

    public boolean hasEffects() {
        return this._effectTemplates != null && !this._effectTemplates.isEmpty();
    }

    public List<EffectTemplate> getEffectTemplates() {
        return this._effectTemplates;
    }

    public boolean hasSelfEffects() {
        return this._effectTemplatesSelf != null && !this._effectTemplatesSelf.isEmpty();
    }

    public final List<L2Effect> getEffects(Creature effector, Creature effected, Env env) {
        if (this.hasEffects() && !this.isPassive()) {
            if (!(effected instanceof Door) && !(effected instanceof SiegeFlag)) {
                if (effector != effected && (this.isOffensive() || this.isDebuff())) {
                    if (effected.isInvul()) {
                        return Collections.emptyList();
                    }

                    if (effector instanceof Player && effector.isGM() && !((Player) effector).getAccessLevel().canGiveDamage()) {
                        return Collections.emptyList();
                    }
                }

                List<L2Effect> effects = new ArrayList<>(this._effectTemplates.size());
                if (env == null) {
                    env = new Env();
                }

                env.setSkillMastery(Formulas.calcSkillMastery(effector, this));
                env.setCharacter(effector);
                env.setTarget(effected);
                env.setSkill(this);

                for (EffectTemplate et : this._effectTemplates) {
                    boolean success = true;
                    if (et.effectPower > (double) -1.0F) {
                        success = Formulas.calcEffectSuccess(effector, effected, et, this, env.getShield(), env.isBlessedSpiritShot());
                    }

                    if (success) {
                        L2Effect e = et.getEffect(env);
                        if (e != null) {
                            e.scheduleEffect();
                            effects.add(e);
                        }
                    } else if (et.icon && effector instanceof Player) {
                        effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(effected).addSkillName(this));
                    }
                }

                return effects;
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public final List<L2Effect> getEffects(Creature effector, Creature effected) {
        return this.getEffects(effector, effected, null);
    }

    public final List<L2Effect> getEffects(Cubic effector, Creature effected, Env env) {
        if (this.hasEffects() && !this.isPassive()) {
            if (effector.getOwner() != effected && (this.isDebuff() || this.isOffensive())) {
                if (effected.isInvul()) {
                    return Collections.emptyList();
                }

                if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage()) {
                    return Collections.emptyList();
                }
            }

            List<L2Effect> effects = new ArrayList<>(this._effectTemplates.size());
            if (env == null) {
                env = new Env();
            }

            env.setCharacter(effector.getOwner());
            env.setCubic(effector);
            env.setTarget(effected);
            env.setSkill(this);

            for (EffectTemplate et : this._effectTemplates) {
                boolean success = true;
                if (et.effectPower > (double) -1.0F) {
                    success = Formulas.calcEffectSuccess(effector.getOwner(), effected, et, this, env.getShield(), env.isBlessedSpiritShot());
                }

                if (success) {
                    L2Effect e = et.getEffect(env);
                    if (e != null) {
                        e.scheduleEffect();
                        effects.add(e);
                    }
                }
            }

            return effects;
        } else {
            return Collections.emptyList();
        }
    }

    public final List<L2Effect> getEffectsSelf(Creature effector) {
        if (this.hasSelfEffects() && !this.isPassive()) {
            List<L2Effect> effects = new ArrayList<>(this._effectTemplatesSelf.size());
            Env env = new Env();
            env.setCharacter(effector);
            env.setTarget(effector);
            env.setSkill(this);

            for (EffectTemplate et : this._effectTemplatesSelf) {
                L2Effect e = et.getEffect(env);
                if (e != null) {
                    e.setSelfEffect();
                    e.scheduleEffect();
                    effects.add(e);
                }
            }

            return effects;
        } else {
            return Collections.emptyList();
        }
    }

    public final void attach(FuncTemplate f) {
        if (this._funcTemplates == null) {
            this._funcTemplates = new ArrayList<>(1);
        }

        this._funcTemplates.add(f);
    }

    public final void attach(EffectTemplate effect) {
        if (this._effectTemplates == null) {
            this._effectTemplates = new ArrayList<>(1);
        }

        this._effectTemplates.add(effect);
    }

    public final void attachSelf(EffectTemplate effect) {
        if (this._effectTemplatesSelf == null) {
            this._effectTemplatesSelf = new ArrayList<>(1);
        }

        this._effectTemplatesSelf.add(effect);
    }

    public final void attach(Condition c, boolean itemOrWeapon) {
        if (itemOrWeapon) {
            if (this._itemPreCondition == null) {
                this._itemPreCondition = new ArrayList<>();
            }

            this._itemPreCondition.add(c);
        } else {
            if (this._preCondition == null) {
                this._preCondition = new ArrayList<>();
            }

            this._preCondition.add(c);
        }

    }

    private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values) {
        String[] prodLists = values.split(";");
        List<L2ExtractableProductItem> products = new ArrayList<>();

        for (String prodList : prodLists) {
            String[] prodData = prodList.split(",");
            if (prodData.length < 3) {
                _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
            }

            int lenght = prodData.length - 1;
            List<IntIntHolder> items = null;
            double chance = 0.0F;
            int prodId = 0;
            int quantity = 0;

            try {
                items = new ArrayList<>(lenght / 2);

                for (int j = 0; j < lenght; ++j) {
                    prodId = Integer.parseInt(prodData[j]);
                    ++j;
                    quantity = Integer.parseInt(prodData[j]);
                    if (prodId <= 0 || quantity <= 0) {
                        _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
                    }

                    items.add(new IntIntHolder(prodId, quantity));
                }

                chance = Double.parseDouble(prodData[lenght]);
            } catch (Exception var18) {
                _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
            }

            products.add(new L2ExtractableProductItem(items, chance));
        }

        if (products.isEmpty()) {
            _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
        }

        return new L2ExtractableSkill(SkillTable.getSkillHashCode(this), products);
    }

    public L2ExtractableSkill getExtractableSkill() {
        return this._extractableItems;
    }

    public boolean isDamage() {
        switch (this._skillType) {
            case FATAL:
            case PDAM:
            case MDAM:
            case BLOW:
            case CPDAMPERCENT:
            case DRAIN:
                return true;
            default:
                return false;
        }
    }

    public boolean isAOE() {
        switch (this._targetType.ordinal()) {
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return true;
            default:
                return false;
        }
    }

    public String toString() {
        return this._name + "[id=" + this._id + ",lvl=" + this._level + "]";
    }

    public static enum SkillOpType {
        OP_PASSIVE,
        OP_ACTIVE,
        OP_TOGGLE;
    }

    public static enum SkillTargetType {
        TARGET_NONE,
        TARGET_SELF,
        TARGET_ONE,
        TARGET_PARTY,
        TARGET_ALLY,
        TARGET_CLAN,
        TARGET_PET,
        TARGET_AREA,
        TARGET_FRONT_AREA,
        TARGET_BEHIND_AREA,
        TARGET_AURA,
        TARGET_FRONT_AURA,
        TARGET_BEHIND_AURA,
        TARGET_CORPSE,
        TARGET_UNDEAD,
        TARGET_AURA_UNDEAD,
        TARGET_CORPSE_ALLY,
        TARGET_CORPSE_PLAYER,
        TARGET_CORPSE_PET,
        TARGET_AREA_CORPSE_MOB,
        TARGET_CORPSE_MOB,
        TARGET_UNLOCKABLE,
        TARGET_HOLY,
        TARGET_PARTY_MEMBER,
        TARGET_PARTY_OTHER,
        TARGET_SUMMON,
        TARGET_AREA_SUMMON,
        TARGET_ENEMY_SUMMON,
        TARGET_OWNER_PET,
        TARGET_GROUND;
    }
}
