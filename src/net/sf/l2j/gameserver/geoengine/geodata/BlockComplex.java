/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import net.sf.l2j.gameserver.enums.GeoType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BlockComplex extends ABlock {
    protected byte[] _buffer;

    protected BlockComplex() {
        this._buffer = null;
    }

    public BlockComplex(ByteBuffer bb, GeoType format) {
        this._buffer = new byte[192];

        for (int i = 0; i < 64; ++i) {
            if (format != GeoType.L2D) {
                short data = bb.getShort();
                this._buffer[i * 3] = (byte) (data & 15);
                data = (short) ((short) (data & '\ufff0') >> 1);
                this._buffer[i * 3 + 1] = (byte) (data & 255);
                this._buffer[i * 3 + 2] = (byte) (data >> 8);
            } else {
                byte nswe = bb.get();
                this._buffer[i * 3] = nswe;
                short height = bb.getShort();
                this._buffer[i * 3 + 1] = (byte) (height & 255);
                this._buffer[i * 3 + 2] = (byte) (height >> 8);
            }
        }

    }

    public final boolean hasGeoPos() {
        return true;
    }

    public final short getHeightNearest(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        return (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
    }

    public short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getHeightNearest(geoX, geoY, worldZ);
    }

    public final short getHeightAbove(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        short height = (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
        return height > worldZ ? height : -32768;
    }

    public final short getHeightBelow(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        short height = (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
        return height < worldZ ? height : 32767;
    }

    public final byte getNsweNearest(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        return this._buffer[index];
    }

    public byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getNsweNearest(geoX, geoY, worldZ);
    }

    public final byte getNsweAbove(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
        return height > worldZ ? this._buffer[index] : 0;
    }

    public final byte getNsweBelow(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
        return height < worldZ ? this._buffer[index] : 0;
    }

    public final int getIndexNearest(int geoX, int geoY, int worldZ) {
        return (geoX % 8 * 8 + geoY % 8) * 3;
    }

    public final int getIndexAbove(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
        return height > worldZ ? index : -1;
    }

    public int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        return this.getIndexAbove(geoX, geoY, worldZ);
    }

    public final int getIndexBelow(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
        return height < worldZ ? index : -1;
    }

    public int getIndexBelowOriginal(int geoX, int geoY, int worldZ) {
        return this.getIndexBelow(geoX, geoY, worldZ);
    }

    public final short getHeight(int index) {
        return (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
    }

    public short getHeightOriginal(int index) {
        return (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
    }

    public final byte getNswe(int index) {
        return this._buffer[index];
    }

    public byte getNsweOriginal(int index) {
        return this._buffer[index];
    }

    public final void setNswe(int index, byte nswe) {
        this._buffer[index] = nswe;
    }

    public final void saveBlock(BufferedOutputStream stream) throws IOException {
        stream.write(-47);
        stream.write(this._buffer, 0, 192);
    }
}