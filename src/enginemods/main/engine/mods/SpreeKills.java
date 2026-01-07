package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.HashMap;
import java.util.Map;

public class SpreeKills extends AbstractMods {
    private static final Map<Integer, Integer> players = new HashMap<>();

    public SpreeKills() {
        registerMod(ConfigData.ENABLE_SpreeKills);
    }

    public static boolean announcements(Player player, int count) {
        String announcement = "";
        for (Map.Entry<Integer, String> kill : ConfigData.ANNOUNCEMENTS_KILLS.entrySet()) {
            announcement = kill.getValue();
            if (kill.getKey() == count)
                break;
        }
        World.toAllOnlinePlayers(new CreatureSay(0, 2, "", announcement.replace("%s1", player.getName())));
        return true;
    }

    public static SpreeKills getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onDeath(Creature player) {
        players.remove(Integer.valueOf(player.getObjectId()));
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (!Util.areObjectType(Player.class, victim) || killer.getActingPlayer() == null)
            return;
        Player activeChar = killer.getActingPlayer();
        int count = 1;
        if (players.containsKey(Integer.valueOf(activeChar.getObjectId()))) {
            count = players.get(Integer.valueOf(activeChar.getObjectId()));
            count++;
        }
        players.put(Integer.valueOf(activeChar.getObjectId()), Integer.valueOf(count));
        if (ConfigData.ENABLE_KILL_EFFECT)
            activeChar.broadcastPacket(new MagicSkillUse(activeChar, victim, ConfigData.KILL_SKILL_EFFECT, 1, 500, 500));
        announcements(activeChar, count);
    }

    private static class SingletonHolder {
        protected static SpreeKills INSTANCE = new SpreeKills();
    }
}
