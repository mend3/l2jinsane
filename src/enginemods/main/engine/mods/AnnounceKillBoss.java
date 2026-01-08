package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class AnnounceKillBoss extends AbstractMods {
    public AnnounceKillBoss() {
        this.registerMod(ConfigData.ENABLE_AnnounceKillBoss);
    }

    public static AnnounceKillBoss getInstance() {
        return AnnounceKillBoss.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (Util.areObjectType(Playable.class, killer)) {
            if (Util.areObjectType(RaidBoss.class, victim)) {
                World.toAllOnlinePlayers(new CreatureSay(0, 2, "", ConfigData.ANNOUNCE_KILL_BOSS.replace("%s1", killer.getActingPlayer().getName()).replace("%s2", victim.getName())));
            } else if (Util.areObjectType(GrandBoss.class, victim)) {
                World.toAllOnlinePlayers(new CreatureSay(0, 2, "", ConfigData.ANNOUNCE_KILL_GRANDBOSS.replace("%s1", killer.getActingPlayer().getName()).replace("%s2", victim.getName())));
            }
        }
    }

    private static class SingletonHolder {
        protected static final AnnounceKillBoss INSTANCE = new AnnounceKillBoss();
    }
}
