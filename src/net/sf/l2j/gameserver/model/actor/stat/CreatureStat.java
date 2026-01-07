package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;

public class CreatureStat {
    private final Creature _activeChar;

    private long _exp = 0L;

    private int _sp = 0;

    private byte _level = 1;

    public CreatureStat(Creature activeChar) {
        this._activeChar = activeChar;
    }

    public final double calcStat(Stats stat, double init, Creature target, L2Skill skill) {
        if (this._activeChar == null || stat == null)
            return init;
        int id = stat.ordinal();
        Calculator c = this._activeChar.getCalculators()[id];
        if (c == null || c.size() == 0)
            return init;
        Env env = new Env();
        env.setCharacter(this._activeChar);
        env.setTarget(target);
        env.setSkill(skill);
        env.setValue(init);
        c.calc(env);
        if (env.getValue() <= 0.0D)
            switch (stat) {
                case MAX_HP:
                case MAX_MP:
                case MAX_CP:
                case MAGIC_DEFENCE:
                case POWER_DEFENCE:
                case POWER_ATTACK:
                case MAGIC_ATTACK:
                case POWER_ATTACK_SPEED:
                case MAGIC_ATTACK_SPEED:
                case SHIELD_DEFENCE:
                case STAT_CON:
                case STAT_DEX:
                case STAT_INT:
                case STAT_MEN:
                case STAT_STR:
                case STAT_WIT:
                    env.setValue(1.0D);
                    break;
            }
        return env.getValue();
    }

    public int getSTR() {
        return (int) calcStat(Stats.STAT_STR, this._activeChar.getTemplate().getBaseSTR(), null, null);
    }

    public int getDEX() {
        return (int) calcStat(Stats.STAT_DEX, this._activeChar.getTemplate().getBaseDEX(), null, null);
    }

    public int getCON() {
        return (int) calcStat(Stats.STAT_CON, this._activeChar.getTemplate().getBaseCON(), null, null);
    }

    public int getINT() {
        return (int) calcStat(Stats.STAT_INT, this._activeChar.getTemplate().getBaseINT(), null, null);
    }

    public int getMEN() {
        return (int) calcStat(Stats.STAT_MEN, this._activeChar.getTemplate().getBaseMEN(), null, null);
    }

    public int getWIT() {
        return (int) calcStat(Stats.STAT_WIT, this._activeChar.getTemplate().getBaseWIT(), null, null);
    }

    public int getCriticalHit(Creature target, L2Skill skill) {
        return Math.min((int) calcStat(Stats.CRITICAL_RATE, this._activeChar.getTemplate().getBaseCritRate(), target, skill), 500);
    }

    public final int getMCriticalHit(Creature target, L2Skill skill) {
        return (int) calcStat(Stats.MCRITICAL_RATE, 8.0D, target, skill);
    }

    public int getEvasionRate(Creature target) {
        return (int) calcStat(Stats.EVASION_RATE, 0.0D, target, null);
    }

    public int getAccuracy() {
        return (int) calcStat(Stats.ACCURACY_COMBAT, 0.0D, null, null);
    }

    public int getMaxHp() {
        return (int) calcStat(Stats.MAX_HP, this._activeChar.getTemplate().getBaseHpMax(this._activeChar.getLevel()), null, null);
    }

    public int getMaxCp() {
        return 0;
    }

    public int getMaxMp() {
        return (int) calcStat(Stats.MAX_MP, this._activeChar.getTemplate().getBaseMpMax(this._activeChar.getLevel()), null, null);
    }

    public int getMAtk(Creature target, L2Skill skill) {
        double attack = this._activeChar.getTemplate().getBaseMAtk() * (this._activeChar.isChampion() ? Config.CHAMPION_ATK : 1.0D);
        if (skill != null)
            attack += skill.getPower();
        return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }

