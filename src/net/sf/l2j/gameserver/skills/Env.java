package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public final class Env {
    private Creature _character;
    private Cubic _cubic;
    private Creature _target;
    private ItemInstance _item;
    private L2Skill _skill;
    private double _value;
    private double _baseValue;
    private boolean _skillMastery = false;
    private byte _shield = 0;
    private boolean _soulShot = false;
    private boolean _spiritShot = false;
    private boolean _blessedSpiritShot = false;

    public Env() {
    }

    public Env(byte shield, boolean soulShot, boolean spiritShot, boolean blessedSpiritShot) {
        this._shield = shield;
        this._soulShot = soulShot;
        this._spiritShot = spiritShot;
        this._blessedSpiritShot = blessedSpiritShot;
    }

    public Creature getCharacter() {
        return this._character;
    }

    public void setCharacter(Creature character) {
        this._character = character;
    }

    public Cubic getCubic() {
        return this._cubic;
    }

    public void setCubic(Cubic cubic) {
        this._cubic = cubic;
    }

    public Creature getTarget() {
        return this._target;
    }

    public void setTarget(Creature target) {
        this._target = target;
    }

    public ItemInstance getItem() {
        return this._item;
    }

    public void setItem(ItemInstance item) {
        this._item = item;
    }

    public L2Skill getSkill() {
        return this._skill;
    }

    public void setSkill(L2Skill skill) {
        this._skill = skill;
    }

    public Player getPlayer() {
        return this._character == null ? null : this._character.getActingPlayer();
    }

    public double getValue() {
        return this._value;
    }

    public void setValue(double value) {
        this._value = value;
    }

    public double getBaseValue() {
        return this._baseValue;
    }

    public void setBaseValue(double baseValue) {
        this._baseValue = baseValue;
    }

    public boolean isSkillMastery() {
        return this._skillMastery;
    }

    public void setSkillMastery(boolean skillMastery) {
        this._skillMastery = skillMastery;
    }

    public byte getShield() {
        return this._shield;
    }

    public void setShield(byte shield) {
        this._shield = shield;
    }

    public boolean isSoulShot() {
        return this._soulShot;
    }

    public void setSoulShot(boolean soulShot) {
        this._soulShot = soulShot;
    }

    public boolean isSpiritShot() {
        return this._spiritShot;
    }

    public void setSpiritShot(boolean spiritShot) {
        this._spiritShot = spiritShot;
    }

    public boolean isBlessedSpiritShot() {
        return this._blessedSpiritShot;
    }

    public void setBlessedSpiritShot(boolean blessedSpiritShot) {
        this._blessedSpiritShot = blessedSpiritShot;
    }

    public void addValue(double value) {
        this._value += value;
    }

    public void subValue(double value) {
        this._value -= value;
    }

    public void mulValue(double value) {
        this._value *= value;
    }

    public void divValue(double value) {
        this._value /= value;
    }
}
