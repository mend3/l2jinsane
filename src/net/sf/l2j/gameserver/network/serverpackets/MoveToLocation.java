package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public final class MoveToLocation extends L2GameServerPacket {
    private final int _charObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _xDst;

    private final int _yDst;

    private final int _zDst;

    public MoveToLocation(Creature cha) {
        this._charObjId = cha.getObjectId();
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
        this._xDst = cha.getXdestination();
        this._yDst = cha.getYdestination();
        this._zDst = cha.getZdestination();
    }

    protected void writeImpl() {
        writeC(1);
        writeD(this._charObjId);
        writeD(this._xDst);
        writeD(this._yDst);
        writeD(this._zDst);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
