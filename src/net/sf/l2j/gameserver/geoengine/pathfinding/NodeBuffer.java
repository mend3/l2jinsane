/**/
package net.sf.l2j.gameserver.geoengine.pathfinding;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geoengine.GeoEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class NodeBuffer {
    private final ReentrantLock _lock = new ReentrantLock();
    private final int _size;
    private final Node[][] _buffer;
    private int _cx = 0;
    private int _cy = 0;
    private int _gtx = 0;
    private int _gty = 0;
    private short _gtz = 0;
    private long _timeStamp = 0L;
    private long _lastElapsedTime = 0L;
    private Node _current = null;

    public NodeBuffer(int size) {
        this._size = size;
        this._buffer = new Node[size][size];

        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                this._buffer[x][y] = new Node();
            }
        }

    }

    public final Node findPath(int gox, int goy, short goz, int gtx, int gty, short gtz) {
        this._timeStamp = System.currentTimeMillis();
        this._cx = gox + (gtx - gox - this._size) / 2;
        this._cy = goy + (gty - goy - this._size) / 2;
        this._gtx = gtx;
        this._gty = gty;
        this._gtz = gtz;
        this._current = this.getNode(gox, goy, goz);
        this._current.setCost(this.getCostH(gox, goy, goz));
        int count = 0;

        do {
            if (this._current.getLoc().getGeoX() == this._gtx && this._current.getLoc().getGeoY() == this._gty && Math.abs(this._current.getLoc().getZ() - this._gtz) < 8) {
                return this._current;
            }

            this.expand();
            this._current = this._current.getChild();
            if (this._current == null) {
                break;
            }

            ++count;
        } while (count < Config.MAX_ITERATIONS);

        return null;
    }

    public final List<Node> debugPath() {
        List<Node> result = new ArrayList<>();

        for (Node n = this._current; n.getParent() != null; n = n.getParent()) {
            result.add(n);
            n.setCost(-n.getCost());
        }

        Node[][] var10 = this._buffer;
        int var3 = var10.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Node[] nodes = var10[var4];
            Node[] var6 = nodes;
            int var7 = nodes.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                Node node = var6[var8];
                if (node.getLoc() != null && node.getCost() > 0.0D) {
                    result.add(node);
                }
            }
        }

        return result;
    }

    public final boolean isLocked() {
        return this._lock.tryLock();
    }

    public final void free() {
        this._current = null;
        Node[][] var1 = this._buffer;
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Node[] nodes = var1[var3];
            Node[] var5 = nodes;
            int var6 = nodes.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                Node node = var5[var7];
                if (node.getLoc() != null) {
                    node.free();
                }
            }
        }

        this._lock.unlock();
        this._lastElapsedTime = System.currentTimeMillis() - this._timeStamp;
    }

    public final long getElapsedTime() {
        return this._lastElapsedTime;
    }

    private final void expand() {
        byte nswe = this._current.getLoc().getNSWE();
        if (nswe != 0) {
            int x = this._current.getLoc().getGeoX();
            int y = this._current.getLoc().getGeoY();
            short z = (short) this._current.getLoc().getZ();
            if ((nswe & 8) != 0) {
                this.addNode(x, y - 1, z, Config.BASE_WEIGHT);
            }

            if ((nswe & 4) != 0) {
                this.addNode(x, y + 1, z, Config.BASE_WEIGHT);
            }

            if ((nswe & 2) != 0) {
                this.addNode(x - 1, y, z, Config.BASE_WEIGHT);
            }

            if ((nswe & 1) != 0) {
                this.addNode(x + 1, y, z, Config.BASE_WEIGHT);
            }

            if ((nswe & -128) != 0) {
                this.addNode(x - 1, y - 1, z, Config.DIAGONAL_WEIGHT);
            }

            if ((nswe & 64) != 0) {
                this.addNode(x + 1, y - 1, z, Config.DIAGONAL_WEIGHT);
            }

            if ((nswe & 32) != 0) {
                this.addNode(x - 1, y + 1, z, Config.DIAGONAL_WEIGHT);
            }

            if ((nswe & 16) != 0) {
                this.addNode(x + 1, y + 1, z, Config.DIAGONAL_WEIGHT);
            }

        }
    }

    private final Node getNode(int x, int y, short z) {
        int ix = x - this._cx;
        if (ix >= 0 && ix < this._size) {
            int iy = y - this._cy;
            if (iy >= 0 && iy < this._size) {
                Node result = this._buffer[ix][iy];
                if (result.getLoc() == null) {
                    result.setLoc(x, y, z);
                }

                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private final void addNode(int x, int y, short z, int weight) {
        Node node = this.getNode(x, y, z);
        if (node != null) {
            if (node.getLoc().getZ() > z + 16) {
                if (Config.DEBUG_GEO_NODE) {
                    GeoEngine.getInstance().addGeoBug(node.getLoc(), "NodeBufferDiag: Check Z coords.");
                }

            } else if (node.getCost() < 0.0D) {
                node.setParent(this._current);
                if (node.getLoc().getNSWE() != -1) {
                    node.setCost(this.getCostH(x, y, node.getLoc().getZ()) + (double) (weight * Config.OBSTACLE_MULTIPLIER));
                } else {
                    node.setCost(this.getCostH(x, y, node.getLoc().getZ()) + (double) weight);
                }

                Node current = this._current;

                int count;
                for (count = 0; current.getChild() != null && count < Config.MAX_ITERATIONS * 4; current = current.getChild()) {
                    ++count;
                    if (current.getChild().getCost() > node.getCost()) {
                        node.setChild(current.getChild());
                        break;
                    }
                }

                if (count >= Config.MAX_ITERATIONS * 4) {
                    System.err.println("Pathfinding: too long loop detected, cost:" + node.getCost());
                }

                current.setChild(node);
            }
        }
    }

    private final double getCostH(int x, int y, int i) {
        int dX = x - this._gtx;
        int dY = y - this._gty;
        int dZ = (i - this._gtz) / 8;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ) * (double) Config.HEURISTIC_WEIGHT;
    }
}