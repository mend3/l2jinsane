package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class FinishRotation extends L2GameServerPacket {
    private final int _heading;

    private final int _charObjId;

    public FinishRotation(Creature cha) {
        this._charObjId = cha.getObjectId();
        this._heading = cha.getHeading();
    }

    protected final void writeImpl() {
        writeC(99);
        writeD(this._charObjId);
        writeD(this._heading);
    }
}
