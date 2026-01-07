/**/
package net.sf.l2j.gameserver.enums;

public enum SealType {
    NONE("", ""),
    AVARICE("Avarice", "Seal of Avarice"),
    GNOSIS("Gnosis", "Seal of Gnosis"),
    STRIFE("Strife", "Seal of Strife");

    public static final SealType[] VALUES = values();
    private final String _shortName;
    private final String _fullName;

    SealType(String param3, String param4) {
        this._shortName = param3;
        this._fullName = param4;
    }

    // $FF: synthetic method
    private static SealType[] $values() {
        return new SealType[]{NONE, AVARICE, GNOSIS, STRIFE};
    }

    public String getShortName() {
        return this._shortName;
    }

    public String getFullName() {
        return this._fullName;
    }
}
