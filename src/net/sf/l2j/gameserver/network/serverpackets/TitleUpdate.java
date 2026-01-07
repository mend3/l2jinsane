package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class TitleUpdate extends L2GameServerPacket {
    private final String _title;

    private final int _objectId;

    public TitleUpdate(Creature cha) {
        this._objectId = cha.getObjectId();
        this._title = cha.getTitle();
    }

    protected void writeImpl() {
        writeC(204);
        writeD(this._objectId);
        writeS(this._title);
    }
}
