package net.sf.l2j.gameserver.model.soulcrystal;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.AbsorbCrystalType;

public final class LevelingInfo {
    private final AbsorbCrystalType _absorbCrystalType;

    private final boolean _skillRequired;

    private final int _chanceStage;

    private final int _chanceBreak;

    private final int[] _levelList;

    public LevelingInfo(StatSet set) {
        this._absorbCrystalType = set.getEnum("absorbType", AbsorbCrystalType.class);
        this._skillRequired = set.getBool("skill");
        this._chanceStage = set.getInteger("chanceStage");
        this._chanceBreak = set.getInteger("chanceBreak");
        this._levelList = set.getIntegerArray("levelList");
    }

    public AbsorbCrystalType getAbsorbCrystalType() {
        return this._absorbCrystalType;
    }

    public boolean isSkillRequired() {
        return this._skillRequired;
    }

    public int getChanceStage() {
        return this._chanceStage;
    }

    public int getChanceBreak() {
        return this._chanceBreak;
    }

    public int[] getLevelList() {
        return this._levelList;
    }
}
