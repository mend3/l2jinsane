package enginemods.main.holders;

import enginemods.main.enums.BuffType;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;

public class BuffHolder {
    private final int _id;

    private final int _level;

    private BuffType _type = BuffType.NONE;

    public BuffHolder(int id, int level) {
        this._id = id;
        this._level = level;
    }

    public BuffHolder(BuffType type, int id, int level) {
        this._type = type;
        this._id = id;
        this._level = level;
    }

    public int getId() {
        return this._id;
    }

    public int getLevel() {
        return this._level;
    }

    public BuffType getType() {
        return this._type;
    }

    public final L2Skill getSkill() {
        return SkillTable.getInstance().getInfo(this._id, this._level);
    }
}
