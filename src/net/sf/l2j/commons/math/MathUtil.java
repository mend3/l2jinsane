package net.sf.l2j.commons.math;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MathUtil {
    public static int countPagesNumber(int objectsSize, int pageSize) {
        return objectsSize / pageSize + ((objectsSize % pageSize == 0) ? 0 : 1);
    }

    public static int limit(int numToTest, int min, int max) {
        return (numToTest > max) ? max : ((numToTest < min) ? min : numToTest);
    }

    public static double calculateAngleFrom(WorldObject obj1, WorldObject obj2) {
        return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
    }

    public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
        double angleTarget = Math.toDegrees(Math.atan2((obj2Y - obj1Y), (obj2X - obj1X)));
        if (angleTarget < 0.0D)
            angleTarget = 360.0D + angleTarget;
        return angleTarget;
    }

    public static double convertHeadingToDegree(int clientHeading) {
        return clientHeading / 182.04444444444445D;
    }

    public static int convertDegreeToClientHeading(double degree) {
        if (degree < 0.0D)
            degree = 360.0D + degree;
        return (int) (degree * 182.04444444444445D);
    }

    public static int calculateHeadingFrom(WorldObject obj1, WorldObject obj2) {
        return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
    }

    public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
        double angleTarget = Math.toDegrees(Math.atan2((obj2Y - obj1Y), (obj2X - obj1X)));
        if (angleTarget < 0.0D)
            angleTarget = 360.0D + angleTarget;
        return (int) (angleTarget * 182.04444444444445D);
    }

    public static int calculateHeadingFrom(double dx, double dy) {
        double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
        if (angleTarget < 0.0D)
            angleTarget = 360.0D + angleTarget;
        return (int) (angleTarget * 182.04444444444445D);
    }

    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        return calculateDistance(x1, y1, 0, x2, y2, 0, false);
    }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        if (includeZAxis) {
            double dz = (z1 - z2);
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double calculateDistance(WorldObject obj1, WorldObject obj2, boolean includeZAxis) {
        if (obj1 == null || obj2 == null)
            return 1000000.0D;
        return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
    }

    public static boolean checkIfInShortRadius(int radius, WorldObject obj1, WorldObject obj2, boolean includeZAxis) {
        if (obj1 == null || obj2 == null)
            return false;
        if (radius == -1)
            return true;
        int dx = obj1.getX() - obj2.getX();
        int dy = obj1.getY() - obj2.getY();
        if (includeZAxis) {
            int dz = obj1.getZ() - obj2.getZ();
            return (dx * dx + dy * dy + dz * dz <= radius * radius);
        }
        return (dx * dx + dy * dy <= radius * radius);
    }

    public static boolean checkIfInRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis) {
        if (obj1 == null || obj2 == null)
            return false;
        if (range == -1)
            return true;
        double rad = 0.0D;
        if (obj1 instanceof Creature)
            rad += ((Creature) obj1).getCollisionRadius();
        if (obj2 instanceof Creature)
            rad += ((Creature) obj2).getCollisionRadius();
        double dx = (obj1.getX() - obj2.getX());
        double dy = (obj1.getY() - obj2.getY());
        if (includeZAxis) {
            double dz = (obj1.getZ() - obj2.getZ());
            double d1 = dx * dx + dy * dy + dz * dz;
            return (d1 <= (range * range) + (2 * range) * rad + rad * rad);
        }
        double d = dx * dx + dy * dy;
        return (d <= (range * range) + (2 * range) * rad + rad * rad);
    }

    public static float roundTo(float val, int numPlaces) {
        if (numPlaces <= 1)
            return Math.round(val);
        float exponent = (float) Math.pow(10.0D, numPlaces);
        return Math.round(val * exponent) / exponent;
    }

    public static String formatDate(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        if (date != null)
            return dateFormat.format(date);
        return null;
    }
}
