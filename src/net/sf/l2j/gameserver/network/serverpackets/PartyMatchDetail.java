package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;

public class PartyMatchDetail extends L2GameServerPacket {
    private final PartyMatchRoom _room;

    public PartyMatchDetail(PartyMatchRoom room) {
        this._room = room;
    }

    protected final void writeImpl() {
        writeC(151);
        writeD(this._room.getId());
        writeD(this._room.getMaxMembers());
        writeD(this._room.getMinLvl());
        writeD(this._room.getMaxLvl());
        writeD(this._room.getLootType());
        writeD(this._room.getLocation());
        writeS(this._room.getTitle());
    }
}
