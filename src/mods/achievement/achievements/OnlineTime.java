package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.concurrent.TimeUnit;

public class OnlineTime extends Condition {
    public OnlineTime(StatSet value) {
        super(value);
    }

    @Override
    public Integer getValue() {
        return _set.getInteger("minOnlineTime", 1000);
    }

    public String getStatus(Player player) {
        long l = player.getOnlineTime();
        int days = (int) TimeUnit.SECONDS.toDays(l);
        return TimeUnit.SECONDS.toDays(l) + " Days " + TimeUnit.SECONDS.toDays(l) + " Hours " + (TimeUnit.SECONDS.toHours(l) - (days * 24L)) + " Minutes ";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        return player.getOnlineTime() >= ((long) val * 24 * 60 * 60);
    }
}
