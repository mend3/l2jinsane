/**/
package net.sf.l2j.gameserver.geoengine;

import net.sf.l2j.Config;
import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.enums.GeoType;
import net.sf.l2j.gameserver.geoengine.geodata.*;
import net.sf.l2j.gameserver.geoengine.pathfinding.Node;
import net.sf.l2j.gameserver.geoengine.pathfinding.NodeBuffer;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeoEngine {
    protected static final CLogger LOGGER = new CLogger(GeoEngine.class.getName());
    private static final String GEO_BUG = "%d;%d;%d;%d;%d;%d;%d;%s\r\n";
    private final ABlock[][] _blocks = new ABlock[2816][4096];
    private final BlockNull _nullBlock = new BlockNull();
    private final PrintWriter _geoBugReports;
    private final Set<ItemInstance> _debugItems = ConcurrentHashMap.newKeySet();
    private final GeoEngine.BufferHolder[] _buffers;
    private int _findSuccess = 0;
    private int _findFails = 0;
    private int _postFilterPlayableUses = 0;
    private int _postFilterUses = 0;
    private long _postFilterElapsed = 0L;

    public GeoEngine() {
        BlockMultilayer.initialize();
        ExProperties props = Config.initProperties("./config/geoengine.properties");
        int loaded = 0;
        int failed = 0;

        for (int rx = 16; rx <= 26; ++rx) {
            for (int ry = 10; ry <= 25; ++ry) {
                String var10001 = String.valueOf(rx);
                if (props.containsKey(var10001 + "_" + ry)) {
                    if (this.loadGeoBlocks(rx, ry)) {
                        ++loaded;
                    } else {
                        ++failed;
                    }
                } else {
                    this.loadNullBlocks(rx, ry);
                }
            }
        }

        LOGGER.info("Loaded {} L2D region files.", loaded);
        BlockMultilayer.release();
        if (failed > 0) {
            LOGGER.warn("Failed to load {} L2D region files. Please consider to check your \"geodata.properties\" settings and location of your geodata files.", failed);
            System.exit(1);
        }

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileOutputStream(new File(Config.GEODATA_PATH + "geo_bugs.txt"), true), true);
        } catch (Exception var12) {
            LOGGER.error("Couldn't load \"geo_bugs.txt\" file.", var12);
        }

        this._geoBugReports = writer;
        String[] array = Config.PATHFIND_BUFFERS.split(";");
        this._buffers = new GeoEngine.BufferHolder[array.length];
        int count = 0;

        for (int i = 0; i < array.length; ++i) {
            String buf = array[i];
            String[] args = buf.split("x");

            try {
                int size = Integer.parseInt(args[1]);
                count += size;
                this._buffers[i] = new GeoEngine.BufferHolder(Integer.parseInt(args[0]), size);
            } catch (Exception var11) {
                LOGGER.error("Couldn't load buffer setting: {}.", var11, buf);
            }
        }

        LOGGER.info("Loaded {} node buffers.", count);
    }

    private static List<Location> constructPath(Node target) {
        LinkedList<Location> list = new LinkedList();
        int dx = 0;
        int dy = 0;

        for (Node parent = target.getParent(); parent != null; parent = parent.getParent()) {
            int nx = parent.getLoc().getGeoX() - target.getLoc().getGeoX();
            int ny = parent.getLoc().getGeoY() - target.getLoc().getGeoY();
            if (dx != nx || dy != ny) {
                list.addFirst(target.getLoc());
                dx = nx;
                dy = ny;
            }

            target = parent;
        }

        return list;
    }

    public static int getGeoX(int worldX) {
        return MathUtil.limit(worldX, -131072, 229376) - -131072 >> 4;
    }

    public static int getGeoY(int worldY) {
        return MathUtil.limit(worldY, -262144, 262144) - -262144 >> 4;
    }

    public static int getWorldX(int geoX) {
        return (MathUtil.limit(geoX, 0, 22528) << 4) + -131072 + 8;
    }

    public static int getWorldY(int geoY) {
        return (MathUtil.limit(geoY, 0, 32768) << 4) + -262144 + 8;
    }

    public static byte[][] calculateGeoObject(boolean[][] inside) {
        int width = inside.length;
        int height = inside[0].length;
        byte[][] result = new byte[width][height];

        for (int ix = 0; ix < width; ++ix) {
            for (int iy = 0; iy < height; ++iy) {
                if (inside[ix][iy]) {
                    result[ix][iy] = 0;
                } else {
                    byte nswe = -1;
                    if (iy < height - 1 && inside[ix][iy + 1]) {
                        nswe &= -5;
                    }

                    if (iy > 0 && inside[ix][iy - 1]) {
                        nswe &= -9;
                    }

                    if (ix < width - 1 && inside[ix + 1][iy]) {
                        nswe &= -2;
                    }

                    if (ix > 0 && inside[ix - 1][iy]) {
                        nswe &= -3;
                    }

                    if (ix < width - 1 && iy < height - 1 && (inside[ix + 1][iy + 1] || inside[ix][iy + 1] || inside[ix + 1][iy])) {
                        nswe &= -17;
                    }

                    if (ix < width - 1 && iy > 0 && (inside[ix + 1][iy - 1] || inside[ix][iy - 1] || inside[ix + 1][iy])) {
                        nswe &= -65;
                    }

                    if (ix > 0 && iy < height - 1 && (inside[ix - 1][iy + 1] || inside[ix][iy + 1] || inside[ix - 1][iy])) {
                        nswe &= -33;
                    }

                    if (ix > 0 && iy > 0 && (inside[ix - 1][iy - 1] || inside[ix][iy - 1] || inside[ix - 1][iy])) {
                        nswe = (byte) (nswe & 127);
                    }

                    result[ix][iy] = nswe;
                }
            }
        }

        return result;
    }

    private static byte getDirXY(byte dirX, byte dirY) {
        if (dirY == 8) {
            return (byte) (dirX == 2 ? -128 : 64);
        } else {
            return (byte) (dirX == 2 ? 32 : 16);
        }
    }

    public static GeoEngine getInstance() {
        return GeoEngine.SingletonHolder.INSTANCE;
    }

    private final NodeBuffer getBuffer(int size, boolean playable) {
        NodeBuffer current = null;
        GeoEngine.BufferHolder[] var4 = this._buffers;
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            GeoEngine.BufferHolder holder = var4[var6];
            if (holder._size >= size) {
                Iterator var8 = holder._buffer.iterator();

                while (var8.hasNext()) {
                    NodeBuffer buffer = (NodeBuffer) var8.next();
                    if (buffer.isLocked()) {
                        ++holder._uses;
                        if (playable) {
                            ++holder._playableUses;
                        }

                        holder._elapsed += buffer.getElapsedTime();
                        return buffer;
                    }
                }

                current = new NodeBuffer(holder._size);
                current.isLocked();
                ++holder._overflows;
                if (playable) {
                    ++holder._playableOverflows;
                }
            }
        }

        return current;
    }

    private final boolean loadGeoBlocks(int regionX, int regionY) {
        String filename = String.format(GeoType.L2D.getFilename(), regionX, regionY);
        String filepath = Config.GEODATA_PATH + filename;

        try {
            RandomAccessFile raf = new RandomAccessFile(filepath, "r");

            boolean var18;
            try {
                FileChannel fc = raf.getChannel();

                try {
                    MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, 0L, fc.size()).load();
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    int blockX = (regionX - 16) * 256;
                    int blockY = (regionY - 10) * 256;
                    int ix = 0;

                    while (true) {
                        if (ix >= 256) {
                            if (buffer.remaining() > 0) {
                                LOGGER.warn("Region file {} can be corrupted, remaining {} bytes to read.", filename, buffer.remaining());
                            }

                            var18 = true;
                            break;
                        }

                        for (int iy = 0; iy < 256; ++iy) {
                            byte type = buffer.get();
                            switch (type) {
                                case -48:
                                    this._blocks[blockX + ix][blockY + iy] = new BlockFlat(buffer, GeoType.L2D);
                                    break;
                                case -47:
                                    this._blocks[blockX + ix][blockY + iy] = new BlockComplex(buffer, GeoType.L2D);
                                    break;
                                case -46:
                                    this._blocks[blockX + ix][blockY + iy] = new BlockMultilayer(buffer, GeoType.L2D);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown block type: " + type);
                            }
                        }

                        ++ix;
                    }
                } catch (Throwable var15) {
                    if (fc != null) {
                        try {
                            fc.close();
                        } catch (Throwable var14) {
                            var15.addSuppressed(var14);
                        }
                    }

                    throw var15;
                }

                if (fc != null) {
                    fc.close();
                }
            } catch (Throwable var16) {
                try {
                    raf.close();
                } catch (Throwable var13) {
                    var16.addSuppressed(var13);
                }

                throw var16;
            }

            raf.close();
            return var18;
        } catch (Exception var17) {
            LOGGER.error("Error loading {} region file.", var17, filename);
            this.loadNullBlocks(regionX, regionY);
            return false;
        }
    }

    private final void loadNullBlocks(int regionX, int regionY) {
        int blockX = (regionX - 16) * 256;
        int blockY = (regionY - 10) * 256;

        for (int ix = 0; ix < 256; ++ix) {
            for (int iy = 0; iy < 256; ++iy) {
                this._blocks[blockX + ix][blockY + iy] = this._nullBlock;
            }
        }

    }

    private final short getHeightNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getBlock(geoX, geoY).getHeightNearestOriginal(geoX, geoY, worldZ);
    }

    private final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ) {
        return this.getBlock(geoX, geoY).getNsweNearestOriginal(geoX, geoY, worldZ);
    }

    public final ABlock getBlock(int geoX, int geoY) {
        return this._blocks[geoX / 8][geoY / 8];
    }

    public final boolean hasGeoPos(int geoX, int geoY) {
        return this.getBlock(geoX, geoY).hasGeoPos();
    }

    public final short getHeightNearest(int geoX, int geoY, int worldZ) {
        return this.getBlock(geoX, geoY).getHeightNearest(geoX, geoY, worldZ);
    }

    public final byte getNsweNearest(int geoX, int geoY, int worldZ) {
        return this.getBlock(geoX, geoY).getNsweNearest(geoX, geoY, worldZ);
    }

    public final boolean hasGeo(int worldX, int worldY) {
        return this.hasGeoPos(getGeoX(worldX), getGeoY(worldY));
    }

    public final short getHeight(int worldX, int worldY, int worldZ) {
        return this.getHeightNearest(getGeoX(worldX), getGeoY(worldY), worldZ);
    }

    public final void addGeoObject(IGeoObject object) {
        this.toggleGeoObject(object, true);
    }

    public final void removeGeoObject(IGeoObject object) {
        this.toggleGeoObject(object, false);
    }

    private final void toggleGeoObject(IGeoObject object, boolean add) {
        int minGX = object.getGeoX();
        int minGY = object.getGeoY();
        byte[][] geoData = object.getObjectGeoData();
        int minBX = minGX / 8;
        int maxBX = (minGX + geoData.length - 1) / 8;
        int minBY = minGY / 8;
        int maxBY = (minGY + geoData[0].length - 1) / 8;

        for (int bx = minBX; bx <= maxBX; ++bx) {
            for (int by = minBY; by <= maxBY; ++by) {
                Object block;
                synchronized (this._blocks) {
                    block = this._blocks[bx][by];
                    if (!(block instanceof IBlockDynamic)) {
                        if (block instanceof BlockNull) {
                            continue;
                        }

                        if (block instanceof BlockFlat) {
                            block = new BlockComplexDynamic(bx, by, (BlockFlat) block);
                            this._blocks[bx][by] = (ABlock) block;
                        } else if (block instanceof BlockComplex) {
                            block = new BlockComplexDynamic(bx, by, (BlockComplex) block);
                            this._blocks[bx][by] = (ABlock) block;
                        } else if (block instanceof BlockMultilayer) {
                            block = new BlockMultilayerDynamic(bx, by, (BlockMultilayer) block);
                            this._blocks[bx][by] = (ABlock) block;
                        }
                    }
                }

                if (add) {
                    ((IBlockDynamic) block).addGeoObject(object);
                } else {
                    ((IBlockDynamic) block).removeGeoObject(object);
                }
            }
        }

    }

    public final boolean canSeeTarget(WorldObject origin, WorldObject target) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int tx = target.getX();
        int ty = target.getY();
        int tz = target.getZ();
        int gox = getGeoX(ox);
        int goy = getGeoY(oy);
        if (!this.hasGeoPos(gox, goy)) {
            return true;
        } else {
            short goz = this.getHeightNearest(gox, goy, oz);
            int gtx = getGeoX(tx);
            int gty = getGeoY(ty);
            if (!this.hasGeoPos(gtx, gty)) {
                return true;
            } else {
                boolean door = target instanceof Door;
                short gtz = door ? this.getHeightNearestOriginal(gtx, gty, tz) : this.getHeightNearest(gtx, gty, tz);
                if (gox == gtx && goy == gty) {
                    return goz == gtz;
                } else {
                    double oheight = 0.0D;
                    if (origin instanceof Creature) {
                        oheight = ((Creature) origin).getCollisionHeight() * 2.0D;
                    }

                    double theight = 0.0D;
                    if (target instanceof Creature) {
                        theight = ((Creature) target).getCollisionHeight() * 2.0D;
                    }

                    return door ? this.checkSeeOriginal(gox, goy, goz, oheight, gtx, gty, gtz, theight) : this.checkSee(gox, goy, goz, oheight, gtx, gty, gtz, theight);
                }
            }
        }
    }

    public final boolean canSeeTarget(WorldObject origin, Location position) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int tx = position.getX();
        int ty = position.getY();
        int tz = position.getZ();
        int gox = getGeoX(ox);
        int goy = getGeoY(oy);
        if (!this.hasGeoPos(gox, goy)) {
            return true;
        } else {
            short goz = this.getHeightNearest(gox, goy, oz);
            int gtx = getGeoX(tx);
            int gty = getGeoY(ty);
            if (!this.hasGeoPos(gtx, gty)) {
                return true;
            } else {
                short gtz = this.getHeightNearest(gtx, gty, tz);
                if (gox == gtx && goy == gty) {
                    return goz == gtz;
                } else {
                    double oheight = 0.0D;
                    if (origin instanceof Creature) {
                        oheight = ((Creature) origin).getTemplate().getCollisionHeight();
                    }

                    return this.checkSee(gox, goy, goz, oheight, gtx, gty, gtz, 0.0D);
                }
            }
        }
    }

    protected final boolean checkSee(int gox, int goy, int goz, double oheight, int gtx, int gty, int gtz, double theight) {
        double losoz = (double) goz + oheight * (double) Config.PART_OF_CHARACTER_HEIGHT / 100.0D;
        double lostz = (double) gtz + theight * (double) Config.PART_OF_CHARACTER_HEIGHT / 100.0D;
        int dx = Math.abs(gtx - gox);
        int sx = gox < gtx ? 1 : -1;
        byte dirox = (byte) (sx > 0 ? 1 : 2);
        byte dirtx = (byte) (sx > 0 ? 2 : 1);
        int dy = Math.abs(gty - goy);
        int sy = goy < gty ? 1 : -1;
        byte diroy = (byte) (sy > 0 ? 4 : 8);
        byte dirty = (byte) (sy > 0 ? 8 : 4);
        int dm = Math.max(dx, dy);
        double dz = (lostz - losoz) / (double) dm;
        byte diroxy = getDirXY(dirox, diroy);
        byte dirtxy = getDirXY(dirtx, dirty);
        int d = dx - dy;
        int nox = gox;
        int noy = goy;
        int ntx = gtx;
        int nty = gty;
        byte nsweo = this.getNsweNearest(gox, goy, goz);
        byte nswet = this.getNsweNearest(gtx, gty, gtz);

        for (int i = 0; i < (dm + 1) / 2; ++i) {
            byte diro = 0;
            byte dirt = 0;
            int e2 = 2 * d;
            if (e2 > -dy && e2 < dx) {
                d -= dy;
                d += dx;
                nox += sx;
                ntx -= sx;
                noy += sy;
                nty -= sy;
                diro |= diroxy;
                dirt |= dirtxy;
            } else if (e2 > -dy) {
                d -= dy;
                nox += sx;
                ntx -= sx;
                diro = (byte) (diro | dirox);
                dirt = (byte) (dirt | dirtx);
            } else if (e2 < dx) {
                d += dx;
                noy += sy;
                nty -= sy;
                diro = (byte) (diro | diroy);
                dirt = (byte) (dirt | dirty);
            }

            ABlock block = this.getBlock(nox, noy);
            int index;
            if ((nsweo & diro) == 0) {
                index = block.getIndexAbove(nox, noy, goz - 48);
            } else {
                index = block.getIndexBelow(nox, noy, goz + 48);
            }

            if (index == -1) {
                return false;
            }

            goz = block.getHeight(index);
            losoz += dz;
            if ((double) goz - losoz > (double) Config.MAX_OBSTACLE_HEIGHT) {
                return false;
            }

            nsweo = block.getNswe(index);
            block = this.getBlock(ntx, nty);
            if ((nswet & dirt) == 0) {
                index = block.getIndexAbove(ntx, nty, gtz - 48);
            } else {
                index = block.getIndexBelow(ntx, nty, gtz + 48);
            }

            if (index == -1) {
                return false;
            }

            gtz = block.getHeight(index);
            lostz -= dz;
            if ((double) gtz - lostz > (double) Config.MAX_OBSTACLE_HEIGHT) {
                return false;
            }

            nswet = block.getNswe(index);
        }

        return Math.abs(goz - gtz) < 32;
    }

    protected final boolean checkSeeOriginal(int gox, int goy, int goz, double oheight, int gtx, int gty, int gtz, double theight) {
        double losoz = (double) goz + oheight * (double) Config.PART_OF_CHARACTER_HEIGHT / 100.0D;
        double lostz = (double) gtz + theight * (double) Config.PART_OF_CHARACTER_HEIGHT / 100.0D;
        int dx = Math.abs(gtx - gox);
        int sx = gox < gtx ? 1 : -1;
        byte dirox = (byte) (sx > 0 ? 1 : 2);
        byte dirtx = (byte) (sx > 0 ? 2 : 1);
        int dy = Math.abs(gty - goy);
        int sy = goy < gty ? 1 : -1;
        byte diroy = (byte) (sy > 0 ? 4 : 8);
        byte dirty = (byte) (sy > 0 ? 8 : 4);
        int dm = Math.max(dx, dy);
        double dz = (lostz - losoz) / (double) dm;
        byte diroxy = getDirXY(dirox, diroy);
        byte dirtxy = getDirXY(dirtx, dirty);
        int d = dx - dy;
        int nox = gox;
        int noy = goy;
        int ntx = gtx;
        int nty = gty;
        byte nsweo = this.getNsweNearestOriginal(gox, goy, goz);
        byte nswet = this.getNsweNearestOriginal(gtx, gty, gtz);

        for (int i = 0; i < (dm + 1) / 2; ++i) {
            byte diro = 0;
            byte dirt = 0;
            int e2 = 2 * d;
            if (e2 > -dy && e2 < dx) {
                d -= dy;
                d += dx;
                nox += sx;
                ntx -= sx;
                noy += sy;
                nty -= sy;
                diro |= diroxy;
                dirt |= dirtxy;
            } else if (e2 > -dy) {
                d -= dy;
                nox += sx;
                ntx -= sx;
                diro = (byte) (diro | dirox);
                dirt = (byte) (dirt | dirtx);
            } else if (e2 < dx) {
                d += dx;
                noy += sy;
                nty -= sy;
                diro = (byte) (diro | diroy);
                dirt = (byte) (dirt | dirty);
            }

            ABlock block = this.getBlock(nox, noy);
            int index;
            if ((nsweo & diro) == 0) {
                index = block.getIndexAboveOriginal(nox, noy, goz - 48);
            } else {
                index = block.getIndexBelowOriginal(nox, noy, goz + 48);
            }

            if (index == -1) {
                return false;
            }

            goz = block.getHeightOriginal(index);
            losoz += dz;
            if ((double) goz - losoz > (double) Config.MAX_OBSTACLE_HEIGHT) {
                return false;
            }

            nsweo = block.getNsweOriginal(index);
            block = this.getBlock(ntx, nty);
            if ((nswet & dirt) == 0) {
                index = block.getIndexAboveOriginal(ntx, nty, gtz - 48);
            } else {
                index = block.getIndexBelowOriginal(ntx, nty, gtz + 48);
            }

            if (index == -1) {
                return false;
            }

            gtz = block.getHeightOriginal(index);
            lostz -= dz;
            if ((double) gtz - lostz > (double) Config.MAX_OBSTACLE_HEIGHT) {
                return false;
            }

            nswet = block.getNsweOriginal(index);
        }

        return Math.abs(goz - gtz) < 32;
    }

    public final boolean canMoveToTarget(int ox, int oy, int oz, int tx, int ty, int tz) {
        int gox = getGeoX(ox);
        int goy = getGeoY(oy);
        if (!this.hasGeoPos(gox, goy)) {
            return true;
        } else {
            short goz = this.getHeightNearest(gox, goy, oz);
            int gtx = getGeoX(tx);
            int gty = getGeoY(ty);
            if (!this.hasGeoPos(gtx, gty)) {
                return true;
            } else {
                short gtz = this.getHeightNearest(gtx, gty, tz);
                if (gox == gtx && goy == gty && goz == gtz) {
                    return true;
                } else {
                    GeoLocation loc = this.checkMove(gox, goy, goz, gtx, gty, gtz);
                    return loc.getGeoX() == gtx && loc.getGeoY() == gty;
                }
            }
        }
    }

    public final Location canMoveToTargetLoc(int ox, int oy, int oz, int tx, int ty, int tz) {
        int gox = getGeoX(ox);
        int goy = getGeoY(oy);
        if (!this.hasGeoPos(gox, goy)) {
            return new Location(tx, ty, tz);
        } else {
            short goz = this.getHeightNearest(gox, goy, oz);
            int gtx = getGeoX(tx);
            int gty = getGeoY(ty);
            if (!this.hasGeoPos(gtx, gty)) {
                return new Location(tx, ty, tz);
            } else {
                short gtz = this.getHeightNearest(gtx, gty, tz);
                return gox == gtx && goy == gty && goz == gtz ? new Location(tx, ty, tz) : this.checkMove(gox, goy, goz, gtx, gty, gtz);
            }
        }
    }

    protected final GeoLocation checkMove(int gox, int goy, int goz, int gtx, int gty, int gtz) {
        int dx = Math.abs(gtx - gox);
        int sx = gox < gtx ? 1 : -1;
        byte dirX = (byte) (sx > 0 ? 1 : 2);
        int dy = Math.abs(gty - goy);
        int sy = goy < gty ? 1 : -1;
        byte dirY = (byte) (sy > 0 ? 4 : 8);
        byte dirXY = getDirXY(dirX, dirY);
        int d = dx - dy;
        int gpx = gox;
        int gpy = goy;
        int gpz = goz;
        int nx = gox;
        int ny = goy;

        do {
            byte direction = 0;
            int e2 = 2 * d;
            if (e2 > -dy && e2 < dx) {
                d -= dy;
                d += dx;
                nx += sx;
                ny += sy;
                direction |= dirXY;
            } else if (e2 > -dy) {
                d -= dy;
                nx += sx;
                direction = (byte) (direction | dirX);
            } else if (e2 < dx) {
                d += dx;
                ny += sy;
                direction = (byte) (direction | dirY);
            }

            if ((this.getNsweNearest(gpx, gpy, gpz) & direction) == 0) {
                return new GeoLocation(gpx, gpy, gpz);
            }

            gpx = nx;
            gpy = ny;
            gpz = this.getHeightNearest(nx, ny, gpz);
        } while (nx != gtx || ny != gty);

        if (gpz == gtz) {
            return new GeoLocation(gtx, gty, gtz);
        } else {
            return new GeoLocation(gox, goy, goz);
        }
    }

    public List<Location> findPath(int ox, int oy, int oz, int tx, int ty, int tz, boolean playable) {
        int gox = getGeoX(ox);
        int goy = getGeoY(oy);
        if (!this.hasGeoPos(gox, goy)) {
            return null;
        } else {
            short goz = this.getHeightNearest(gox, goy, oz);
            int gtx = getGeoX(tx);
            int gty = getGeoY(ty);
            if (!this.hasGeoPos(gtx, gty)) {
                return null;
            } else {
                short gtz = this.getHeightNearest(gtx, gty, tz);
                NodeBuffer buffer = this.getBuffer(64 + 2 * Math.max(Math.abs(gox - gtx), Math.abs(goy - gty)), playable);
                if (buffer == null) {
                    return null;
                } else {
                    boolean debug = playable && Config.DEBUG_PATH;
                    if (debug) {
                        this.clearDebugItems();
                    }

                    List path = null;

                    try {
                        Iterator var18;
                        try {
                            Node result = buffer.findPath(gox, goy, goz, gtx, gty, gtz);
                            if (result == null) {
                                ++this._findFails;
                                var18 = null;
                                return (List<Location>) var18;
                            }

                            if (debug) {
                                this.dropDebugItem(728, 0, new GeoLocation(gox, goy, goz));
                                var18 = buffer.debugPath().iterator();

                                while (var18.hasNext()) {
                                    Node n = (Node) var18.next();
                                    if (n.getCost() < 0.0D) {
                                        this.dropDebugItem(1831, (int) (-n.getCost() * 10.0D), n.getLoc());
                                    } else {
                                        this.dropDebugItem(57, (int) (n.getCost() * 10.0D), n.getLoc());
                                    }
                                }
                            }

                            path = constructPath(result);
                        } catch (Exception var28) {
                            LOGGER.error("Failed to generate a path.", var28);
                            ++this._findFails;
                            var18 = null;
                            return (List<Location>) var18;
                        }
                    } finally {
                        buffer.free();
                        ++this._findSuccess;
                    }

                    if (path.size() < 3) {
                        return path;
                    } else {
                        long timeStamp = System.currentTimeMillis();
                        ++this._postFilterUses;
                        if (playable) {
                            ++this._postFilterPlayableUses;
                        }

                        ListIterator<Location> point = path.listIterator();
                        int nodeAx = gox;
                        int nodeAy = goy;
                        short nodeAz = goz;

                        for (GeoLocation nodeB = (GeoLocation) point.next(); point.hasNext(); nodeB = (GeoLocation) point.next()) {
                            GeoLocation nodeC = (GeoLocation) path.get(point.nextIndex());
                            GeoLocation loc = this.checkMove(nodeAx, nodeAy, nodeAz, nodeC.getGeoX(), nodeC.getGeoY(), nodeC.getZ());
                            if (loc.getGeoX() == nodeC.getGeoX() && loc.getGeoY() == nodeC.getGeoY()) {
                                point.remove();
                                if (debug) {
                                    this.dropDebugItem(735, 0, nodeB);
                                }
                            } else {
                                nodeAx = nodeB.getGeoX();
                                nodeAy = nodeB.getGeoY();
                                nodeAz = (short) nodeB.getZ();
                            }
                        }

                        if (debug) {
                            Iterator var32 = path.iterator();

                            while (var32.hasNext()) {
                                Location node = (Location) var32.next();
                                this.dropDebugItem(65, 0, node);
                            }
                        }

                        this._postFilterElapsed += System.currentTimeMillis() - timeStamp;
                        return path;
                    }
                }
            }
        }
    }

    public List<String> getStat() {
        List<String> list = new ArrayList();
        GeoEngine.BufferHolder[] var2 = this._buffers;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            GeoEngine.BufferHolder buffer = var2[var4];
            list.add(buffer.toString());
        }

        String var10001 = String.valueOf(this._postFilterPlayableUses);
        list.add("Use: playable=" + var10001 + " non-playable=" + (this._postFilterUses - this._postFilterPlayableUses));
        if (this._postFilterUses > 0) {
            var10001 = String.valueOf(this._postFilterElapsed);
            list.add("Time (ms): total=" + var10001 + " avg=" + String.format("%1.2f", (double) this._postFilterElapsed / (double) this._postFilterUses));
        }

        var10001 = String.valueOf(this._findSuccess);
        list.add("Pathfind: success=" + var10001 + ", fail=" + this._findFails);
        return list;
    }

    public final boolean addGeoBug(Location loc, String comment) {
        int gox = getGeoX(loc.getX());
        int goy = getGeoY(loc.getY());
        int goz = loc.getZ();
        int rx = gox / 2048 + 16;
        int ry = goy / 2048 + 10;
        int bx = gox / 8 % 256;
        int by = goy / 8 % 256;
        int cx = gox % 8;
        int cy = goy % 8;

        try {
            this._geoBugReports.printf("%d;%d;%d;%d;%d;%d;%d;%s\r\n", rx, ry, bx, by, cx, cy, goz, comment.replace(";", ":"));
            return true;
        } catch (Exception var13) {
            LOGGER.error("Couldn't save new entry to \"geo_bugs.txt\" file.", var13);
            return false;
        }
    }

    public final void dropDebugItem(int id, int count, Location loc) {
        ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), id);
        item.setCount(count);
        item.spawnMe(loc);
        this._debugItems.add(item);
    }

    public final void clearDebugItems() {
        Iterator var1 = this._debugItems.iterator();

        while (var1.hasNext()) {
            ItemInstance item = (ItemInstance) var1.next();
            item.decayMe();
        }

        this._debugItems.clear();
    }

    private static final class BufferHolder {
        final int _size;
        final int _count;
        ArrayList<NodeBuffer> _buffer;
        int _playableUses = 0;
        int _uses = 0;
        int _playableOverflows = 0;
        int _overflows = 0;
        long _elapsed = 0L;

        public BufferHolder(int size, int count) {
            this._size = size;
            this._count = count;
            this._buffer = new ArrayList(count);

            for (int i = 0; i < count; ++i) {
                this._buffer.add(new NodeBuffer(size));
            }

        }

        public String toString() {
            StringBuilder sb = new StringBuilder(100);
            StringUtil.append(sb, "Buffer ", String.valueOf(this._size), "x", String.valueOf(this._size), ": count=", String.valueOf(this._count), " uses=", String.valueOf(this._playableUses), "/", String.valueOf(this._uses));
            if (this._uses > 0) {
                StringUtil.append(sb, " total/avg(ms)=", String.valueOf(this._elapsed), "/", String.format("%1.2f", (double) this._elapsed / (double) this._uses));
            }

            StringUtil.append(sb, " ovf=", String.valueOf(this._playableOverflows), "/", String.valueOf(this._overflows));
            return sb.toString();
        }
    }

    private static class SingletonHolder {
        protected static final GeoEngine INSTANCE = new GeoEngine();
    }
}