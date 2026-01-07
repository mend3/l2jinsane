/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class BlockComplexDynamic extends BlockComplex implements IBlockDynamic {
    private final int _bx;
    private final int _by;
    private final byte[] _original;
    private final List<IGeoObject> _objects;

    public BlockComplexDynamic(int bx, int by, BlockFlat block) {
        byte nswe = block._nswe;
        byte heightLow = (byte) (block._height & 255);
        byte heightHigh = (byte) (block._height >> 8);
        this._buffer = new byte[192];

        for (int i = 0; i < 64; ++i) {
            this._buffer[i * 3] = nswe;
            this._buffer[i * 3 + 1] = heightLow;
            this._buffer[i * 3 + 2] = heightHigh;
        }

        this._bx = bx;
        this._by = by;
        this._original = new byte[192];
        System.arraycopy(this._buffer, 0, this._original, 0, 192);
        this._objects = new LinkedList();
    }

    public BlockComplexDynamic(int bx, int by, BlockComplex block) {
        this._buffer = block._buffer;
        block._buffer = null;
        this._bx = bx;
        this._by = by;
        this._original = new byte[192];
        System.arraycopy(this._buffer, 0, this._original, 0, 192);
        this._objects = new LinkedList();
    }

    public final short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        return (short) (this._original[index + 1] & 255 | this._original[index + 2] << 8);
    }

    public final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        return this._original[index];
    }

    public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._original[index + 1] & 255 | this._original[index + 2] << 8;
        return height > worldZ ? index : -1;
    }

    public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ) {
        int index = (geoX % 8 * 8 + geoY % 8) * 3;
        int height = this._original[index + 1] & 255 | this._original[index + 2] << 8;
        return height < worldZ ? index : -1;
    }

    public final short getHeightOriginal(int index) {
        return (short) (this._original[index + 1] & 255 | this._original[index + 2] << 8);
    }

    public final byte getNsweOriginal(int index) {
        return this._original[index];
    }

    public final synchronized void addGeoObject(IGeoObject object) {
        if (this._objects.add(object)) {
            this.update();
        }

    }

    public final synchronized void removeGeoObject(IGeoObject object) {
        if (this._objects.remove(object)) {
            this.update();
        }

    }

    private final void update() {
        System.arraycopy(this._original, 0, this._buffer, 0, 192);
        int minBX = this._bx * 8;
        int minBY = this._by * 8;
        int maxBX = minBX + 8;
        int maxBY = minBY + 8;
        Iterator var5 = this._objects.iterator();

        while (var5.hasNext()) {
            IGeoObject object = (IGeoObject) var5.next();
            int minOX = object.getGeoX();
            int minOY = object.getGeoY();
            int minOZ = object.getGeoZ();
            int maxOZ = minOZ + object.getHeight();
            byte[][] geoData = object.getObjectGeoData();
            int minGX = Math.max(minBX, minOX);
            int minGY = Math.max(minBY, minOY);
            int maxGX = Math.min(maxBX, minOX + geoData.length);
            int maxGY = Math.min(maxBY, minOY + geoData[0].length);

            for (int gx = minGX; gx < maxGX; ++gx) {
                for (int gy = minGY; gy < maxGY; ++gy) {
                    byte objNswe = geoData[gx - minOX][gy - minOY];
                    if (objNswe != 255) {
                        int ib = ((gx - minBX) * 8 + (gy - minBY)) * 3;
                        if (this._buffer[ib + 1] == this._original[ib + 1] && this._buffer[ib + 2] == this._original[ib + 2]) {
                            if (objNswe == 0) {
                                this._buffer[ib] = 0;
                                this._buffer[ib + 1] = (byte) (maxOZ & 255);
                                this._buffer[ib + 2] = (byte) (maxOZ >> 8);
                            } else {
                                short z = this.getHeight(ib);
                                if (Math.abs(z - minOZ) <= 48) {
                                    byte[] var10000 = this._buffer;
                                    var10000[ib] &= objNswe;
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}