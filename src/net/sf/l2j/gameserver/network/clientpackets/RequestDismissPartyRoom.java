package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;

public class RequestDismissPartyRoom extends L2GameClientPacket {
    private int _roomid;

    private int _data2;

    protected void readImpl() {
        this._roomid = readD();
        this._data2 = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._roomid);
        if (room == null)
            return;
        PartyMatchRoomList.getInstance().deleteRoom(this._roomid);
    }
}
