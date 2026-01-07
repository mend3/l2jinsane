package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

import java.util.StringTokenizer;

public class CastleDoorman extends Doorman {
    public CastleDoorman(int objectID, NpcTemplate template) {
        super(objectID, template);
    }

    protected void openDoors(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
        st.nextToken();
        while (st.hasMoreTokens())
            getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
    }

    protected final void closeDoors(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
        st.nextToken();
        while (st.hasMoreTokens())
            getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
    }

    protected final boolean isOwnerClan(Player player) {
        if (player.getClan() != null)
            if (getCastle() != null)
                return player.getClanId() == getCastle().getOwnerId() && (player.getClanPrivileges() & 0x8000) == 32768;
        return false;
    }

    protected final boolean isUnderSiege() {
        return getCastle().getSiegeZone().isActive();
    }
}
