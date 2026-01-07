/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.geoengine.geodata.IGeoObject;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExColosseumFenceInfo;

public class Fence extends WorldObject implements IGeoObject {
    private static final int FENCE_HEIGHT = 24;
    private final int _type;
    private final int _sizeX;
    private final int _sizeY;
    private final int _height;
    private final Fence.L2DummyFence _object2;
    private final Fence.L2DummyFence _object3;
    private final int _geoX;
    private final int _geoY;
    private final int _geoZ;
    private final byte[][] _geoData;

    public Fence(int type, int sizeZ, int sizeY, int height, int geoX, int geoY, int geoZ, byte[][] geoData) {
        super(IdFactory.getInstance().getNextId());
        this._type = type;
        this._sizeX = sizeZ;
        this._sizeY = sizeY;
        this._height = height * 24;
        this._object2 = height > 1 ? new L2DummyFence(this, this) : null;
        this._object3 = height > 2 ? new L2DummyFence(this, this) : null;
        this._geoX = geoX;
        this._geoY = geoY;
        this._geoZ = geoZ;
        this._geoData = geoData;
    }

    public int getType() {
        return this._type;
    }

    public int getSizeX() {
        return this._sizeX;
    }

    public int getSizeY() {
        return this._sizeY;
    }

    public void onSpawn() {
        super.onSpawn();
        if (this._object2 != null) {
            this._object2.spawnMe(this.getPosition());
        }

        if (this._object3 != null) {
            this._object3.spawnMe(this.getPosition());
        }

    }

    public void decayMe() {
        if (this._object2 != null) {
            this._object2.decayMe();
        }

        if (this._object3 != null) {
            this._object3.decayMe();
        }

        super.decayMe();
    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public void sendInfo(Player activeChar) {
        activeChar.sendPacket(new ExColosseumFenceInfo(this.getObjectId(), this));
    }

    public int getGeoX() {
        return this._geoX;
    }

    public int getGeoY() {
        return this._geoY;
    }

    public int getGeoZ() {
        return this._geoZ;
    }

    public int getHeight() {
        return this._height;
    }

    public byte[][] getObjectGeoData() {
        return this._geoData;
    }

    protected static class L2DummyFence extends WorldObject {
        private final Fence _fence;

        public L2DummyFence(final Fence fence, Fence fence2) {
            super(IdFactory.getInstance().getNextId());
            this._fence = fence;
        }

        public boolean isAutoAttackable(Creature attacker) {
            return false;
        }

        public void sendInfo(Player activeChar) {
            activeChar.sendPacket(new ExColosseumFenceInfo(this.getObjectId(), this._fence));
        }
    }
}