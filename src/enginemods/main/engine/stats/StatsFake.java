package enginemods.main.engine.stats;

import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class StatsFake extends AbstractMods {
    public StatsFake() {
        registerMod(true);
    }

    public static StatsFake getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Player.class, character))
            return value;
        if (PlayerData.get((Player) character).isFake())
            switch (stat) {
                case MAX_MP:
                    value *= 10.0D;
                    break;
                case REGENERATE_MP_RATE:
                    value *= 10.0D;
                    break;
                case REGENERATE_HP_RATE:
                    value *= 10.0D;
                    break;
                case MAGIC_DEFENCE:
                    value *= 4.0D;
                    break;
                case POWER_DEFENCE:
                    value *= 4.0D;
                    break;
                case MAGIC_REUSE_RATE:
                    value *= 0.8D;
                    break;
                case POWER_ATTACK_SPEED:
                    value *= 3.0D;
                    break;
                case MAGIC_ATTACK_SPEED:
                    value *= 3.0D;
                    break;
                case POWER_ATTACK:
                    value *= 4.5D;
                    break;
                case MAGIC_ATTACK:
                    value *= 4.5D;
                    break;
            }
        return value;
    }

    private static class SingletonHolder {
        protected static final StatsFake INSTANCE = new StatsFake();
    }
}
