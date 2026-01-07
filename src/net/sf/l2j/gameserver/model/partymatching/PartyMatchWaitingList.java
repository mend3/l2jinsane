package net.sf.l2j.gameserver.model.partymatching;

import net.sf.l2j.gameserver.model.actor.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyMatchWaitingList {
    private final List<Player> _members = new ArrayList<>();

    public static PartyMatchWaitingList getInstance() {
        return SingletonHolder._instance;
    }

    public void addPlayer(Player player) {
        if (!this._members.contains(player))
            this._members.add(player);
    }

    public void removePlayer(Player player) {
        this._members.remove(player);
    }

    public List<Player> getPlayers() {
        return this._members;
    }

    private static class SingletonHolder {
        protected static final PartyMatchWaitingList _instance = new PartyMatchWaitingList();
    }
}
