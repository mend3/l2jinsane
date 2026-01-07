package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class StopMove extends L2GameServerPacket {
    private final int _objectId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _heading;

    public StopMove(Creature cha) {
        this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
    }

    public StopMove(int objectId, int x, int y, int z, int heading) {
        this._objectId = objectId;
        this._x = x;
        this._y = y;
        this._z = z;
        this._heading = heading;
    }

    protected final void writeImpl() {
        writeC(71);
        writeD(this._objectId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._heading);
    }
}
