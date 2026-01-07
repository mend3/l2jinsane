package mods.achievement.achievements.base;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Achievement {
    private static final Logger _log = Logger.getLogger(Achievement.class.getName());
    protected final StatSet _set;
    private final int _id;
    private final String _name;
    private final String _reward;
    private final List<RewardHolder> _rewardList = new ArrayList<>();
    private final String _template;
    private final String _description;
    private final Condition _condition;

    public Achievement(StatSet set) {
        _set = set;
        this._id = set.getInteger("id");
        this._name = set.getString("name");
        this._description = set.getString("description");
        this._reward = set.getString("reward");
        this._template = set.getString("template");
        try {
            this._condition = makeCondition();
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        createRewardList();
    }

    private void createRewardList() {
        for (String s : this._reward.split(";")) {
            if (!s.isEmpty()) {
                String[] split = s.split(",");
                int item = 0;
                long count = 0L;
                try {
                    item = Integer.parseInt(split[0]);
                    count = Long.parseLong(split[1]);
                } catch (NumberFormatException nfe) {
                    _log.warning("Achievements Error: Wrong reward " + nfe);
                }
                RewardHolder holder = new RewardHolder(item, count);
                this._rewardList.add(holder);
            }
        }
    }

    public boolean meetAchievementRequirements(Player player) {
        return getCondition() != null && getCondition().meetConditionRequirements(player);
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

    public List<RewardHolder> getRewardList() {
        return this._rewardList;
    }

    public Condition getCondition() {
        return this._condition;
    }

    public boolean meetConditionRequirements(Player player) {
        return this.getCondition() != null && this.getCondition().meetConditionRequirements(player);
    }

    private Condition makeCondition() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("mods.achievement.achievements." + this._template);
        Constructor<?> _constructor = clazz.getConstructor(StatSet.class);
        return (Condition) _constructor.newInstance(this._set);
    }

    public record RewardHolder(Item _item, long _count) {
        public RewardHolder(int _item, long _count) {
            this(ItemTable.getInstance().getTemplate(_item), _count);
        }
    }
}
