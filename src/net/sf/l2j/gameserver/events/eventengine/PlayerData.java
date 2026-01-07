package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

public class PlayerData {
    private final int playerId;

    private final int playerColor;

    private final String playerTitle;

    private final Location playerLocation;

    public PlayerData(Player player) {
        this.playerId = player.getObjectId();
        this.playerColor = player.getAppearance().getNameColor();
        this.playerTitle = player.getTitle();
        this.playerLocation = new Location(player.getX(), player.getY(), player.getZ());
    }

    public void restore(Player player) {
        if (player.isDead())
            player.doRevive();
        player.getAppearance().setNameColor(this.playerColor);
        player.setTitle(this.playerTitle);
        player.setTeam(TeamType.NONE);
        player.teleToLocation(this.playerLocation);
        player.sendMessage("Your status has been restored after leaving an event.");
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getPlayerColor() {
        return this.playerColor;
    }

    public String getPlayerTitle() {
        return this.playerTitle;
    }
}
