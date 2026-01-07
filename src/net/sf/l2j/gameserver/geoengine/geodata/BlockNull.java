/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import java.io.BufferedOutputStream;

public class BlockNull extends ABlock {
    private byte _nswe = -1;

    public final boolean hasGeoPos() {
        return false;
    }

    public final short getHeightNearest(int geoX, int geoY, int worldZ) {
        return (short) worldZ;
    }

    public final short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        return (short) worldZ;
    }

    public final short getHeightAbove(int geoX, int geoY, int worldZ) {
        return (short) worldZ;
    }

    public final short getHeightBelow(int geoX, int geoY, int worldZ) {
        return (short) worldZ;
    }

    public final byte getNsweNearest(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final byte getNsweAbove(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final byte getNsweBelow(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final int getIndexNearest(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final int getIndexAbove(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final int getIndexBelow(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final short getHeight(int index) {
        return 0;
    }

    public final short getHeightOriginal(int index) {
        return 0;
    }

    public final byte getNswe(int index) {
        return this._nswe;
    }

    public final byte getNsweOriginal(int index) {
        return this._nswe;
    }

    public final void setNswe(int index, byte nswe) {
    }

    public final void saveBlock(BufferedOutputStream stream) {
    }
}