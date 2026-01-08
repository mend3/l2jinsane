package net.sf.l2j.gameserver.model.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public abstract class AbstractGroup {
    private Player _leader;
    private int _level;

    public AbstractGroup(Player leader) {
        this._leader = leader;
    }

    public abstract List<Player> getMembers();

    public abstract int getMembersCount();

    public abstract boolean containsPlayer(WorldObject var1);

    public abstract void broadcastPacket(L2GameServerPacket var1);

    public abstract void broadcastCreatureSay(CreatureSay var1, Player var2);

    public abstract void recalculateLevel();

    public abstract void disband();

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public Player getLeader() {
        return this._leader;
    }

    public void setLeader(Player leader) {
        this._leader = leader;
    }

    public int getLeaderObjectId() {
        return this._leader.getObjectId();
    }

    public boolean isLeader(Player player) {
        return this._leader.getObjectId() == player.getObjectId();
    }

    public void broadcastMessage(SystemMessageId message) {
        this.broadcastPacket(SystemMessage.getSystemMessage(message));
    }

    public void broadcastString(String text) {
        this.broadcastPacket(SystemMessage.sendString(text));
    }

    public Player getRandomPlayer() {
        return Rnd.get(this.getMembers());
    }
}
