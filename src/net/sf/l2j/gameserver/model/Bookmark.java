package net.sf.l2j.gameserver.model;

public class Bookmark {
    private final String _name;
    private final int _objId;
    private final int _x;
    private final int _y;
    private final int _z;

    public Bookmark(String name, int objId, int x, int y, int z) {
        this._name = name;
        this._objId = objId;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public String getName() {
        return this._name;
    }

    public int getId() {
        return this._objId;
    }

    public int getX() {
        return this._x;
    }

    public int getY() {
        return this._y;
    }

    public int getZ() {
        return this._z;
    }
}
