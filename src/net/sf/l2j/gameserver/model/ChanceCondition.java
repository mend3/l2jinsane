/**/
package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChanceCondition {
    public static final int EVT_HIT = 1;
    public static final int EVT_CRIT = 2;
    public static final int EVT_CAST = 4;
    public static final int EVT_PHYSICAL = 8;
    public static final int EVT_MAGIC = 16;
    public static final int EVT_MAGIC_GOOD = 32;
    public static final int EVT_MAGIC_OFFENSIVE = 64;
    public static final int EVT_ATTACKED = 128;
    public static final int EVT_ATTACKED_HIT = 256;
    public static final int EVT_ATTACKED_CRIT = 512;
    public static final int EVT_HIT_BY_SKILL = 1024;
    public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
    public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
    public static final int EVT_EVADED_HIT = 8192;
    public static final int EVT_ON_START = 16384;
    public static final int EVT_ON_ACTION_TIME = 32768;
    public static final int EVT_ON_EXIT = 65536;
    private static final Logger _log = Logger.getLogger(ChanceCondition.class.getName());
    private final ChanceCondition.TriggerType _triggerType;
    private final int _chance;

    private ChanceCondition(ChanceCondition.TriggerType trigger, int chance) {
        this._triggerType = trigger;
        this._chance = chance;
    }

    public static ChanceCondition parse(StatSet set) {
        try {
            ChanceCondition.TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
            int chance = set.getInteger("activationChance", -1);
            if (trigger != null) {
                return new ChanceCondition(trigger, chance);
            }
        } catch (Exception var3) {
            _log.log(Level.WARNING, "", var3);
        }

        return null;
    }

    public static ChanceCondition parse(String chanceType, int chance) {
        try {
            if (chanceType == null) {
                return null;
            }

            ChanceCondition.TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);
            if (trigger != null) {
                return new ChanceCondition(trigger, chance);
            }
        } catch (Exception var3) {
            _log.log(Level.WARNING, "", var3);
        }

        return null;
    }

    public boolean trigger(int event) {
        return this._triggerType.check(event) && (this._chance < 0 || Rnd.get(100) < this._chance);
    }

    public String toString() {
        int var10000 = this._chance;
        return "Trigger[" + var10000 + ";" + this._triggerType.toString() + "]";
    }

    public enum TriggerType {
        ON_HIT(1),
        ON_CRIT(2),
        ON_CAST(4),
        ON_PHYSICAL(8),
        ON_MAGIC(16),
        ON_MAGIC_GOOD(32),
        ON_MAGIC_OFFENSIVE(64),
        ON_ATTACKED(128),
        ON_ATTACKED_HIT(256),
        ON_ATTACKED_CRIT(512),
        ON_HIT_BY_SKILL(1024),
        ON_HIT_BY_OFFENSIVE_SKILL(2048),
        ON_HIT_BY_GOOD_MAGIC(4096),
        ON_EVADED_HIT(8192),
        ON_START(16384),
        ON_ACTION_TIME(32768),
        ON_EXIT(65536);

        private final int _mask;

        TriggerType(int mask) {
            this._mask = mask;
        }

        // $FF: synthetic method
        private static ChanceCondition.TriggerType[] $values() {
            return new ChanceCondition.TriggerType[]{ON_HIT, ON_CRIT, ON_CAST, ON_PHYSICAL, ON_MAGIC, ON_MAGIC_GOOD, ON_MAGIC_OFFENSIVE, ON_ATTACKED, ON_ATTACKED_HIT, ON_ATTACKED_CRIT, ON_HIT_BY_SKILL, ON_HIT_BY_OFFENSIVE_SKILL, ON_HIT_BY_GOOD_MAGIC, ON_EVADED_HIT, ON_START, ON_ACTION_TIME, ON_EXIT};
        }

        public final boolean check(int event) {
            return (this._mask & event) != 0;
        }
    }
}