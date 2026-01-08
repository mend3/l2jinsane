package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Polygon extends AShape {
    private static final int TRIANGULATION_MAX_LOOPS = 100;

    protected final List<? extends AShape> _shapes;

    protected final int _size;

    public Polygon(List<? extends AShape> shapes) {
        this._shapes = shapes;
        int size = 0;
        for (AShape shape : shapes)
            size += shape.getSize();
        this._size = size;
    }

    public Polygon(int id, List<int[]> points) {
        List<Triangle> triangles = null;
        int size = 0;
        try {
            if (points.size() < 3)
                throw new IndexOutOfBoundsException("Can not create Polygon (id=" + id + ") from less than 3 coordinates.");
            boolean isCw = getPolygonOrientation(points);
            List<int[]> nonConvexPoints = calculateNonConvexPoints(points, isCw);
            triangles = doTriangulationAlgorithm(points, isCw, nonConvexPoints);
            for (AShape shape : triangles)
                size += shape.getSize();
        } catch (Exception e) {
            e.printStackTrace();
            triangles = new ArrayList<>();
        }
        this._shapes = triangles;
        this._size = size;
    }

    private static boolean getPolygonOrientation(List<int[]> points) {
        int size = points.size();
        int index = 0;
        int[] point = points.getFirst();
        for (int i = 1; i < size; i++) {
            int[] pt = points.get(i);
            if (pt[0] < point[0] || (pt[0] == point[0] && pt[1] > point[1])) {
                point = pt;
                index = i;
            }
        }
        int[] pointPrev = points.get(getPrevIndex(size, index));
        int[] pointNext = points.get(getNextIndex(size, index));
        int vx = point[0] - pointPrev[0];
        int vy = point[1] - pointPrev[1];
        int res = pointNext[0] * vy - pointNext[1] * vx + vx * pointPrev[1] - vy * pointPrev[0];
        return (res <= 0);
    }

    private static int getNextIndex(int size, int index) {
        if (++index >= size)
            return 0;
        return index;
    }

    private static int getPrevIndex(int size, int index) {
        if (--index < 0)
            return size - 1;
        return index;
    }

    private static List<int[]> calculateNonConvexPoints(List<int[]> points, boolean isCw) {
        List<int[]> nonConvexPoints = new ArrayList<>();
        int size = points.size();
        for (int i = 0; i < size - 1; i++) {
            int[] point = points.get(i);
            int[] pointNext = points.get(i + 1);
            int[] pointNextNext = points.get(getNextIndex(size, i + 2));
            int vx = pointNext[0] - point[0];
            int vy = pointNext[1] - point[1];
            boolean res = (pointNextNext[0] * vy - pointNextNext[1] * vx + vx * point[1] - vy * point[0] > 0);
            if (res == isCw)
                nonConvexPoints.add(pointNext);
        }
        return nonConvexPoints;
    }

    private static List<Triangle> doTriangulationAlgorithm(List<int[]> points, boolean isCw, List<int[]> nonConvexPoints) throws Exception {
        List<Triangle> triangles = new ArrayList<>();
        int size = points.size();
        int loops = 0;
        int index = 1;
        while (size > 3) {
            int indexPrev = getPrevIndex(size, index);
            int indexNext = getNextIndex(size, index);
            int[] pointPrev = points.get(indexPrev);
            int[] point = points.get(index);
            int[] pointNext = points.get(indexNext);
            if (isEar(isCw, nonConvexPoints, pointPrev, point, pointNext)) {
                triangles.add(new Triangle(pointPrev, point, pointNext));
                points.remove(index);
                size--;
                index = getPrevIndex(size, index);
            } else {
                index = indexNext;
            }
            if (++loops == 100)
                throw new Exception("Coordinates are not aligned to form monotone polygon.");
        }
        triangles.add(new Triangle(points.get(0), points.get(1), points.get(2)));
        return triangles;
    }

    private static boolean isEar(boolean isCw, List<int[]> nonConvexPoints, int[] A, int[] B, int[] C) {
        if (!isConvex(isCw, A, B, C))
            return false;
        for (int[] nonConvexPoint : nonConvexPoints) {
            if (isInside(A, B, C, nonConvexPoint))
                return false;
        }
        return true;
    }

    private static boolean isConvex(boolean isCw, int[] A, int[] B, int[] C) {
        int BAx = B[0] - A[0];
        int BAy = B[1] - A[1];
        boolean cw = (C[0] * BAy - C[1] * BAx + BAx * A[1] - BAy * A[0] > 0);
        return (cw != isCw);
    }

    private static boolean isInside(int[] A, int[] B, int[] C, int[] P) {
        int BAx = B[0] - A[0];
        int BAy = B[1] - A[1];
        int CAx = C[0] - A[0];
        int CAy = C[1] - A[1];
        int PAx = P[0] - A[0];
        int PAy = P[1] - A[1];
        double detXYZ = (BAx * CAy - CAx * BAy);
        double ba = (BAx * PAy - PAx * BAy) / detXYZ;
        double ca = (PAx * CAy - CAx * PAy) / detXYZ;
        return (ba > 0.0D && ca > 0.0D && ba + ca < 1.0D);
    }

    public int getSize() {
        return this._size;
    }

    public double getArea() {
        return -1.0D;
    }

    public double getVolume() {
        return -1.0D;
    }

    public boolean isInside(int x, int y) {
        for (AShape shape : this._shapes) {
            if (shape.isInside(x, y))
                return true;
        }
        return false;
    }

    public boolean isInside(int x, int y, int z) {
        for (AShape shape : this._shapes) {
            if (shape.isInside(x, y, z))
                return true;
        }
        return false;
    }

    public Location getRandomLocation() {
        int size = Rnd.get(this._size);
        for (AShape shape : this._shapes) {
            size -= shape.getSize();
            if (size < 0)
                return shape.getRandomLocation();
        }
        return null;
    }
}
