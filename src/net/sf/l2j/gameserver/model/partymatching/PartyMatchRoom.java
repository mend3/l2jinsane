package net.sf.l2j.gameserver.model.partymatching;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExManagePartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class PartyMatchRoom {
    private final int _id;
    private final List<Player> _members = new ArrayList<>();
    private String _title;
    private int _loot;
    private int _location;
    private int _minlvl;
    private int _maxlvl;
    private int _maxmem;

    public PartyMatchRoom(int id, String title, int loot, int minlvl, int maxlvl, int maxmem, Player owner) {
        this._id = id;
        this._title = title;
        this._loot = loot;
        this._location = MapRegionData.getInstance().getClosestLocation(owner.getX(), owner.getY());
        this._minlvl = minlvl;
        this._maxlvl = maxlvl;
        this._maxmem = maxmem;
        this._members.add(owner);
    }

    public List<Player> getPartyMembers() {
        return this._members;
    }

    public void addMember(Player player) {
        this._members.add(player);
    }

    public void deleteMember(Player player) {
        if (player != getOwner()) {
            this._members.remove(player);
            notifyMembersAboutExit(player);
        } else if (this._members.size() == 1) {
            PartyMatchRoomList.getInstance().deleteRoom(this._id);
        } else {
            changeLeader(this._members.get(1));
            deleteMember(player);
        }
    }

    public void notifyMembersAboutExit(Player player) {
        for (Player _member : getPartyMembers()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM);
            sm.addCharName(player);
            _member.sendPacket(sm);
            _member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
        }
    }

    public void changeLeader(Player newLeader) {
        Player oldLeader = this._members.get(0);
        this._members.remove(newLeader);
        this._members.set(0, newLeader);
        this._members.add(oldLeader);
        for (Player member : getPartyMembers()) {
            member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
            member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
            member.sendPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED);
        }
    }

    public int getId() {
        return this._id;
    }

    public Player getOwner() {
        return this._members.get(0);
    }

    public int getMembers() {
        return this._members.size();
    }

    public int getLootType() {
        return this._loot;
    }

    public void setLootType(int loot) {
        this._loot = loot;
    }

    public int getMinLvl() {
        return this._minlvl;
    }

    public void setMinLvl(int minlvl) {
        this._minlvl = minlvl;
    }

    public int getMaxLvl() {
        return this._maxlvl;
    }

    public void setMaxLvl(int maxlvl) {
        this._maxlvl = maxlvl;
    }

    public int getLocation() {
        return this._location;
    }

    public void setLocation(int loc) {
        this._location = loc;
    }

    public int getMaxMembers() {
        return this._maxmem;
    }

    public void setMaxMembers(int maxmem) {
        this._maxmem = maxmem;
    }

    public String getTitle() {
        return this._title;
    }

    public void setTitle(String title) {
        this._title = title;
    }
}
