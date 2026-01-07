package mods.achievement;

import mods.achievement.achievements.base.Achievement;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AchievementsManager implements IXmlReader {
    private static final CLogger LOGGER = new CLogger(AchievementsManager.class.getName());

    private final Map<Integer, Achievement> _achievementList = new HashMap<>();

    public AchievementsManager() {
    }

    public static AchievementsManager getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {
        parseFile("./data/xml/achievements.xml");
        LOGGER.info("Loaded {} Achievements.", this._achievementList.size());
    }

    public void parseDocument(Document doc, Path path) {
        for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling()) {
            if ("list".equalsIgnoreCase(list.getNodeName())) {
                for (Node achievement = list.getFirstChild(); achievement != null; achievement = achievement.getNextSibling()) {
                    if ("achievement".equalsIgnoreCase(achievement.getNodeName())) {
                        StatSet set = parseAttributes(achievement);

                        Achievement a = new Achievement(set);
                        if (a.getCondition() == null) {
                            LOGGER.warn(String.format("Condition is null [%s]", a));
                            continue;
                        }
                        _achievementList.put(a.getID(), a);
                    }
                }
            }
        }
    }

    public void rewardForAchievement(int achievementID, Player player) {
        Achievement achievement = this._achievementList.get(achievementID);
        for (Achievement.RewardHolder reward : achievement.getRewardList()) {
            int count = (int) reward._count();
            player.addItem(achievement.getName(), reward._item().getItemId(), count, player, true);
        }
    }

    public Map<Integer, Achievement> getAchievementList() {
        return this._achievementList;
    }

    private static class SingletonHolder {
        protected static final AchievementsManager _instance = new AchievementsManager();
    }
}
