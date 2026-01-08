/**/
package net.sf.l2j.gameserver.enums.actors;

public enum StoreType {
    NONE(0),
    SELL(1),
    SELL_MANAGE(2),
    BUY(3),
    BUY_MANAGE(4),
    MANUFACTURE(5),
    PACKAGE_SELL(8);

    private final int _id;

    StoreType(int id) {
        this._id = id;
    }

    public int getId() {
        return this._id;
    }
}
