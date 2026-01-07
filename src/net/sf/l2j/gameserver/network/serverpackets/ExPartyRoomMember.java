package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;

public class ExPartyRoomMember extends L2GameServerPacket {
    private final PartyMatchRoom _room;

    private final int _mode;

    public ExPartyRoomMember(PartyMatchRoom room, int mode) {
        this._room = room;
        this._mode = mode;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(14);
        writeD(this._mode);
        writeD(this._room.getMembers());
        for (Player member : this._room.getPartyMembers()) {
            writeD(member.getObjectId());
            writeS(member.getName());
            writeD(member.getActiveClass());
            writeD(member.getLevel());
            writeD(MapRegionData.getInstance().getClosestLocation(member.getX(), member.getY()));
            if (this._room.getOwner().equals(member)) {
                writeD(1);
                continue;
            }
            if (this._room.getOwner().isInParty() && member.isInParty() && this._room.getOwner().getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()) {
                writeD(2);
                continue;
            }
            writeD(0);
        }
    }
}
