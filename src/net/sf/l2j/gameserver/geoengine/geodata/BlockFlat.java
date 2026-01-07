/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import net.sf.l2j.gameserver.enums.GeoType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BlockFlat extends ABlock {
    protected final short _height;
    protected byte _nswe;

    public BlockFlat(ByteBuffer bb, GeoType format) {
        this._height = bb.getShort();
        this._nswe = (byte) (format != GeoType.L2D ? 15 : -1);
        if (format == GeoType.L2OFF) {
            bb.getShort();
        }

    }

    public final boolean hasGeoPos() {
        return true;
    }

    public final short getHeightNearest(int geoX, int geoY, int worldZ) {
        return this._height;
    }

    public final short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        return this._height;
    }

    public final short getHeightAbove(int geoX, int geoY, int worldZ) {
        return this._height > worldZ ? this._height : -32768;
    }

    public final short getHeightBelow(int geoX, int geoY, int worldZ) {
        return this._height < worldZ ? this._height : 32767;
    }

    public final byte getNsweNearest(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        return this._nswe;
    }

    public final byte getNsweAbove(int geoX, int geoY, int worldZ) {
        return this._height > worldZ ? this._nswe : 0;
    }

    public final byte getNsweBelow(int geoX, int geoY, int worldZ) {
        return this._height < worldZ ? this._nswe : 0;
    }

    public final int getIndexNearest(int geoX, int geoY, int worldZ) {
        return 0;
    }

    public final int getIndexAbove(int geoX, int geoY, int worldZ) {
        return this._height > worldZ ? 0 : -1;
    }

    public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        return this.getIndexAbove(geoX, geoY, worldZ);
    }

    public final int getIndexBelow(int geoX, int geoY, int worldZ) {
        return this._height < worldZ ? 0 : -1;
    }

    public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ) {
        return this.getIndexBelow(geoX, geoY, worldZ);
    }

    public final short getHeight(int index) {
        return this._height;
    }

    public final short getHeightOriginal(int index) {
        return this._height;
    }

    public final byte getNswe(int index) {
        return this._nswe;
    }

    public final byte getNsweOriginal(int index) {
        return this._nswe;
    }

    public final void setNswe(int index, byte nswe) {
        this._nswe = nswe;
    }

    public final void saveBlock(BufferedOutputStream stream) throws IOException {
        stream.write(-48);
        stream.write((byte) (this._height & 255));
        stream.write((byte) (this._height >> 8));
    }
}