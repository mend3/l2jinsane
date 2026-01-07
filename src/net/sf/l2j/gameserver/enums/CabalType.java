/**/
package net.sf.l2j.gameserver.enums;

public enum CabalType {
    NORMAL("No Cabal", "No Cabal"),
    DUSK("dusk", "Revolutionaries of Dusk"),
    DAWN("dawn", "Lords of Dawn");

    public static final CabalType[] VALUES = values();
    private final String _shortName;
    private final String _fullName;

    CabalType(String shortName, String fullName) {
        this._shortName = shortName;
        this._fullName = fullName;
    }

    // $FF: synthetic method
    private static CabalType[] $values() {
        return new CabalType[]{NORMAL, DUSK, DAWN};
    }

    public String getShortName() {
        return this._shortName;
    }

    public String getFullName() {
        return this._fullName;
    }
}
