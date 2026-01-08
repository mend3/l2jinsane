/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import net.sf.l2j.gameserver.enums.GeoType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BlockMultilayer extends ABlock {
    private static final int MAX_LAYERS = 127;
    private static ByteBuffer _temp;
    protected byte[] _buffer;

    protected BlockMultilayer() {
        this._buffer = null;
    }

    public BlockMultilayer(ByteBuffer bb, GeoType format) {
        for (int cell = 0; cell < 64; ++cell) {
            byte layers = format != GeoType.L2OFF ? bb.get() : (byte) bb.getShort();
            if (layers <= 0 || layers > 127) {
                throw new RuntimeException("Invalid layer count for MultilayerBlock");
            }

            _temp.put(layers);

            for (byte layer = 0; layer < layers; ++layer) {
                if (format != GeoType.L2D) {
                    short data = bb.getShort();
                    _temp.put((byte) (data & 15));
                    _temp.putShort((short) ((short) (data & '\ufff0') >> 1));
                } else {
                    _temp.put(bb.get());
                    _temp.putShort(bb.getShort());
                }
            }
        }

        this._buffer = Arrays.copyOf(_temp.array(), _temp.position());
        _temp.clear();
    }

    public static void initialize() {
        _temp = ByteBuffer.allocate(24384);
        _temp.order(ByteOrder.LITTLE_ENDIAN);
    }

    public static void release() {
        _temp = null;
    }

    public final boolean hasGeoPos() {
        return true;
    }

    public final short getHeightNearest(int geoX, int geoY, int worldZ) {
        int index = this.getIndexNearest(geoX, geoY, worldZ);
        return (short) (this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8);
    }

    public short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getHeightNearest(geoX, geoY, worldZ);
    }

    public final short getHeightAbove(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        byte layers = this._buffer[index++];

        for (index += (layers - 1) * 3; layers-- > 0; index -= 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height > worldZ) {
                return (short) height;
            }
        }

        return -32768;
    }

    public final short getHeightBelow(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        for (byte layers = this._buffer[index++]; layers-- > 0; index += 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height < worldZ) {
                return (short) height;
            }
        }

        return 32767;
    }

    public final byte getNsweNearest(int geoX, int geoY, int worldZ) {
        int index = this.getIndexNearest(geoX, geoY, worldZ);
        return this._buffer[index];
    }

    public byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getNsweNearest(geoX, geoY, worldZ);
    }

    public final byte getNsweAbove(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        byte layers = this._buffer[index++];

        for (index += (layers - 1) * 3; layers-- > 0; index -= 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height > worldZ) {
                return this._buffer[index];
            }
        }

        return 0;
    }

    public final byte getNsweBelow(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        for (byte layers = this._buffer[index++]; layers-- > 0; index += 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height < worldZ) {
                return this._buffer[index];
            }
        }

        return 0;
    }

    public final int getIndexNearest(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        byte layers = this._buffer[index++];

        for (int limit = Integer.MAX_VALUE; layers-- > 0; index += 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            int distance = Math.abs(height - worldZ);
            if (distance > limit) {
                break;
            }

            limit = distance;
        }

        return index - 3;
    }

    public final int getIndexAbove(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        byte layers = this._buffer[index++];

        for (index += (layers - 1) * 3; layers-- > 0; index -= 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height > worldZ) {
                return index;
            }
        }

        return -1;
    }

    public int getIndexAboveOriginal(int geoX, int geoY, int worldZ) {
        return this.getIndexAbove(geoX, geoY, worldZ);
    }

    public final int getIndexBelow(int geoX, int geoY, int worldZ) {
        int index = 0;

        for (int i = 0; i < geoX % 8 * 8 + geoY % 8; ++i) {
            index += this._buffer[index] * 3 + 1;
        }

        for (byte layers = this._buffer[index++]; layers-- > 0; index += 3) {
            int height = this._buffer[index + 1] & 255 | this._buffer[index + 2] << 8;
            if (height < worldZ) {
                return index;
            }
        }

        return -1;
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
        stream.write(-46);
        int index = 0;

        for (int i = 0; i < 64; ++i) {
            byte layers = this._buffer[index++];
            stream.write(layers);
            stream.write(this._buffer, index, layers * 3);
            index += layers * 3;
        }

    }
}