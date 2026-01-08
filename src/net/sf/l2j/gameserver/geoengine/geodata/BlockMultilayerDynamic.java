/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import java.util.LinkedList;
import java.util.List;

public final class BlockMultilayerDynamic extends BlockMultilayer implements IBlockDynamic {
    private final int _bx;
    private final int _by;
    private final byte[] _original;
    private final List<IGeoObject> _objects;

    public BlockMultilayerDynamic(int bx, int by, BlockMultilayer block) {
        this._buffer = block._buffer;
        block._buffer = null;
        this._bx = bx;
        this._by = by;
        this._original = new byte[this._buffer.length];
        System.arraycopy(this._buffer, 0, this._original, 0, this._buffer.length);
        this._objects = new LinkedList<>();
    }

    public short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        int index = this.getIndexNearestOriginal(geoX, geoY, worldZ);
        return (short) (this._original[index + 1] & 255 | this._original[index + 2] << 8);
    }

    public byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        int index = this.getIndexNearestOriginal(geoX, geoY, worldZ);
        return this._original[index];
    }

    private int getIndexNearestOriginal(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._original[index] * 3 + 1;
        }

        byte layers = this._original[index++];

        for (int limit = Integer.MAX_VALUE; layers-- > 0; index += 3) {
            int height = this._original[index + 1] & 255 | this._original[index + 2] << 8;
            int distance = Math.abs(height - worldZ);
            if (distance > limit) {
                break;
            }

            limit = distance;
        }

        return index - 3;
    }

    public int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._original[index] * 3 + 1;
        }

        byte layers = this._original[index++];

        for (index += (layers - 1) * 3; layers-- > 0; index -= 3) {
            int height = this._original[index + 1] & 255 | this._original[index + 2] << 8;
            if (height > worldZ) {
                return index;
            }
        }

        return -1;
    }

    public int getIndexBelowOriginal(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._original[index] * 3 + 1;
        }

        for (byte layers = this._original[index++]; layers-- > 0; index += 3) {
            int height = this._original[index + 1] & 255 | this._original[index + 2] << 8;
            if (height < worldZ) {
                return index;
            }
        }

        return -1;
    }

    public short getHeightOriginal(int index) {
        return (short) (this._original[index + 1] & 255 | this._original[index + 2] << 8);
    }

    public byte getNsweOriginal(int index) {
        return this._original[index];
    }

    public synchronized void addGeoObject(IGeoObject object) {
        if (this._objects.add(object)) {
            this.update();
        }

    }

    public synchronized void removeGeoObject(IGeoObject object) {
        if (this._objects.remove(object)) {
            this.update();
        }

    }

    private void update() {
        System.arraycopy(this._original, 0, this._buffer, 0, this._original.length);
        int minBX = this._bx * 8;
        int minBY = this._by * 8;
        int maxBX = minBX + 8;
        int maxBY = minBY + 8;

        for (IGeoObject object : this._objects) {
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
                        int ib = this.getIndexNearest(gx, gy, minOZ);
                        if (this._buffer[ib + 1] == this._original[ib + 1] && this._buffer[ib + 2] == this._original[ib + 2]) {
                            if (objNswe == 0) {
                                this._buffer[ib] = 0;
                                int z = maxOZ;
                                int i = this.getIndexAbove(gx, gy, minOZ);
                                if (i != -1) {
                                    int az = this.getHeight(i);
                                    if (az <= maxOZ) {
                                        z = az - 48;
                                    }
                                }

                                this._buffer[ib + 1] = (byte) (z & 255);
                                this._buffer[ib + 2] = (byte) (z >> 8);
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