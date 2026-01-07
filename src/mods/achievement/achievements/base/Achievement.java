package mods.achievement.achievements.base;

import net.sf.l2j.gameserver.model.actor.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class Achievement {
    private static final Logger _log = Logger.getLogger(Achievement.class.getName());
    private final int _id;
    private final String _name;
    private final String _reward;
    private String _description = "No Description!";
    private final HashMap<Integer, Long> _rewardList;
    private final ArrayList<Condition> _conditions;

    public Achievement(int id, String name, String description, String reward, ArrayList<Condition> conditions) {
        this._rewardList = new HashMap<>();
        this._id = id;
        this._name = name;
        this._description = description;
        this._reward = reward;
        this._conditions = conditions;
        createRewardList();
    }

    private void createRewardList() {
        for (String s : this._reward.split(";")) {
            if (s != null && !s.isEmpty()) {
                String[] split = s.split(",");
                Integer item = Integer.valueOf(0);
                Long count = Long.valueOf(0L);
                try {
                    item = Integer.valueOf(split[0]);
                    count = Long.valueOf(split[1]);
                } catch (NumberFormatException nfe) {
                    _log.warning("Achievements Error: Wrong reward " + nfe);
                }
                this._rewardList.put(item, count);
            }
        }
    }

    public boolean meetAchievementRequirements(Player player) {
        for (Condition c : getConditions()) {
            if (!c.meetConditionRequirements(player))
                return false;
        }
        return true;
    }

    public int getID() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }

    public String getDescription() {
        return this._description;
    }

    public String getReward() {
        return this._reward;
    }

    public HashMap<Integer, Long> getRewardList() {
        return this._rewardList;
    }

    public ArrayList<Condition> getConditions() {
        return this._conditions;
    }
}
