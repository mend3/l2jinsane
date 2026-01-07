package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class TargetUnselected extends L2GameServerPacket {
    private final int _targetObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    public TargetUnselected(Creature character) {
        this._targetObjId = character.getObjectId();
        this._x = character.getX();
        this._y = character.getY();
        this._z = character.getZ();
    }

    protected final void writeImpl() {
        writeC(42);
        writeD(this._targetObjId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
