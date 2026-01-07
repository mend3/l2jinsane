package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.model.actor.Player;

public class Castle extends Condition {
    public Castle(Object value) {
        super(value);
        setName("Have Castle");
    }

    public String getStatus(Player player) {
        if (getValue() == null)
            return "null";
        if (player.getClan() != null &&
                player.getClan().getCastleId() > 0)
            return CastleManager.getInstance().getCastleById(player.getClan().getCastleId()).getName();
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        if (player.getClan() != null)
            return player.getClan().getCastleId() > 0;
        return false;
    }
}
