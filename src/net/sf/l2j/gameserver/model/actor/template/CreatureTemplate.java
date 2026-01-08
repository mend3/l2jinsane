package net.sf.l2j.gameserver.model.actor.template;

import net.sf.l2j.commons.util.StatSet;

public class CreatureTemplate {
    protected final double _collisionRadius;
    protected final double _collisionHeight;
    private final int _baseSTR;
    private final int _baseCON;
    private final int _baseDEX;
    private final int _baseINT;
    private final int _baseWIT;
    private final int _baseMEN;
    private final double _baseHpMax;
    private final double _baseMpMax;
    private final double _baseHpReg;
    private final double _baseMpReg;
    private final double _basePAtk;
    private final double _baseMAtk;
    private final double _basePDef;
    private final double _baseMDef;
    private final int _basePAtkSpd;
    private final int _baseCritRate;
    private final int _baseWalkSpd;
    private final int _baseRunSpd;

    public CreatureTemplate(StatSet set) {
        this._baseSTR = set.getInteger("str", 40);
        this._baseCON = set.getInteger("con", 21);
        this._baseDEX = set.getInteger("dex", 30);
        this._baseINT = set.getInteger("int", 20);
        this._baseWIT = set.getInteger("wit", 43);
        this._baseMEN = set.getInteger("men", 20);
        this._baseHpMax = set.getDouble("hp", 0.0F);
        this._baseMpMax = set.getDouble("mp", 0.0F);
        this._baseHpReg = set.getDouble("hpRegen", 1.5F);
        this._baseMpReg = set.getDouble("mpRegen", 0.9);
        this._basePAtk = set.getDouble("pAtk");
        this._baseMAtk = set.getDouble("mAtk");
        this._basePDef = set.getDouble("pDef");
        this._baseMDef = set.getDouble("mDef");
        this._basePAtkSpd = set.getInteger("atkSpd", 300);
        this._baseCritRate = set.getInteger("crit", 4);
        this._baseWalkSpd = set.getInteger("walkSpd", 0);
        this._baseRunSpd = set.getInteger("runSpd", 1);
        this._collisionRadius = set.getDouble("radius");
        this._collisionHeight = set.getDouble("height");
    }

    public final int getBaseSTR() {
        return this._baseSTR;
    }

    public final int getBaseCON() {
        return this._baseCON;
    }

    public final int getBaseDEX() {
        return this._baseDEX;
    }

    public final int getBaseINT() {
        return this._baseINT;
    }

    public final int getBaseWIT() {
        return this._baseWIT;
    }

    public final int getBaseMEN() {
        return this._baseMEN;
    }

    public double getBaseHpMax(int level) {
        return this._baseHpMax;
    }

    public double getBaseMpMax(int level) {
        return this._baseMpMax;
    }

    public final double getBaseHpReg() {
        return this._baseHpReg;
    }

    public final double getBaseMpReg() {
        return this._baseMpReg;
    }

    public final double getBasePAtk() {
        return this._basePAtk;
    }

    public final double getBaseMAtk() {
        return this._baseMAtk;
    }

    public final double getBasePDef() {
        return this._basePDef;
    }

    public final double getBaseMDef() {
        return this._baseMDef;
    }

    public final int getBasePAtkSpd() {
        return this._basePAtkSpd;
    }

    public final int getBaseCritRate() {
        return this._baseCritRate;
    }

    public final int getBaseWalkSpeed() {
        return this._baseWalkSpd;
    }

    public final int getBaseRunSpeed() {
        return this._baseRunSpd;
    }

    public final double getCollisionRadius() {
        return this._collisionRadius;
    }

    public final double getCollisionHeight() {
        return this._collisionHeight;
    }
}
