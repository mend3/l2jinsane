package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;

public class ExManagePartyRoomMember extends L2GameServerPacket {
    private final Player _activeChar;

    private final PartyMatchRoom _room;

    private final int _mode;

    public ExManagePartyRoomMember(Player player, PartyMatchRoom room, int mode) {
        this._activeChar = player;
        this._room = room;
        this._mode = mode;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(16);
        writeD(this._mode);
        writeD(this._activeChar.getObjectId());
        writeS(this._activeChar.getName());
        writeD(this._activeChar.getActiveClass());
        writeD(this._activeChar.getLevel());
        writeD(MapRegionData.getInstance().getClosestLocation(this._activeChar.getX(), this._activeChar.getY()));
        if (this._room.getOwner().equals(this._activeChar)) {
            writeD(1);
        } else if (this._room.getOwner().isInParty() && this._activeChar.isInParty() && this._room.getOwner().getParty().getLeaderObjectId() == this._activeChar.getParty().getLeaderObjectId()) {
            writeD(2);
        } else {
            writeD(0);
        }
    }
}
