package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public final class SkillList extends L2GameServerPacket {
    private final List<Skill> _skills = new ArrayList<>();

    public void addSkill(int id, int level, boolean passive, boolean disabled) {
        this._skills.add(new Skill(id, level, passive, disabled));
    }

    protected void writeImpl() {
        writeC(88);
        writeD(this._skills.size());
        for (Skill temp : this._skills) {
            writeD(temp.passive ? 1 : 0);
            writeD(temp.level);
            writeD(temp.id);
            writeC(temp.disabled ? 1 : 0);
        }
    }

    record Skill(int id, int level, boolean passive, boolean disabled) {
    }
}
