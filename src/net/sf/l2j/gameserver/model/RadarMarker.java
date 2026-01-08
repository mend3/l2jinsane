package net.sf.l2j.gameserver.model;

public record RadarMarker(int _type, int _x, int _y, int _z) {

    public RadarMarker(int x, int y, int z) {
        this(1, x, y, z);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof RadarMarker(int type, int x, int y, int z))) {
            return false;
        } else {
            if (this._type != type) {
                return false;
            } else if (this._x != x) {
                return false;
            } else if (this._y != y) {
                return false;
            } else {
                return this._z == z;
            }
        }
    }
}
