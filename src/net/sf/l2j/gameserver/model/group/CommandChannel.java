package net.sf.l2j.gameserver.model.group;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandChannel extends AbstractGroup {
    private final List<Party> _parties = new CopyOnWriteArrayList<>();

    public CommandChannel(Party requestor, Party target) {
        super(requestor.getLeader());
        this._parties.add(requestor);
        this._parties.add(target);
        requestor.setCommandChannel(this);
        target.setCommandChannel(this);
        recalculateLevel();
        for (Player member : requestor.getMembers()) {
            member.sendPacket(SystemMessageId.COMMAND_CHANNEL_FORMED);
            member.sendPacket(ExOpenMPCC.STATIC_PACKET);
        }
        for (Player member : target.getMembers()) {
            member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
            member.sendPacket(ExOpenMPCC.STATIC_PACKET);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CommandChannel))
            return false;
        if (obj == this)
            return true;
        return isLeader(((CommandChannel) obj).getLeader());
    }

    public List<Player> getMembers() {
        List<Player> members = new ArrayList<>();
        for (Party party : this._parties)
            members.addAll(party.getMembers());
        return members;
    }

    public int getMembersCount() {
        int count = 0;
        for (Party party : this._parties)
            count += party.getMembersCount();
        return count;
    }

    public boolean containsPlayer(WorldObject player) {
        for (Party party : this._parties) {
            if (party.containsPlayer(player))
                return true;
        }
        return false;
    }

    public void broadcastPacket(L2GameServerPacket packet) {
        for (Party party : this._parties)
            party.broadcastPacket(packet);
    }

    public void broadcastCreatureSay(CreatureSay msg, Player broadcaster) {
        for (Party party : this._parties)
            party.broadcastCreatureSay(msg, broadcaster);
    }

    public void recalculateLevel() {
        int newLevel = 0;
        for (Party party : this._parties) {
            if (party.getLevel() > newLevel)
                newLevel = party.getLevel();
        }
        setLevel(newLevel);
    }

    public void disband() {
        for (Party party : this._parties) {
            party.setCommandChannel(null);
            party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
            party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
        }
        this._parties.clear();
    }

    public void addParty(Party party) {
        if (party == null || this._parties.contains(party))
            return;
        broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));
        this._parties.add(party);
        if (party.getLevel() > getLevel())
            setLevel(party.getLevel());
        party.setCommandChannel(this);
        for (Player member : party.getMembers()) {
            member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
            member.sendPacket(ExOpenMPCC.STATIC_PACKET);
        }
    }

    public boolean removeParty(Party party) {
        if (party == null || !this._parties.contains(party))
            return false;
        if (this._parties.size() == 2) {
            disband();
        } else {
            this._parties.remove(party);
            party.setCommandChannel(null);
            party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
            recalculateLevel();
            broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
        }
        return true;
    }

    public List<Party> getParties() {
        return this._parties;
    }

    public boolean meetRaidWarCondition(Attackable attackable) {
        switch (attackable.getNpcId()) {
            case 29001:
            case 29006:
            case 29014:
            case 29022:
                return (getMembersCount() > 36);
            case 29020:
                return (getMembersCount() > 56);
            case 29019:
                return (getMembersCount() > 225);
            case 29028:
                return (getMembersCount() > 99);
        }
        return (getMembersCount() > 18);
    }
}
