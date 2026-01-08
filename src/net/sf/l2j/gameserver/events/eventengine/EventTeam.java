package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.ArrayList;
import java.util.List;

public class EventTeam {
    private final String name;
    private final int color;
    private final Location location;
    private final List<Player> players;
    private int score = 0;

    public EventTeam(String name, int color, Location location) {
        this.name = name;
        this.color = color;
        this.location = location;
        this.players = new ArrayList<>();
    }

    public void clear() {
        this.score = 0;
        this.players.clear();
    }

    public void reward(int id, int count) {
        for (Player player : this.players) {
            player.addItem("Event reward.", id, count, null, true);
        }

    }

    public void teleportTeam() {
        for (Player player : this.players) {
            player.teleToLocation(this.location);
        }

    }

    public Location getLocation() {
        return this.location;
    }

    public int getScore() {
        return this.score;
    }

    public void increaseScore(int count) {
        this.score += count;
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        player.getAppearance().setNameColor(this.color);
        player.broadcastUserInfo();
    }

    public boolean inTeam(Player player) {
        return this.players.contains(player);
    }

    public String getName() {
        return this.name;
    }
}
