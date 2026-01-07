package net.sf.l2j.gameserver.model.holder.skillnode;

import net.sf.l2j.commons.util.StatSet;

public final class FishingSkillNode extends SkillNode {
    private final int _itemId;

    private final int _itemCount;

    private final boolean _isDwarven;

    public FishingSkillNode(StatSet set) {
        super(set);
        this._itemId = set.getInteger("itemId");
        this._itemCount = set.getInteger("itemCount");
        this._isDwarven = set.getBool("isDwarven", false);
    }

    public int getItemId() {
        return this._itemId;
    }

    public int getItemCount() {
        return this._itemCount;
    }

    public boolean isDwarven() {
        return this._isDwarven;
    }
}
