/**/
package net.sf.l2j.gameserver.geoengine.geodata;

import java.io.BufferedOutputStream;
import java.io.IOException;

public abstract class ABlock {
    public abstract boolean hasGeoPos();

    public abstract short getHeightNearest(int var1, int var2, int var3);

    public abstract short getHeightNearestOriginal(int var1, int var2, int var3);

    public abstract short getHeightAbove(int var1, int var2, int var3);

    public abstract short getHeightBelow(int var1, int var2, int var3);

    public abstract byte getNsweNearest(int var1, int var2, int var3);

    public abstract byte getNsweNearestOriginal(int var1, int var2, int var3);

    public abstract byte getNsweAbove(int var1, int var2, int var3);

    public abstract byte getNsweBelow(int var1, int var2, int var3);

    public abstract int getIndexNearest(int var1, int var2, int var3);

    public abstract int getIndexAbove(int var1, int var2, int var3);

    public abstract int getIndexAboveOriginal(int var1, int var2, int var3);

    public abstract int getIndexBelow(int var1, int var2, int var3);

    public abstract int getIndexBelowOriginal(int var1, int var2, int var3);

    public abstract short getHeight(int var1);

    public abstract short getHeightOriginal(int var1);

    public abstract byte getNswe(int var1);

    public abstract byte getNsweOriginal(int var1);

    public abstract void setNswe(int var1, byte var2);

    public abstract void saveBlock(BufferedOutputStream var1) throws IOException;
}