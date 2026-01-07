package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.StaticObject;

public class StaticObjectInfo extends L2GameServerPacket {
    private final StaticObject _staticObject;

    public StaticObjectInfo(StaticObject staticObject) {
        this._staticObject = staticObject;
    }

    protected final void writeImpl() {
        writeC(153);
        writeD(this._staticObject.getStaticObjectId());
        writeD(this._staticObject.getObjectId());
    }
}
