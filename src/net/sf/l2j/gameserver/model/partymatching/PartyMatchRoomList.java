package net.sf.l2j.gameserver.model.partymatching;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;

import java.util.HashMap;
import java.util.Map;

public class PartyMatchRoomList {
    private final Map<Integer, PartyMatchRoom> _rooms;
    private int _maxid = 1;

    protected PartyMatchRoomList() {
        this._rooms = new HashMap<>();
    }

    public static PartyMatchRoomList getInstance() {
        return SingletonHolder._instance;
    }

    public synchronized void addPartyMatchRoom(int id, PartyMatchRoom room) {
        this._rooms.put(id, room);
        this._maxid++;
    }

    public void deleteRoom(int id) {
        for (Player _member : getRoom(id).getPartyMembers()) {
            if (_member == null)
                continue;
            _member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
            _member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED);
            _member.setPartyRoom(0);
            _member.broadcastUserInfo();
        }
        this._rooms.remove(id);
    }

    public PartyMatchRoom getRoom(int id) {
        return this._rooms.get(id);
    }

    public PartyMatchRoom[] getRooms() {
        return (PartyMatchRoom[]) this._rooms.values().toArray((Object[]) new PartyMatchRoom[this._rooms.size()]);
    }

    public int getPartyMatchRoomCount() {
        return this._rooms.size();
    }

    public int getMaxId() {
        return this._maxid;
    }

    public PartyMatchRoom getPlayerRoom(Player player) {
        for (PartyMatchRoom _room : this._rooms.values()) {
            for (Player member : _room.getPartyMembers()) {
                if (member.equals(player))
                    return _room;
            }
        }
        return null;
    }

    public int getPlayerRoomId(Player player) {
        for (PartyMatchRoom _room : this._rooms.values()) {
            for (Player member : _room.getPartyMembers()) {
                if (member.equals(player))
                    return _room.getId();
            }
        }
        return -1;
    }

    private static class SingletonHolder {
        protected static final PartyMatchRoomList _instance = new PartyMatchRoomList();
    }
}
