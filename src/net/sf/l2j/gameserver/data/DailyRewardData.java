package net.sf.l2j.gameserver.data;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.DailyReward;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailyRewardData implements IXmlReader {
    private Map<Integer, DailyReward> _dailyRewards = new LinkedHashMap<>();

    public DailyRewardData() {
    }

    public static DailyRewardData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        parseFile("./data/xml/dailyreward/dailyrewards.xml");
        LOGGER.info("Loaded " + this._dailyRewards.size() + " Daily Rewards data.");
    }

    public void parseDocument(Document doc, Path path) {
        try {
            Node n = doc.getFirstChild();
            for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling()) {
                if ("reward".equalsIgnoreCase(o.getNodeName())) {
                    NamedNodeMap attrs = o.getAttributes();
                    DailyReward reward = null;
                    int day = Integer.parseInt(attrs.getNamedItem("Day").getNodeValue());
                    for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("item".equalsIgnoreCase(d.getNodeName())) {
                            attrs = d.getAttributes();
                            reward = new DailyReward(day, Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue()));
                            reward.setAmount(Integer.parseInt(attrs.getNamedItem("amount").getNodeValue()));
                            reward.setEnchantLevel(Integer.parseInt(attrs.getNamedItem("enchantLevel").getNodeValue()));
                            if (ItemTable.getInstance().getTemplate(Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue())) != null) {
                                this._dailyRewards.put(Integer.valueOf(day), reward);
                            } else {
                                LOGGER.warn("Daily Reward Data: Item ID: " + Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue()) + " doesn't exists in game.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Daily Reward Data: Error while creating table: " + e);
        }
    }

    public void reload() {
        this._dailyRewards.clear();
        load();
    }

    public DailyReward getDailyRewardByDay(int day) {
        return this._dailyRewards.get(Integer.valueOf(day));
    }

    public List<DailyReward> getAllDailyRewads() {
        return new ArrayList<>(this._dailyRewards.values());
    }

    public Map<Integer, DailyReward> getDailyRewards() {
        return this._dailyRewards;
    }

    public void setDailyRewards(Map<Integer, DailyReward> dailyRewards) {
        this._dailyRewards = dailyRewards;
    }

    private static class SingletonHolder {
        private static final DailyRewardData INSTANCE = new DailyRewardData();
    }
}
