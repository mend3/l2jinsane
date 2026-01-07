package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;

public class IntIntHolder {
    private int _id;

    private int _value;

    public IntIntHolder(int id, int value) {
        this._id = id;
        this._value = value;
    }

    public int getId() {
        return this._id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public int getValue() {
        return this._value;
    }

    public void setValue(int value) {
        this._value = value;
    }

    public final L2Skill getSkill() {
        return SkillTable.getInstance().getInfo(this._id, this._value);
    }

    public String toString() {
        return getClass().getSimpleName() + ": Id: " + getClass().getSimpleName() + ", Value: " + this._id;
    }
}
