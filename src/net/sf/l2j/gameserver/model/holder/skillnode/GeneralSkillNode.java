package net.sf.l2j.gameserver.model.holder.skillnode;

import net.sf.l2j.commons.util.StatSet;

public class GeneralSkillNode extends SkillNode {
    private final int _cost;

    public GeneralSkillNode(StatSet set) {
        super(set);
        this._cost = set.getInteger("cost");
    }

    public int getCost() {
        return this._cost;
    }

    public int getCorrectedCost() {
        return Math.max(0, this._cost);
    }
}
