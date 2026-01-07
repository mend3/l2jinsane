package net.sf.l2j.gameserver.model.holder.skillnode;

import net.sf.l2j.commons.util.StatSet;

public final class ClanSkillNode extends GeneralSkillNode {
    private final int _itemId;

    public ClanSkillNode(StatSet set) {
        super(set);
        this._itemId = set.getInteger("itemId");
    }

    public int getItemId() {
        return this._itemId;
    }
}
