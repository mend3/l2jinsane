/**/
package net.sf.l2j.gameserver.geoengine.geodata;

public final class GeoStructure {
    public static final byte CELL_FLAG_E = 1;
    public static final byte CELL_FLAG_W = 2;
    public static final byte CELL_FLAG_S = 4;
    public static final byte CELL_FLAG_N = 8;
    public static final byte CELL_FLAG_SE = 16;
    public static final byte CELL_FLAG_SW = 32;
    public static final byte CELL_FLAG_NE = 64;
    public static final byte CELL_FLAG_NW = -128;
    public static final byte CELL_FLAG_S_AND_E = 5;
    public static final byte CELL_FLAG_S_AND_W = 6;
    public static final byte CELL_FLAG_N_AND_E = 9;
    public static final byte CELL_FLAG_N_AND_W = 10;
    public static final int CELL_SIZE = 16;
    public static final int CELL_HEIGHT = 8;
    public static final int CELL_IGNORE_HEIGHT = 48;
    public static final byte TYPE_FLAT_L2J_L2OFF = 0;
    public static final byte TYPE_FLAT_L2D = -48;
    public static final byte TYPE_COMPLEX_L2J = 1;
    public static final byte TYPE_COMPLEX_L2OFF = 64;
    public static final byte TYPE_COMPLEX_L2D = -47;
    public static final byte TYPE_MULTILAYER_L2J = 2;
    public static final byte TYPE_MULTILAYER_L2D = -46;
    public static final int BLOCK_CELLS_X = 8;
    public static final int BLOCK_CELLS_Y = 8;
    public static final int BLOCK_CELLS = 64;
    public static final int REGION_BLOCKS_X = 256;
    public static final int REGION_BLOCKS_Y = 256;
    public static final int REGION_BLOCKS = 65536;
    public static final int REGION_CELLS_X = 2048;
    public static final int REGION_CELLS_Y = 2048;
    public static final int GEO_REGIONS_X = 11;
    public static final int GEO_REGIONS_Y = 16;
    public static final int GEO_BLOCKS_X = 2816;
    public static final int GEO_BLOCKS_Y = 4096;
    public static final int GEO_CELLS_X = 22528;
    public static final int GEO_CELLS_Y = 32768;
}