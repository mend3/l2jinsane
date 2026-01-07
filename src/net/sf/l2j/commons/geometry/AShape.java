package net.sf.l2j.commons.geometry;

import net.sf.l2j.gameserver.model.location.Location;

public abstract class AShape {
    public abstract int getSize();

    public abstract double getArea();

    public abstract double getVolume();

    public abstract boolean isInside(int paramInt1, int paramInt2);

    public abstract boolean isInside(int paramInt1, int paramInt2, int paramInt3);

    public abstract Location getRandomLocation();
}
