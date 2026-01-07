package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket {
    private int _roomid;

    private int _unk1;

    protected void readImpl() {
        this._roomid = readD();
        this._unk1 = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._roomid);
        if (room == null)
            return;
        if (!activeChar.isInParty() || !room.getOwner().isInParty() || activeChar.getParty().getLeaderObjectId() != room.getOwner().getParty().getLeaderObjectId()) {
            room.deleteMember(activeChar);
            activeChar.setPartyRoom(0);
            activeChar.broadcastUserInfo();
            activeChar.sendPacket(ExClosePartyRoom.STATIC_PACKET);
            activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
        }
    }
}
