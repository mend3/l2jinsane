package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Player;

public class HeroCount extends Condition {
    public HeroCount(StatSet value) {
        super(value);
    }

    @Override
    public Integer getValue() {
        return _set.getInteger("minHeroCount", 10);
    }

    public String getStatus(Player player) {
        return "" + HeroManager.getInstance().getHeroesCount(player);
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return HeroManager.getInstance().getHeroesCount(player) >= getValue();
    }
}
