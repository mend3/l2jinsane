package net.sf.l2j.gameserver.model;

public class Doll {
    private final int _id;
    private final int _skillId;
    private final int _skillLvl;

    public Doll(int id, int skillId, int skillLvl) {
        this._id = id;
        this._skillId = skillId;
        this._skillLvl = skillLvl;
    }

    public int getId() {
        return this._id;
    }

    public int getSkillId() {
        return this._skillId;
    }

    public int getSkillLvl() {
        return this._skillLvl;
    }
}
