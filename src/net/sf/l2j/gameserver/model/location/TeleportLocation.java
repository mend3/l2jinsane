package net.sf.l2j.gameserver.model.location;

import net.sf.l2j.commons.util.StatSet;

public class TeleportLocation extends Location {
    private final int _price;
    private final boolean _isNoble;

    public TeleportLocation(StatSet set) {
        super(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
        this._price = set.getInteger("price");
        this._isNoble = set.getBool("isNoble");
    }

    public int getPrice() {
        return this._price;
    }

    public boolean isNoble() {
        return this._isNoble;
    }
}
