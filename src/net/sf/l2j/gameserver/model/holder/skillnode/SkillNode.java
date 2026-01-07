package net.sf.l2j.gameserver.model.holder.skillnode;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

public class SkillNode extends IntIntHolder {
    private final int _minLvl;

    public SkillNode(StatSet set) {
        super(set.getInteger("id"), set.getInteger("lvl"));
        this._minLvl = set.getInteger("minLvl");
    }

    public int getMinLvl() {
        return this._minLvl;
    }
}
