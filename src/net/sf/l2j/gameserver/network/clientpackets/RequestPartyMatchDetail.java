package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExManagePartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPartyMatchDetail extends L2GameClientPacket {
    private int _roomid;

    private int _unk1;

    private int _unk2;

    private int _unk3;

    protected void readImpl() {
        this._roomid = readD();
        this._unk1 = readD();
        this._unk2 = readD();
        this._unk3 = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._roomid);
        if (room == null)
            return;
        if (activeChar.getLevel() >= room.getMinLvl() && activeChar.getLevel() <= room.getMaxLvl()) {
            PartyMatchWaitingList.getInstance().removePlayer(activeChar);
            activeChar.setPartyRoom(this._roomid);
            activeChar.sendPacket(new PartyMatchDetail(room));
            activeChar.sendPacket(new ExPartyRoomMember(room, 0));
            for (Player member : room.getPartyMembers()) {
                if (member == null)
                    continue;
                member.sendPacket(new ExManagePartyRoomMember(activeChar, room, 0));
                member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addCharName(activeChar));
            }
            room.addMember(activeChar);
            activeChar.broadcastUserInfo();
        } else {
            activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
        }
    }
}
