package mods.achievement;

import mods.achievement.achievements.*;
import mods.achievement.achievements.base.Achievement;
import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AchievementsManager implements IXmlReader {
    private static final CLogger LOGGER = new CLogger(AchievementsManager.class.getName());

    private final Map<Integer, Achievement> _achievementList = new HashMap<>();

    private final ArrayList<String> _binded = new ArrayList<>();

    public AchievementsManager() {
    }

    public static AchievementsManager getInstance() {
        return SingletonHolder._instance;
    }

    private static void addToConditionList(String nodeName, Object value, ArrayList<Condition> conditions) {
        if (nodeName.equals("minPvPCount")) {
            conditions.add(new Pvp(value));
        } else if (nodeName.equals("minPkCount")) {
            conditions.add(new Pk(value));
        } else if (nodeName.equals("mustBeHero")) {
            conditions.add(new Hero(value));
        } else if (nodeName.equals("mustBeNoble")) {
            conditions.add(new Noble(value));
        } else if (nodeName.equals("minWeaponEnchant")) {
            conditions.add(new WeaponEnchant(value));
        } else if (nodeName.equals("mustBeMarried")) {
            conditions.add(new Marry(value));
        } else if (nodeName.equals("itemAmmount")) {
            conditions.add(new ItemsCount(value));
        } else if (nodeName.equals("lordOfCastle")) {
            conditions.add(new Castle(value));
        } else if (nodeName.equals("CompleteAchievements")) {
            conditions.add(new CompleteAchievements(value));
        } else if (nodeName.equals("minSkillEnchant")) {
            conditions.add(new SkillEnchant(value));
        } else if (nodeName.equals("minOnlineTime")) {
            conditions.add(new OnlineTime(value));
        } else if (nodeName.equals("minHeroCount")) {
            conditions.add(new HeroCount(value));
        } else if (nodeName.equals("raidToKill")) {
            conditions.add(new RaidKill(value));
        } else if (nodeName.equals("raidToKill1")) {
            conditions.add(new RaidKill(value));
        } else if (nodeName.equals("raidToKill2")) {
            conditions.add(new RaidKill(value));
        } else if (nodeName.equals("minRaidPoints")) {
            conditions.add(new RaidPoints(value));
        }
    }

    public void load() {
        parseFile("./data/xml/achievements.xml");
        LOGGER.info("Loaded {} Achievements.", Integer.valueOf(this._achievementList.size()));
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "achievement", nnn -> {
        }));
    }

    public void rewardForAchievement(int achievementID, Player player) {
        Achievement achievement = this._achievementList.get(Integer.valueOf(achievementID));
        for (Iterator<Integer> iterator = achievement.getRewardList().keySet().iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            int count = achievement.getRewardList().get(Integer.valueOf(id)).intValue();
            player.addItem(achievement.getName(), id, count, player, true);
        }
    }

    public ArrayList<Condition> conditionList(NamedNodeMap attributesList) {
        ArrayList<Condition> conditions = new ArrayList<>();
        for (int j = 0; j < attributesList.getLength(); j++)
            addToConditionList(attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue(), conditions);
        return conditions;
    }

    public Map<Integer, Achievement> getAchievementList() {
        return this._achievementList;
    }

    public ArrayList<String> getBinded() {
        return this._binded;
    }

    public boolean isBinded(int obj, int ach) {
        for (String binds : this._binded) {
            String[] spl = binds.split("@");
            if (spl[0].equals(String.valueOf(obj)) && spl[1].equals(String.valueOf(ach)))
                return true;
        }
        return false;
    }

    public void loadUsed() {
        String sql = "SELECT ";
        for (int i = 1; i <= getAchievementList().size(); i++) {
            if (i != getAchievementList().size()) {
                sql = sql + "a" + sql + ",";
            } else {
                sql = sql + "a" + sql;
            }
        }
        sql = sql + " from achievements";
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement(sql);
                try {
                    ResultSet rs = statement.executeQuery();
                    try {
                        while (rs.next()) {
                            for (int j = 1; j <= getAchievementList().size(); j++) {
                                String ct = rs.getString(j);
                                if (ct.length() > 1 && ct.startsWith("1"))
                                    this._binded.add(ct.substring(ct.indexOf("1") + 1) + "@" + ct.substring(ct.indexOf("1") + 1));
                            }
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SQLException e) {
            LOGGER.warn("Achievement Save data via loadUsed error");
            LOGGER.error(e);
        }
    }

    private static class SingletonHolder {
        protected static final AchievementsManager _instance = new AchievementsManager();
    }
}
