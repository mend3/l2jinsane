package net.sf.l2j.gameserver.model.soulcrystal;

import net.sf.l2j.commons.util.StatSet;

public final class SoulCrystal {
    private final int _level;

    private final int _initialItemId;

    private final int _stagedItemId;

    private final int _brokenItemId;

    public SoulCrystal(StatSet set) {
        this._level = set.getInteger("level");
        this._initialItemId = set.getInteger("initial");
        this._stagedItemId = set.getInteger("staged");
        this._brokenItemId = set.getInteger("broken");
    }

    public int getLevel() {
        return this._level;
    }

    public int getInitialItemId() {
        return this._initialItemId;
    }

    public int getStagedItemId() {
        return this._stagedItemId;
    }

    public int getBrokenItemId() {
        return this._brokenItemId;
    }
}
