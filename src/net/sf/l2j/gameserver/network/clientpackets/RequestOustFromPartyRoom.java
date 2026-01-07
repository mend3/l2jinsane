package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public final class RequestOustFromPartyRoom extends L2GameClientPacket {
    private int _charid;

    protected void readImpl() {
        this._charid = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player member = World.getInstance().getPlayer(this._charid);
        if (member == null)
            return;
        PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
        if (room == null)
            return;
        if (room.getOwner() != activeChar)
            return;
        if (activeChar.isInParty() && member.isInParty() && activeChar.getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
        } else {
            room.deleteMember(member);
            member.setPartyRoom(0);
            member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
            PartyMatchWaitingList.getInstance().addPlayer(member);
            member.sendPacket(new PartyMatchList(member, 0, MapRegionData.getInstance().getClosestLocation(member.getX(), member.getY()), member.getLevel()));
            member.broadcastUserInfo();
            member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
        }
    }
}
