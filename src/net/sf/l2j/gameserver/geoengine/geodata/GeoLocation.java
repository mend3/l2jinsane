/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.location.Location;

public class GeoLocation extends Location {
    private byte _nswe;

    public GeoLocation(int x, int y, int z) {
        super(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
        this._nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
    }

    public void set(int x, int y, short z) {
        super.set(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
        this._nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
    }

    public int getGeoX() {
        return this._x;
    }

    public int getGeoY() {
        return this._y;
    }

    public int getX() {
        return GeoEngine.getWorldX(this._x);
    }

    public int getY() {
        return GeoEngine.getWorldY(this._y);
    }

    public byte getNSWE() {
        return this._nswe;
    }
}