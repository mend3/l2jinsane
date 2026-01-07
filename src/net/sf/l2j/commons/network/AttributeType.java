/**/
package net.sf.l2j.commons.network;

public enum AttributeType {
    NONE(0),
    STATUS(1),
    CLOCK(2),
    BRACKETS(3),
    AGE_LIMIT(4),
    TEST_SERVER(5),
    PVP_SERVER(6),
    MAX_PLAYERS(7);

    public static final AttributeType[] VALUES = values();
    private final int _id;

    AttributeType(int id) {
        this._id = id;
    }

    // $FF: synthetic method
    private static AttributeType[] $values() {
        return new AttributeType[]{NONE, STATUS, CLOCK, BRACKETS, AGE_LIMIT, TEST_SERVER, PVP_SERVER, MAX_PLAYERS};
    }

    public int getId() {
        return this._id;
    }
}