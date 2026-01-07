package enginemods.main.engine.stats;

import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;

public class StatsNpc extends AbstractMods {
    public StatsNpc() {
        registerMod(true);
    }

    public static StatsNpc getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Monster.class, character))
            return value;
        switch (stat) {
            case POWER_DEFENCE:
                value /= 1.0D;
                break;
            case MAGIC_DEFENCE:
                value /= 1.0D;
                break;
        }
        return value;
    }

    private static class SingletonHolder {
        protected static final StatsNpc INSTANCE = new StatsNpc();
    }
}
