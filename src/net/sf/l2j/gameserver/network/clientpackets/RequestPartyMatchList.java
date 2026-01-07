package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;

public class RequestPartyMatchList extends L2GameClientPacket {
    private int _roomid;

    private int _membersmax;

    private int _lvlmin;

    private int _lvlmax;

    private int _loot;

    private String _roomtitle;

    protected void readImpl() {
        this._roomid = readD();
        this._membersmax = readD();
        this._lvlmin = readD();
        this._lvlmax = readD();
        this._loot = readD();
        this._roomtitle = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._roomid > 0) {
            PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._roomid);
            if (room != null) {
                room.setMaxMembers(this._membersmax);
                room.setMinLvl(this._lvlmin);
                room.setMaxLvl(this._lvlmax);
                room.setLootType(this._loot);
                room.setTitle(this._roomtitle);
                for (Player member : room.getPartyMembers()) {
                    if (member == null)
                        continue;
                    member.sendPacket(new PartyMatchDetail(room));
                    member.sendPacket(SystemMessageId.PARTY_ROOM_REVISED);
                }
            }
        } else {
            int maxId = PartyMatchRoomList.getInstance().getMaxId();
            PartyMatchRoom room = new PartyMatchRoom(maxId, this._roomtitle, this._loot, this._lvlmin, this._lvlmax, this._membersmax, player);
            PartyMatchWaitingList.getInstance().removePlayer(player);
            PartyMatchRoomList.getInstance().addPartyMatchRoom(maxId, room);
            Party party = player.getParty();
            if (party != null)
                for (Player member : party.getMembers()) {
                    if (member == player)
                        continue;
                    member.setPartyRoom(maxId);
                    room.addMember(member);
                }
            player.sendPacket(new PartyMatchDetail(room));
            player.sendPacket(new ExPartyRoomMember(room, 1));
            player.sendPacket(SystemMessageId.PARTY_ROOM_CREATED);
            player.setPartyRoom(maxId);
            player.broadcastUserInfo();
        }
    }
}
