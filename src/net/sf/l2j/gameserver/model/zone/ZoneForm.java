package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.awt.geom.Line2D;

public abstract class ZoneForm {
    protected static final int STEP = 50;

    protected static void dropDebugItem(int id, int x, int y, int z) {
        ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 57);
        item.setCount(id);
        item.spawnMe(x, y, z + 5);
        ZoneManager.getInstance().addDebugItem(item);
    }

    public abstract boolean isInsideZone(int var1, int var2, int var3);

    public abstract boolean intersectsRectangle(int var1, int var2, int var3, int var4);

    public abstract double getDistanceToZone(int var1, int var2);

    public abstract int getLowZ();

    public abstract int getHighZ();

    public abstract void visualizeZone(int var1, int var2);

    protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2) {
        return Line2D.linesIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
    }
}
