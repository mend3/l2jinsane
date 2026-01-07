package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;

import java.util.ArrayList;
import java.util.List;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket {
    private final Player _activeChar;

    private final int _page;

    private final int _minlvl;

    private final int _maxlvl;

    private final int _mode;

    private final List<Player> _members;

    public ExListPartyMatchingWaitingRoom(Player player, int page, int minlvl, int maxlvl, int mode) {
        this._activeChar = player;
        this._page = page;
        this._minlvl = minlvl;
        this._maxlvl = maxlvl;
        this._mode = mode;
        this._members = new ArrayList<>();
    }

    protected void writeImpl() {
        writeC(254);
        writeH(53);
        if (this._mode == 0) {
            PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._activeChar.getPartyRoom());
            if (room == null || !room.getOwner().equals(this._activeChar)) {
                writeD(0);
                writeD(0);
                return;
            }
        }
        for (Player cha : PartyMatchWaitingList.getInstance().getPlayers()) {
            if (cha == null || cha == this._activeChar)
                continue;
            if (cha.getLevel() < this._minlvl || cha.getLevel() > this._maxlvl)
                continue;
            this._members.add(cha);
        }
        writeD(1);
        writeD(this._members.size());
        for (Player member : this._members) {
            writeS(member.getName());
            writeD(member.getActiveClass());
            writeD(member.getLevel());
        }
    }
}
