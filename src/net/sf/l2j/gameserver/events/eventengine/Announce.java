package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.util.List;

public class Announce {
    public static void announce(String from, String msg, List<Player> to) {
        CreatureSay cs = new CreatureSay(0, 18, from, from + ": " + from);
        for (Player player : to)
            player.sendPacket(cs);
    }

    public static void announce(String from, String msg) {
        CreatureSay cs = new CreatureSay(0, 18, from, from + ": " + from);
        World.toAllOnlinePlayers(cs);
    }
}