    public int getMAtkSpd() {
        return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, 333.0D * (this._activeChar.isChampion() ? Config.CHAMPION_SPD_ATK : 1.0D), null, null);
    }

    public int getMDef(Creature target, L2Skill skill) {
        return (int) calcStat(Stats.MAGIC_DEFENCE, this._activeChar.getTemplate().getBaseMDef() * (this._activeChar.isRaidRelated() ? Config.RAID_DEFENCE_MULTIPLIER : 1.0D), target, skill);
    }

    public int getPAtk(Creature target) {
        return (int) calcStat(Stats.POWER_ATTACK, this._activeChar.getTemplate().getBasePAtk() * (this._activeChar.isChampion() ? Config.CHAMPION_ATK : 1.0D), target, null);
    }

    public int getPAtkSpd() {
        return (int) calcStat(Stats.POWER_ATTACK_SPEED, this._activeChar.getTemplate().getBasePAtkSpd() * (this._activeChar.isChampion() ? Config.CHAMPION_SPD_ATK : 1.0D), null, null);
    }

    public int getPDef(Creature target) {
        return (int) calcStat(Stats.POWER_DEFENCE, this._activeChar.getTemplate().getBasePDef() * (this._activeChar.isRaidRelated() ? Config.RAID_DEFENCE_MULTIPLIER : 1.0D), target, null);
    }

    public final double getPAtkAnimals(Creature target) {
        return calcStat(Stats.PATK_ANIMALS, 1.0D, target, null);
    }

    public final double getPAtkDragons(Creature target) {
        return calcStat(Stats.PATK_DRAGONS, 1.0D, target, null);
    }

    public final double getPAtkInsects(Creature target) {
        return calcStat(Stats.PATK_INSECTS, 1.0D, target, null);
    }

    public final double getPAtkMonsters(Creature target) {
        return calcStat(Stats.PATK_MONSTERS, 1.0D, target, null);
    }

    public final double getPAtkPlants(Creature target) {
        return calcStat(Stats.PATK_PLANTS, 1.0D, target, null);
    }

    public final double getPAtkGiants(Creature target) {
        return calcStat(Stats.PATK_GIANTS, 1.0D, target, null);
    }

    public final double getPAtkMagicCreatures(Creature target) {
        return calcStat(Stats.PATK_MCREATURES, 1.0D, target, null);
    }

    public final double getPDefAnimals(Creature target) {
        return calcStat(Stats.PDEF_ANIMALS, 1.0D, target, null);
    }

    public final double getPDefDragons(Creature target) {
        return calcStat(Stats.PDEF_DRAGONS, 1.0D, target, null);
    }

    public final double getPDefInsects(Creature target) {
        return calcStat(Stats.PDEF_INSECTS, 1.0D, target, null);
    }

    public final double getPDefMonsters(Creature target) {
        return calcStat(Stats.PDEF_MONSTERS, 1.0D, target, null);
    }

    public final double getPDefPlants(Creature target) {
        return calcStat(Stats.PDEF_PLANTS, 1.0D, target, null);
    }

    public final double getPDefGiants(Creature target) {
        return calcStat(Stats.PDEF_GIANTS, 1.0D, target, null);
    }

    public final double getPDefMagicCreatures(Creature target) {
        return calcStat(Stats.PDEF_MCREATURES, 1.0D, target, null);
    }

    public int getPhysicalAttackRange() {
        return getActiveChar().getAttackType().getRange();
    }

    public final int getShldDef() {
        return (int) calcStat(Stats.SHIELD_DEFENCE, 0.0D, null, null);
    }

    public final int getMpConsume(L2Skill skill) {
        if (skill == null)
            return 1;
        double mpConsume = skill.getMpConsume();
        if (skill.isDance())
            if (this._activeChar != null && this._activeChar.getDanceCount() > 0)
                mpConsume += (this._activeChar.getDanceCount() * skill.getNextDanceMpCost());
        if (skill.isDance())
            return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
        if (skill.isMagic())
            return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
        return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
    }

    public final int getMpInitialConsume(L2Skill skill) {
        if (skill == null)
            return 1;
        double mpConsume = skill.getMpInitialConsume();
        if (skill.isDance())
            return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
        if (skill.isMagic())
            return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
        return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
    }

    public int getAttackElementValue(byte attackAttribute) {
        switch (attackAttribute) {
            case 1:
                return (int) calcStat(Stats.WIND_POWER, 0.0D, null, null);
            case 2:
                return (int) calcStat(Stats.FIRE_POWER, 0.0D, null, null);
            case 3:
                return (int) calcStat(Stats.WATER_POWER, 0.0D, null, null);
            case 4:
                return (int) calcStat(Stats.EARTH_POWER, 0.0D, null, null);
            case 5:
                return (int) calcStat(Stats.HOLY_POWER, 0.0D, null, null);
            case 6:
                return (int) calcStat(Stats.DARK_POWER, 0.0D, null, null);
        }
        return 0;
    }

    public double getDefenseElementValue(byte defenseAttribute) {
        switch (defenseAttribute) {
            case 1:
                return calcStat(Stats.WIND_RES, 1.0D, null, null);
            case 2:
                return calcStat(Stats.FIRE_RES, 1.0D, null, null);
            case 3:
                return calcStat(Stats.WATER_RES, 1.0D, null, null);
            case 4:
                return calcStat(Stats.EARTH_RES, 1.0D, null, null);
            case 5:
                return calcStat(Stats.HOLY_RES, 1.0D, null, null);
            case 6:
                return calcStat(Stats.DARK_RES, 1.0D, null, null);
        }
        return 1.0D;
    }

    public int getBaseRunSpeed() {
        return this._activeChar.getTemplate().getBaseRunSpeed();
    }

    public int getBaseWalkSpeed() {
        return this._activeChar.getTemplate().getBaseWalkSpeed();
    }

    protected final int getBaseMoveSpeed() {
        return this._activeChar.isRunning() ? getBaseRunSpeed() : getBaseWalkSpeed();
    }

    public final float getMovementSpeedMultiplier() {
        return getMoveSpeed() / getBaseMoveSpeed();
    }

    public final float getAttackSpeedMultiplier() {
        return (float) (1.1D * getPAtkSpd() / this._activeChar.getTemplate().getBasePAtkSpd());
    }

    public float getMoveSpeed() {
        return (float) calcStat(Stats.RUN_SPEED, getBaseMoveSpeed(), null, null);
    }

    public long getExp() {
        return this._exp;
    }

    public void setExp(long value) {
        this._exp = value;
    }

    public int getSp() {
        return this._sp;
    }

    public void setSp(int value) {
        this._sp = value;
    }

    public byte getLevel() {
        return this._level;
    }

    public void setLevel(byte value) {
        this._level = value;
    }

    public Creature getActiveChar() {
        return this._activeChar;
    }
}
