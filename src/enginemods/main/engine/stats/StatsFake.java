package enginemods.main.engine.stats;

import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class StatsFake extends AbstractMods {
    public StatsFake() {
        this.registerMod(true);
    }

    public static StatsFake getInstance() {
        return StatsFake.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Player.class, character)) {
            return value;
        } else {
            if (PlayerData.get((Player) character).isFake()) {
                switch (stat) {
                    case MAX_MP -> value *= 10.0F;
                    case REGENERATE_MP_RATE -> value *= 10.0F;
                    case REGENERATE_HP_RATE -> value *= 10.0F;
                    case MAGIC_DEFENCE -> value *= 4.0F;
                    case POWER_DEFENCE -> value *= 4.0F;
                    case MAGIC_REUSE_RATE -> value *= 0.8;
                    case POWER_ATTACK_SPEED -> value *= 3.0F;
                    case MAGIC_ATTACK_SPEED -> value *= 3.0F;
                    case POWER_ATTACK -> value *= 4.5F;
                    case MAGIC_ATTACK -> value *= 4.5F;
                }
            }

            return value;
        }
    }

    private static class SingletonHolder {
        protected static final StatsFake INSTANCE = new StatsFake();
    }
}
