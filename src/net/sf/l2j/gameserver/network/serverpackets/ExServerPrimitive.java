package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.location.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExServerPrimitive extends L2GameServerPacket {
    private final String _name;

    private final List<Point> _points = new ArrayList<>();

    private final List<Line> _lines = new ArrayList<>();

    private int _x;

    private int _y;

    private int _z;

    public ExServerPrimitive(String name, int x, int y, int z) {
        this._name = name;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public ExServerPrimitive(String name, Location location) {
        this(name, location.getX(), location.getY(), location.getZ());
    }

    public ExServerPrimitive(String name) {
        this(name, 0, 0, 0);
    }

    public void setXYZ(int x, int y, int z) {
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public void setXYZ(Location location) {
        setXYZ(location.getX(), location.getY(), location.getZ());
    }

    public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z) {
        this._points.add(new Point(name, color, isNameColored, x, y, z));
    }

    public void addPoint(String name, int color, boolean isNameColored, Location location) {
        addPoint(name, color, isNameColored, location.getX(), location.getY(), location.getZ());
    }

    public void addPoint(int color, int x, int y, int z) {
        addPoint("", color, false, x, y, z);
    }

    public void addPoint(int color, Location location) {
        addPoint("", color, false, location);
    }

    public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z) {
        addPoint(name, color.getRGB(), isNameColored, x, y, z);
    }

    public void addPoint(String name, Color color, boolean isNameColored, Location location) {
        addPoint(name, color.getRGB(), isNameColored, location);
    }

    public void addPoint(Color color, int x, int y, int z) {
        addPoint("", color, false, x, y, z);
    }

    public void addPoint(Color color, Location location) {
        addPoint("", color, false, location);
    }

    public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
        this._lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
    }

    public void addLine(String name, int color, boolean isNameColored, Location location, int x2, int y2, int z2) {
        addLine(name, color, isNameColored, location.getX(), location.getY(), location.getZ(), x2, y2, z2);
    }

    public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, Location location) {
        addLine(name, color, isNameColored, x, y, z, location.getX(), location.getY(), location.getZ());
    }

    public void addLine(String name, int color, boolean isNameColored, Location location, Location location2) {
        addLine(name, color, isNameColored, location, location2.getX(), location2.getY(), location2.getZ());
    }

    public void addLine(int color, int x, int y, int z, int x2, int y2, int z2) {
        addLine("", color, false, x, y, z, x2, y2, z2);
    }

    public void addLine(int color, Location location, int x2, int y2, int z2) {
        addLine("", color, false, location, x2, y2, z2);
    }

    public void addLine(int color, int x, int y, int z, Location location) {
        addLine("", color, false, x, y, z, location);
    }

    public void addLine(int color, Location location, Location location2) {
        addLine("", color, false, location, location2);
    }

    public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
        addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
    }

    public void addLine(String name, Color color, boolean isNameColored, Location location, int x2, int y2, int z2) {
        addLine(name, color.getRGB(), isNameColored, location, x2, y2, z2);
    }

    public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, Location location) {
        addLine(name, color.getRGB(), isNameColored, x, y, z, location);
    }

    public void addLine(String name, Color color, boolean isNameColored, Location location, Location location2) {
        addLine(name, color.getRGB(), isNameColored, location, location2);
    }

    public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2) {
        addLine("", color, false, x, y, z, x2, y2, z2);
    }

    public void addLine(Color color, Location location, int x2, int y2, int z2) {
        addLine("", color, false, location, x2, y2, z2);
    }

    public void addLine(Color color, int x, int y, int z, Location location) {
        addLine("", color, false, x, y, z, location);
    }

    public void addLine(Color color, Location location, Location location2) {
        addLine("", color, false, location, location2);
    }

    protected void writeImpl() {
        writeC(254);
        writeH(36);
        writeS(this._name);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(2147483647);
        writeD(2147483647);
        writeD(this._points.size() + this._lines.size());
        for (Point point : this._points) {
            writeC(1);
            writeS(point.getName());
            int color = point.getColor();
            writeD(color >> 16 & 0xFF);
            writeD(color >> 8 & 0xFF);
            writeD(color & 0xFF);
            writeD(point.isNameColored() ? 1 : 0);
            writeD(point.getX());
            writeD(point.getY());
            writeD(point.getZ());
        }
        for (Line line : this._lines) {
            writeC(2);
            writeS(line.getName());
            int color = line.getColor();
            writeD(color >> 16 & 0xFF);
            writeD(color >> 8 & 0xFF);
            writeD(color & 0xFF);
            writeD(line.isNameColored() ? 1 : 0);
            writeD(line.getX());
            writeD(line.getY());
            writeD(line.getZ());
            writeD(line.getX2());
            writeD(line.getY2());
            writeD(line.getZ2());
        }
    }

    private static class Point {
        private final String _name;

        private final int _color;

        private final boolean _isNameColored;

        private final int _x;

        private final int _y;

        private final int _z;

        public Point(String name, int color, boolean isNameColored, int x, int y, int z) {
            this._name = name;
            this._color = color;
            this._isNameColored = isNameColored;
            this._x = x;
            this._y = y;
            this._z = z;
        }

        public String getName() {
            return this._name;
        }

        public int getColor() {
            return this._color;
        }

        public boolean isNameColored() {
            return this._isNameColored;
        }

        public int getX() {
            return this._x;
        }

        public int getY() {
            return this._y;
        }

        public int getZ() {
            return this._z;
        }
    }

    private static class Line extends Point {
        private final int _x2;

        private final int _y2;

        private final int _z2;

        public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
            super(name, color, isNameColored, x, y, z);
            this._x2 = x2;
            this._y2 = y2;
            this._z2 = z2;
        }

        public int getX2() {
            return this._x2;
        }

        public int getY2() {
            return this._y2;
        }

        public int getZ2() {
            return this._z2;
        }
    }
}
