package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class ValidateLocation extends L2GameServerPacket {
    private final int _charObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _heading;

    public ValidateLocation(Creature cha) {
        this._charObjId = cha.getObjectId();
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
        this._heading = cha.getHeading();
    }

    protected final void writeImpl() {
        writeC(97);
        writeD(this._charObjId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._heading);
    }
}
