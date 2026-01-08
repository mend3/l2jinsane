/**/
package net.sf.l2j.commons.network;

public enum StatusType {
    AUTO(0, "Auto"),
    GOOD(1, "Good"),
    NORMAL(2, "Normal"),
    FULL(3, "Full"),
    DOWN(4, "Down"),
    GM_ONLY(5, "Gm Only");

    public static final StatusType[] VALUES = values();
    private final int _id;
    private final String _name;

    StatusType(int id, String name) {
        this._id = id;
        this._name = name;
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }
}