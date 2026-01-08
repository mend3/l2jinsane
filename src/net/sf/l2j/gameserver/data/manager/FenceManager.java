/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.instance.Fence;

import java.util.ArrayList;
import java.util.List;

public class FenceManager {
    private static final CLogger LOGGER = new CLogger(FenceManager.class.getName());
    private final List<Fence> _fences = new ArrayList<>();

    protected FenceManager() {
    }

    private static FenceManager.FenceSize getFenceSize(int size) {
        if (size < 199) {
            return FenceManager.FenceSize.SIZE_100;
        } else if (size < 299) {
            return FenceManager.FenceSize.SIZE_200;
        } else if (size < 399) {
            return FenceManager.FenceSize.SIZE_300;
        } else if (size < 499) {
            return FenceManager.FenceSize.SIZE_400;
        } else if (size < 599) {
            return FenceManager.FenceSize.SIZE_500;
        } else if (size < 699) {
            return FenceManager.FenceSize.SIZE_600;
        } else if (size < 799) {
            return FenceManager.FenceSize.SIZE_700;
        } else if (size < 899) {
            return FenceManager.FenceSize.SIZE_800;
        } else if (size < 999) {
            return FenceManager.FenceSize.SIZE_900;
        } else {
            return size < 1099 ? FenceManager.FenceSize.SIZE_1000 : null;
        }
    }

    public static FenceManager getInstance() {
        return FenceManager.SingletonHolder.INSTANCE;
    }

    public final List<Fence> getFences() {
        return this._fences;
    }

    public final void addFence(int x, int y, int z, int type, int sizeX, int sizeY, int height) {
        FenceManager.FenceSize fsx = getFenceSize(sizeX);
        FenceManager.FenceSize fsy = getFenceSize(sizeY);
        if (fsx != null && fsy != null) {
            x &= -16 + fsx._offset;
            y &= -16 + fsy._offset;
            int sx = fsx._geoDataSize;
            int sy = fsy._geoDataSize;
            int geoX = GeoEngine.getGeoX(x) - sx / 2;
            int geoY = GeoEngine.getGeoY(y) - sy / 2;
            int geoZ = GeoEngine.getInstance().getHeight(x, y, z);
            boolean[][] inside = new boolean[sx][sy];

            for (int ix = 1; ix < sx - 1; ++ix) {
                for (int iy = 1; iy < sy - 1; ++iy) {
                    if (type == 2) {
                        inside[ix][iy] = ix < 3 || ix >= sx - 3 || iy < 3 || iy >= sy - 3;
                    } else {
                        inside[ix][iy] = (ix < 3 || ix >= sx - 3) && (iy < 3 || iy >= sy - 3);
                    }
                }
            }

            byte[][] geoData = GeoEngine.calculateGeoObject(inside);
            Fence fence = new Fence(type, sizeX, sizeY, height, geoX, geoY, geoZ, geoData);
            fence.spawnMe(x, y, z);
            GeoEngine.getInstance().addGeoObject(fence);
            this._fences.add(fence);
        } else {
            LOGGER.warn("Unknown dimensions for fence, x={} y={}.", sizeX, sizeY);
        }
    }

    public final void removeFence(Fence fence) {
        fence.decayMe();
        GeoEngine.getInstance().removeGeoObject(fence);
        this._fences.remove(fence);
    }

    private enum FenceSize {
        SIZE_100(8, 11),
        SIZE_200(0, 18),
        SIZE_300(0, 24),
        SIZE_400(0, 30),
        SIZE_500(0, 36),
        SIZE_600(0, 42),
        SIZE_700(8, 49),
        SIZE_800(8, 55),
        SIZE_900(8, 61),
        SIZE_1000(0, 68);

        final int _offset;
        final int _geoDataSize;

        FenceSize(int offset, int geoDataSize) {
            this._offset = offset;
            this._geoDataSize = geoDataSize;
        }
    }

    private static class SingletonHolder {
        protected static final FenceManager INSTANCE = new FenceManager();
    }
}