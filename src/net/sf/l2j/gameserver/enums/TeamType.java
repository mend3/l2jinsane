/**/
package net.sf.l2j.gameserver.enums;

public enum TeamType {
    NONE(0),
    BLUE(1),
    RED(2);

    private final int _id;

    TeamType(int id) {
        this._id = id;
    }

    // $FF: synthetic method
    private static TeamType[] $values() {
        return new TeamType[]{NONE, BLUE, RED};
    }

    public int getId() {
        return this._id;
    }
}
