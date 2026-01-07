package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;

import java.util.ArrayList;
import java.util.List;

public class PartyMatchList extends L2GameServerPacket {
    private final Player _cha;

    private final int _loc;

    private final int _lim;

    private final List<PartyMatchRoom> _rooms;

    public PartyMatchList(Player player, int auto, int location, int limit) {
        this._cha = player;
        this._loc = location;
        this._lim = limit;
        this._rooms = new ArrayList<>();
    }

    protected final void writeImpl() {
        if (getClient().getPlayer() == null)
            return;
        for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms()) {
            if (room.getMembers() < 1 || room.getOwner() == null || !room.getOwner().isOnline() || room.getOwner().getPartyRoom() != room.getId()) {
                PartyMatchRoomList.getInstance().deleteRoom(room.getId());
            } else if (this._loc <= 0 || this._loc == room.getLocation()) {
                if (this._lim != 0 || (this._cha.getLevel() >= room.getMinLvl() && this._cha.getLevel() <= room.getMaxLvl()))
                    this._rooms.add(room);
            }
        }
        writeC(150);
        writeD(!this._rooms.isEmpty() ? 1 : 0);
        writeD(this._rooms.size());
        for (PartyMatchRoom room : this._rooms) {
            writeD(room.getId());
            writeS(room.getTitle());
            writeD(room.getLocation());
            writeD(room.getMinLvl());
            writeD(room.getMaxLvl());
            writeD(room.getMembers());
            writeD(room.getMaxMembers());
            writeS(room.getOwner().getName());
        }
    }
}
