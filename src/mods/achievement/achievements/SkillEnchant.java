package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;

public class SkillEnchant extends Condition {
    public SkillEnchant(StatSet value) {
        super(value);
    }

    public String getStatus(Player player) {
        return "null";
    }

    @Override
    public Integer getValue() {
        return _set.getInteger("minSkillEnchant", 10);
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        for (L2Skill s : player.getSkills().values()) {
            String lvl = String.valueOf(s.getLevel());
            if (lvl.length() > 2) {
                int sklvl = Integer.parseInt(lvl.substring(1));
                if (sklvl >= val)
                    return true;
            }
        }
        return false;
    }
}
