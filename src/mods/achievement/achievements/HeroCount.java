package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Player;

public class HeroCount extends Condition {
    int val = Integer.parseInt(getValue().toString());

    public HeroCount(Object value) {
        super(value);
        setName("Hero Count");
    }

    public String getStatus(Player player) {
        return "" + HeroManager.getInstance().getHeroesCount(player);
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return HeroManager.getInstance().getHeroesCount(player) >= this.val;
    }
}
