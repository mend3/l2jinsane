/**/
package net.sf.l2j.gameserver.enums;

public enum OlympiadType {
    CLASSED("classed"),
    NON_CLASSED("non-classed");

    private final String _name;

    OlympiadType(String name) {
        this._name = name;
    }

    // $FF: synthetic method
    private static OlympiadType[] $values() {
        return new OlympiadType[]{CLASSED, NON_CLASSED};
    }

    public final String toString() {
        return this._name;
    }
}
