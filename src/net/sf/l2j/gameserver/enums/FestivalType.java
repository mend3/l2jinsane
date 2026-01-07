/**/
package net.sf.l2j.gameserver.enums;

public enum FestivalType {
    MAX_31(60, "Level 31 or lower", 31),
    MAX_42(70, "Level 42 or lower", 42),
    MAX_53(100, "Level 53 or lower", 53),
    MAX_64(120, "Level 64 or lower", 64),
    MAX_NONE(150, "No Level Limit", 80);

    public static final FestivalType[] VALUES = values();
    private final int _maxScore;
    private final String _name;
    private final int _maxLevel;

    FestivalType(int maxScore, String name, int maxLevel) {
        this._maxScore = maxScore;
        this._name = name;
        this._maxLevel = maxLevel;
    }

    // $FF: synthetic method
    private static FestivalType[] $values() {
        return new FestivalType[]{MAX_31, MAX_42, MAX_53, MAX_64, MAX_NONE};
    }

    public int getMaxScore() {
        return this._maxScore;
    }

    public String getName() {
        return this._name;
    }

    public int getMaxLevel() {
        return this._maxLevel;
    }
}
