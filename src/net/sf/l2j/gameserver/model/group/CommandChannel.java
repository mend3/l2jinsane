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
        this.recalculateLevel();

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
        if (!(obj instanceof CommandChannel)) {
            return false;
        } else {
            return obj == this ? true : this.isLeader(((CommandChannel) obj).getLeader());
        }
    }

    public List<Player> getMembers() {
        List<Player> members = new ArrayList<>();

        for (Party party : this._parties) {
            members.addAll(party.getMembers());
        }

        return members;
    }

    public int getMembersCount() {
        int count = 0;

        for (Party party : this._parties) {
            count += party.getMembersCount();
        }

        return count;
    }

    public boolean containsPlayer(WorldObject player) {
        for (Party party : this._parties) {
            if (party.containsPlayer(player)) {
                return true;
            }
        }

        return false;
    }

    public void broadcastPacket(L2GameServerPacket packet) {
        for (Party party : this._parties) {
            party.broadcastPacket(packet);
        }

    }

    public void broadcastCreatureSay(CreatureSay msg, Player broadcaster) {
        for (Party party : this._parties) {
            party.broadcastCreatureSay(msg, broadcaster);
        }

    }

    public void recalculateLevel() {
        int newLevel = 0;

        for (Party party : this._parties) {
            if (party.getLevel() > newLevel) {
                newLevel = party.getLevel();
            }
        }

        this.setLevel(newLevel);
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
        if (party != null && !this._parties.contains(party)) {
            this.broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));
            this._parties.add(party);
            if (party.getLevel() > this.getLevel()) {
                this.setLevel(party.getLevel());
            }

            party.setCommandChannel(this);

            for (Player member : party.getMembers()) {
                member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
                member.sendPacket(ExOpenMPCC.STATIC_PACKET);
            }

        }
    }

    public boolean removeParty(Party party) {
        if (party != null && this._parties.contains(party)) {
            if (this._parties.size() == 2) {
                this.disband();
            } else {
                this._parties.remove(party);
                party.setCommandChannel(null);
                party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
                this.recalculateLevel();
                this.broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
            }

            return true;
        } else {
            return false;
        }
    }

    public List<Party> getParties() {
        return this._parties;
    }

    public boolean meetRaidWarCondition(Attackable attackable) {
        return switch (attackable.getNpcId()) {
            case 29001, 29006, 29014, 29022 -> this.getMembersCount() > 36;
            case 29019 -> this.getMembersCount() > 225;
            case 29020 -> this.getMembersCount() > 56;
            case 29028 -> this.getMembersCount() > 99;
            default -> this.getMembersCount() > 18;
        };
    }
}
