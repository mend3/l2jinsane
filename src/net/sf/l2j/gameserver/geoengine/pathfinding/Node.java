/**/
package net.sf.l2j.gameserver.geoengine.pathfinding;

import net.sf.l2j.gameserver.geoengine.geodata.GeoLocation;

public class Node {
    private GeoLocation _loc;
    private Node _parent;
    private Node _child;
    private double _cost = -1000.0D;

    public void setLoc(int x, int y, int z) {
        this._loc = new GeoLocation(x, y, z);
    }

    public GeoLocation getLoc() {
        return this._loc;
    }

    public Node getParent() {
        return this._parent;
    }

    public void setParent(Node parent) {
        this._parent = parent;
    }

    public Node getChild() {
        return this._child;
    }

    public void setChild(Node child) {
        this._child = child;
    }

    public double getCost() {
        return this._cost;
    }

    public void setCost(double cost) {
        this._cost = cost;
    }

    public void free() {
        this._loc = null;
        this._parent = null;
        this._child = null;
        this._cost = -1000.0D;
    }
}