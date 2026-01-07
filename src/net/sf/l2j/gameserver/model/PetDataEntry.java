package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.util.StatSet;

public class PetDataEntry {
    private final long _maxExp;

    private final int _maxMeal;

    private final int _expType;

    private final int _mealInBattle;

    private final int _mealInNormal;

    private final double _pAtk;

    private final double _pDef;

    private final double _mAtk;

    private final double _mDef;

    private final double _maxHp;

    private final double _maxMp;

    private final float _hpRegen;

    private final float _mpRegen;

    private final int _ssCount;

    private final int _spsCount;

    private final int _mountMealInBattle;

    private final int _mountMealInNormal;

    private final int _mountAtkSpd;

    private final double _mountPAtk;

    private final double _mountMAtk;

    private final int _mountBaseSpeed;

    private final int _mountWaterSpeed;

    private final int _mountFlySpeed;

    public PetDataEntry(StatSet stats) {
        this._maxExp = stats.getLong("exp");
        this._maxMeal = stats.getInteger("maxMeal");
        this._expType = stats.getInteger("expType");
        this._mealInBattle = stats.getInteger("mealInBattle");
        this._mealInNormal = stats.getInteger("mealInNormal");
        this._pAtk = stats.getDouble("pAtk");
        this._pDef = stats.getDouble("pDef");
        this._mAtk = stats.getDouble("mAtk");
        this._mDef = stats.getDouble("mDef");
        this._maxHp = stats.getDouble("hp");
        this._maxMp = stats.getDouble("mp");
        this._hpRegen = stats.getFloat("hpRegen");
        this._mpRegen = stats.getFloat("mpRegen");
        this._ssCount = stats.getInteger("ssCount");
        this._spsCount = stats.getInteger("spsCount");
        this._mountMealInBattle = stats.getInteger("mealInBattleOnRide", 0);
        this._mountMealInNormal = stats.getInteger("mealInNormalOnRide", 0);
        this._mountAtkSpd = stats.getInteger("atkSpdOnRide", 0);
        this._mountPAtk = stats.getDouble("pAtkOnRide", 0.0D);
        this._mountMAtk = stats.getDouble("mAtkOnRide", 0.0D);
        String speed = stats.getString("speedOnRide", null);
        if (speed != null) {
            String[] speeds = speed.split(";");
            this._mountBaseSpeed = Integer.parseInt(speeds[0]);
            this._mountWaterSpeed = Integer.parseInt(speeds[2]);
            this._mountFlySpeed = Integer.parseInt(speeds[4]);
        } else {
            this._mountBaseSpeed = 0;
            this._mountWaterSpeed = 0;
            this._mountFlySpeed = 0;
        }
    }

    public long getMaxExp() {
        return this._maxExp;
    }

    public int getMaxMeal() {
        return this._maxMeal;
    }

    public int getExpType() {
        return this._expType;
    }

    public int getMealInBattle() {
        return this._mealInBattle;
    }

    public int getMealInNormal() {
        return this._mealInNormal;
    }

    public double getPAtk() {
        return this._pAtk;
    }

    public double getPDef() {
        return this._pDef;
    }

    public double getMAtk() {
        return this._mAtk;
    }

    public double getMDef() {
        return this._mDef;
    }

    public double getMaxHp() {
        return this._maxHp;
    }

    public double getMaxMp() {
        return this._maxMp;
    }

    public float getHpRegen() {
        return this._hpRegen;
    }

    public float getMpRegen() {
        return this._mpRegen;
    }

    public int getSsCount() {
        return this._ssCount;
    }

    public int getSpsCount() {
        return this._spsCount;
    }

    public int getMountMealInBattle() {
        return this._mountMealInBattle;
    }

    public int getMountMealInNormal() {
        return this._mountMealInNormal;
    }

    public int getMountAtkSpd() {
        return this._mountAtkSpd;
    }

    public double getMountPAtk() {
        return this._mountPAtk;
    }

    public double getMountMAtk() {
        return this._mountMAtk;
    }

    public int getMountBaseSpeed() {
        return this._mountBaseSpeed;
    }

    public int getMountSwimSpeed() {
        return this._mountWaterSpeed;
    }

    public int getMountFlySpeed() {
        return this._mountFlySpeed;
    }
}
