package net.sf.l2j.gameserver.data;

import mods.xml.XMLDocument;
import net.sf.l2j.gameserver.model.Doll;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DollsData extends XMLDocument {
    private final Map<Integer, Doll> dolls = new HashMap<>();

    public DollsData() {
    }

    public static DollsData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static Doll getDoll(Player player) {
        List<ItemInstance> collect = player.getInventory().getItems().stream().filter(x -> getInstance().isDollById(x.getItemId())).toList();
        int skillLv = 0;
        int itemId = 0;
        System.out.println(collect.isEmpty());
        if (!collect.isEmpty())
            for (ItemInstance y : collect) {
                int skillLvl = getInstance().getDollById(y.getItemId()).getSkillLvl();
                if (skillLvl > skillLv) {
                    skillLv = skillLvl;
                    itemId = y.getItemId();
                }
            }
        if (itemId == 0)
            return null;
        return getInstance().getDollById(itemId);
    }

    public static void setSkillForDoll(Player player, int dollItemId) {
        Doll doll = getInstance().getDollById(dollItemId);
        if (doll == null)
            return;
        int skillId = doll.getSkillId();
        int skillLvl = doll.getSkillLvl();
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
        if (skill != null) {
            int currentSkillLvl = player.getSkillLevel(skillId);
            if (currentSkillLvl > 0)
                player.removeSkill(skillId, false);
            if (player.getInventory().getItemByItemId(dollItemId) == null) {
                refreshAllDollSkills(player);
            } else {
                player.addSkill(skill, false);
            }
            player.sendSkillList();
        }
    }

    public static void refreshAllDollSkills(Player player) {
        Map<Integer, Integer> highestSkillLevels = new HashMap<>();
        List<ItemInstance> collect = player.getInventory().getItems().stream().filter(x -> getInstance().isDollById(x.getItemId())).collect(Collectors.toList());
        for (ItemInstance dollItem : collect) {
            int skillId = getInstance().getDollById(dollItem.getItemId()).getSkillId();
            int skillLvl = getInstance().getDollById(dollItem.getItemId()).getSkillLvl();
            if (!highestSkillLevels.containsKey(Integer.valueOf(skillId)) || skillLvl > highestSkillLevels.get(Integer.valueOf(skillId)))
                highestSkillLevels.put(Integer.valueOf(skillId), Integer.valueOf(skillLvl));
        }
        for (Map.Entry<Integer, Integer> entry : highestSkillLevels.entrySet()) {
            L2Skill skill = SkillTable.getInstance().getInfo(entry.getKey(), entry.getValue());
            if (skill != null)
                player.addSkill(skill, false);
        }
        player.sendSkillList();
    }

    public static void getSkillDoll(Player player, ItemInstance item) {
        if (item != null &&
                getInstance().isDollById(item.getItemId())) {
            setSkillForDoll(player, item.getItemId());
            refreshAllDollSkills(player);
        }
    }

    public void reload() {
        this.dolls.clear();
        load();
    }

    public void load() {
        loadDocument("./data/xml/Dolls.xml");
        LOG.info("DollsData: Loaded " + this.dolls.size() + " dolls.");
    }

    protected void parseDocument(Document doc, File file) {
        try {
            Node root = doc.getFirstChild();
            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                if ("Doll".equalsIgnoreCase(node.getNodeName())) {
                    NamedNodeMap attrs = node.getAttributes();
                    int id = Integer.parseInt(attrs.getNamedItem("Id").getNodeValue());
                    int skillId = Integer.parseInt(attrs.getNamedItem("SkillId").getNodeValue());
                    int skillLvl = Integer.parseInt(attrs.getNamedItem("SkillLvl").getNodeValue());
                    Doll doll = new Doll(id, skillId, skillLvl);
                    this.dolls.put(Integer.valueOf(id), doll);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Doll> getDolls() {
        return this.dolls;
    }

    public Doll getDollById(int id) {
        return this.dolls.get(Integer.valueOf(id));
    }

    public boolean isDollById(int id) {
        return this.dolls.containsKey(Integer.valueOf(id));
    }

    private static class SingletonHolder {
        protected static final DollsData INSTANCE = new DollsData();
    }
}
