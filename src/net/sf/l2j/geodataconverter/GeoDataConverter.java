package net.sf.l2j.geodataconverter;

import net.sf.l2j.Config;
import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.gameserver.enums.GeoType;
import net.sf.l2j.gameserver.geoengine.geodata.ABlock;
import net.sf.l2j.gameserver.geoengine.geodata.BlockComplex;
import net.sf.l2j.gameserver.geoengine.geodata.BlockFlat;
import net.sf.l2j.gameserver.geoengine.geodata.BlockMultilayer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public final class GeoDataConverter {
    private static GeoType _format;

    private static ABlock[][] _blocks;

    public static void main(String[] args) {
        Config.loadGeodataConverter();
        String type = "";
        Scanner scn = new Scanner(System.in);
        try {
            while (!type.equalsIgnoreCase("J") && !type.equalsIgnoreCase("O") && !type.equalsIgnoreCase("E")) {
                System.out.println("GeoDataConverter: Select source geodata type:");
                System.out.println("  J: L2J (e.g. 23_22.l2j)");
                System.out.println("  O: L2OFF (e.g. 23_22_conv.dat)");
                System.out.println("  E: Exit");
                System.out.print("Choice: ");
                type = scn.next();
            }
            scn.close();
        } catch (Throwable throwable) {
            try {
                scn.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
        if (type.equalsIgnoreCase("E"))
            System.exit(0);
        _format = type.equalsIgnoreCase("J") ? GeoType.L2J : GeoType.L2OFF;
        System.out.println("GeoDataConverter: Converting all " + _format + " according to listing in \"geoengine.properties\" config file.");
        _blocks = new ABlock[256][256];
        BlockMultilayer.initialize();
        ExProperties props = Config.initProperties("./config/geoengine.properties");
        int converted = 0;
        for (int rx = 16; rx <= 26; rx++) {
            for (int ry = 10; ry <= 25; ry++) {
                if (props.containsKey(rx + "_" + rx)) {
                    String input = String.format(_format.getFilename(), rx, ry);
                    if (!loadGeoBlocks(input)) {
                        System.out.println("GeoDataConverter: Unable to load " + input + " region file.");
                    } else if (!recalculateNswe()) {
                        System.out.println("GeoDataConverter: Unable to convert " + input + " region file.");
                    } else {
                        String output = String.format(GeoType.L2D.getFilename(), rx, ry);
                        if (!saveGeoBlocks(output)) {
                            System.out.println("GeoDataConverter: Unable to save " + output + " region file.");
                        } else {
                            converted++;
                            System.out.println("GeoDataConverter: Created " + output + " region file.");
                        }
                    }
                }
            }
        }
        System.out.println("GeoDataConverter: Converted " + converted + " " + _format.toString() + " to L2D region file(s).");
        BlockMultilayer.release();
    }

    private static boolean loadGeoBlocks(String filename) {
        try {
            RandomAccessFile raf = new RandomAccessFile(Config.GEODATA_PATH + Config.GEODATA_PATH, "r");
            try {
                FileChannel fc = raf.getChannel();
                try {
                    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size()).load();
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    if (_format == GeoType.L2OFF)
                        for (int i = 0; i < 18; i++)
                            buffer.get();
                    int ix;
                    for (ix = 0; ix < 256; ix++) {
                        for (int iy = 0; iy < 256; iy++) {
                            if (_format == GeoType.L2J) {
                                byte type = buffer.get();
                                switch (type) {
                                    case 0:
                                        _blocks[ix][iy] = new BlockFlat(buffer, _format);
                                        break;
                                    case 1:
                                        _blocks[ix][iy] = new BlockComplex(buffer, _format);
                                        break;
                                    case 2:
                                        _blocks[ix][iy] = new BlockMultilayer(buffer, _format);
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Unknown block type: " + type);
                                }
                            } else {
                                short type = buffer.getShort();
                                switch (type) {
                                    case 0:
                                        _blocks[ix][iy] = new BlockFlat(buffer, _format);
                                        break;
                                    case 64:
                                        _blocks[ix][iy] = new BlockComplex(buffer, _format);
                                        break;
                                    default:
                                        _blocks[ix][iy] = new BlockMultilayer(buffer, _format);
                                        break;
                                }
                            }
                        }
                    }
                    if (buffer.remaining() > 0) {
                        System.out.println("GeoDataConverter: Region file " + filename + " can be corrupted, remaining " + buffer.remaining() + " bytes to read.");
                        ix = 0;
                        if (fc != null)
                            fc.close();
                        raf.close();
                        return ix > 0;
                    }
                    ix = 1;
                    if (fc != null)
                        fc.close();
                    raf.close();
                    return ix > 0;
                } catch (Throwable throwable) {
                    if (fc != null)
                        try {
                            fc.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (Throwable throwable) {
                try {
                    raf.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Exception e) {
            System.out.println("GeoDataConverter: Error while loading " + filename + " region file.");
            return false;
        }
    }

    private static boolean recalculateNswe() {
        try {
            for (int x = 0; x < 2048; x++) {
                for (int y = 0; y < 2048; y++) {
                    ABlock block = _blocks[x / 8][y / 8];
                    if (!(block instanceof BlockFlat)) {
                        short height = Short.MAX_VALUE;
                        int index;
                        while ((index = block.getIndexBelow(x, y, height)) != -1) {
                            height = block.getHeight(index);
                            byte nswe = block.getNswe(index);
                            nswe = updateNsweBelow(x, y, height, nswe);
                            block.setNswe(index, nswe);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte updateNsweBelow(int x, int y, short z, byte nswe) {
        short height = (short) (z + 48);
        byte nsweN = getNsweBelow(x, y - 1, height);
        byte nsweS = getNsweBelow(x, y + 1, height);
        byte nsweW = getNsweBelow(x - 1, y, height);
        byte nsweE = getNsweBelow(x + 1, y, height);
        if ((nswe & 0x8) != 0 && (nsweN & 0x2) != 0 && (nswe & 0x2) != 0 && (nsweW & 0x8) != 0)
            nswe = (byte) (nswe | Byte.MIN_VALUE);
        if ((nswe & 0x8) != 0 && (nsweN & 0x1) != 0 && (nswe & 0x1) != 0 && (nsweE & 0x8) != 0)
            nswe = (byte) (nswe | 0x40);
        if ((nswe & 0x4) != 0 && (nsweS & 0x2) != 0 && (nswe & 0x2) != 0 && (nsweW & 0x4) != 0)
            nswe = (byte) (nswe | 0x20);
        if ((nswe & 0x4) != 0 && (nsweS & 0x1) != 0 && (nswe & 0x1) != 0 && (nsweE & 0x4) != 0)
            nswe = (byte) (nswe | 0x10);
        return nswe;
    }

    private static byte getNsweBelow(int geoX, int geoY, short worldZ) {
        if (geoX < 0 || geoX >= 2048)
            return 0;
        if (geoY < 0 || geoY >= 2048)
            return 0;
        ABlock block = _blocks[geoX / 8][geoY / 8];
        int index = block.getIndexBelow(geoX, geoY, worldZ);
        return (index == -1) ? 0 : block.getNswe(index);
    }

    private static boolean saveGeoBlocks(String filename) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Config.GEODATA_PATH + Config.GEODATA_PATH), 12582912);
            try {
                int ix;
                for (ix = 0; ix < 256; ix++) {
                    for (int iy = 0; iy < 256; iy++)
                        _blocks[ix][iy].saveBlock(bos);
                }
                bos.flush();
                ix = 1;
                bos.close();
                return ix > 0;
            } catch (Throwable throwable) {
                try {
                    bos.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
