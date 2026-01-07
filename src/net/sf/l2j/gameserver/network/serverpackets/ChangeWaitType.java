package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class ChangeWaitType extends L2GameServerPacket {
    public static final int WT_SITTING = 0;

    public static final int WT_STANDING = 1;

    public static final int WT_START_FAKEDEATH = 2;

    public static final int WT_STOP_FAKEDEATH = 3;

    private final int _charObjId;

    private final int _moveType;

    private final int _x;

    private final int _y;

    private final int _z;

    public ChangeWaitType(Creature character, int newMoveType) {
        this._charObjId = character.getObjectId();
        this._moveType = newMoveType;
        this._x = character.getX();
        this._y = character.getY();
        this._z = character.getZ();
    }

    protected final void writeImpl() {
        writeC(47);
        writeD(this._charObjId);
        writeD(this._moveType);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
