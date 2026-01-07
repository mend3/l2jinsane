package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public final class RequestPartyMatchConfig extends L2GameClientPacket {
    private int _auto;

    private int _loc;

    private int _lvl;

    protected void readImpl() {
        this._auto = readD();
        this._loc = readD();
        this._lvl = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (!activeChar.isInPartyMatchRoom() && activeChar.getParty() != null && activeChar.getParty().getLeader() != activeChar) {
            activeChar.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activeChar.isInPartyMatchRoom()) {
            PartyMatchRoomList list = PartyMatchRoomList.getInstance();
            if (list == null)
                return;
            PartyMatchRoom room = list.getPlayerRoom(activeChar);
            if (room == null)
                return;
            activeChar.sendPacket(new PartyMatchDetail(room));
            activeChar.sendPacket(new ExPartyRoomMember(room, 2));
            activeChar.setPartyRoom(room.getId());
            activeChar.broadcastUserInfo();
        } else {
            PartyMatchWaitingList.getInstance().addPlayer(activeChar);
            activeChar.sendPacket(new PartyMatchList(activeChar, this._auto, this._loc, this._lvl));
        }
    }
}
