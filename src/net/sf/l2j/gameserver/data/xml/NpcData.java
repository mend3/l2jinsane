/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.PetDataEntry;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.PetTemplate;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NpcData implements IXmlReader {
    private final Map<Integer, NpcTemplate> _npcs = new HashMap<>();

    protected NpcData() {
    }

    public static NpcData getInstance() {
        return NpcData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/npcs");
        this.parseFile("./data/xml/npcs/custom");
        this.parseFile("./data/xml/npcs/raidboss");
        this.parseFile("./data/xml/npcs/grandboss");
        this.parseFile("./data/xml/npcs/farmzone");
        this.parseFile("./data/xml/npcs/events");
        LOGGER.info("Loaded {} NPC templates.", this._npcs.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "npc", (npcNode) -> {
                NamedNodeMap attrs = npcNode.getAttributes();
                int npcId = this.parseInteger(attrs, "id");
                int templateId = attrs.getNamedItem("idTemplate") == null ? npcId : this.parseInteger(attrs, "idTemplate");
                StatSet set = new StatSet();
                set.set("id", npcId);
                set.set("idTemplate", templateId);
                set.set("name", this.parseString(attrs, "name"));
                set.set("title", this.parseString(attrs, "title"));
                this.forEach(npcNode, "set", (setNode) -> {
                    NamedNodeMap setAttrs = setNode.getAttributes();
                    set.set(this.parseString(setAttrs, "name"), this.parseString(setAttrs, "val"));
                });
                this.forEach(npcNode, "ai", (aiNode) -> {
                    NamedNodeMap aiAttrs = aiNode.getAttributes();
                    set.set("aiType", this.parseString(aiAttrs, "type"));
                    set.set("ssCount", this.parseInteger(aiAttrs, "ssCount"));
                    set.set("ssRate", this.parseInteger(aiAttrs, "ssRate"));
                    set.set("spsCount", this.parseInteger(aiAttrs, "spsCount"));
                    set.set("spsRate", this.parseInteger(aiAttrs, "spsRate"));
                    set.set("aggro", this.parseInteger(aiAttrs, "aggro"));
                    if (aiAttrs.getNamedItem("clan") != null) {
                        set.set("clan", this.parseString(aiAttrs, "clan").split(";"));
                        set.set("clanRange", this.parseInteger(aiAttrs, "clanRange"));
                        if (aiAttrs.getNamedItem("ignoredIds") != null) {
                            set.set("ignoredIds", this.parseString(aiAttrs, "ignoredIds"));
                        }
                    }

                    set.set("canMove", this.parseBoolean(aiAttrs, "canMove"));
                    set.set("seedable", this.parseBoolean(aiAttrs, "seedable"));
                });
                this.forEach(npcNode, "drops", (dropsNode) -> {
                    String type = set.getString("type");
                    boolean isRaid = type.equalsIgnoreCase("RaidBoss") || type.equalsIgnoreCase("GrandBoss");
                    List<DropCategory> drops = new ArrayList<>();
                    this.forEach(dropsNode, "category", (categoryNode) -> {
                        NamedNodeMap categoryAttrs = categoryNode.getAttributes();
                        DropCategory category = new DropCategory(this.parseInteger(categoryAttrs, "id"));
                        this.forEach(categoryNode, "drop", (dropNode) -> {
                            NamedNodeMap dropAttrs = dropNode.getAttributes();
                            DropData data = new DropData();
                            data.setItemId(this.parseInteger(dropAttrs, "itemid"));
                            data.setMinDrop(this.parseInteger(dropAttrs, "min"));
                            data.setMaxDrop(this.parseInteger(dropAttrs, "max"));
                            data.setChance(this.parseInteger(dropAttrs, "chance"));
                            if (ItemTable.getInstance().getTemplate(data.getItemId()) == null) {
                                LOGGER.warn("Droplist data for undefined itemId: {}.", data.getItemId());
                            } else {
                                category.addDropData(data, isRaid);
                            }
                        });
                        drops.add(category);
                    });
                    set.set("drops", drops);
                });
                this.forEach(npcNode, "minions", (minionsNode) -> {
                    List<MinionData> minions = new ArrayList<>();
                    this.forEach(minionsNode, "minion", (minionNode) -> {
                        NamedNodeMap minionAttrs = minionNode.getAttributes();
                        MinionData data = new MinionData();
                        data.setMinionId(this.parseInteger(minionAttrs, "id"));
                        data.setAmountMin(this.parseInteger(minionAttrs, "min"));
                        data.setAmountMax(this.parseInteger(minionAttrs, "max"));
                        minions.add(data);
                    });
                    set.set("minions", minions);
                });
                this.forEach(npcNode, "petdata", (petdataNode) -> {
                    NamedNodeMap petdataAttrs = petdataNode.getAttributes();
                    set.set("mustUsePetTemplate", true);
                    set.set("food1", this.parseInteger(petdataAttrs, "food1"));
                    set.set("food2", this.parseInteger(petdataAttrs, "food2"));
                    set.set("autoFeedLimit", this.parseDouble(petdataAttrs, "autoFeedLimit"));
                    set.set("hungryLimit", this.parseDouble(petdataAttrs, "hungryLimit"));
                    set.set("unsummonLimit", this.parseDouble(petdataAttrs, "unsummonLimit"));
                    Map<Integer, PetDataEntry> entries = new HashMap<>();
                    this.forEach(petdataNode, "stat", (statNode) -> {
                        StatSet petSet = this.parseAttributes(statNode);
                        entries.put(petSet.getInteger("level"), new PetDataEntry(petSet));
                    });
                    set.set("petData", entries);
                });
                this.forEach(npcNode, "skills", (skillsNode) -> {
                    List<L2Skill> skills = new ArrayList<>();
                    this.forEach(skillsNode, "skill", (skillNode) -> {
                        NamedNodeMap skillAttrs = skillNode.getAttributes();
                        int skillId = this.parseInteger(skillAttrs, "id");
                        int level = this.parseInteger(skillAttrs, "level");
                        if (skillId == 4416) {
                            set.set("raceId", level);
                        } else {
                            L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
                            if (skill != null) {
                                skills.add(skill);
                            }
                        }
                    });
                    set.set("skills", skills);
                });
                this.forEach(npcNode, "teachTo", (teachToNode) -> {
                    set.set("teachTo", this.parseString(teachToNode.getAttributes(), "classes"));
                });
                this._npcs.put(npcId, set.getBool("mustUsePetTemplate", false) ? new PetTemplate(set) : new NpcTemplate(set));
            });
        });
    }

    public void reload() {
        this._npcs.clear();
        this.load();
    }

    public NpcTemplate getTemplate(int id) {
        return this._npcs.get(id);
    }

    public NpcTemplate getTemplateByName(String name) {
        return this._npcs.values().stream().filter((t) -> {
            return t.getName().equalsIgnoreCase(name);
        }).findFirst().orElse(null);
    }

    public List<NpcTemplate> getTemplates(Predicate<NpcTemplate> filter) {
        return this._npcs.values().stream().filter(filter).collect(Collectors.toList());
    }

    public Collection<NpcTemplate> getAllNpcs() {
        return this._npcs.values();
    }

    private static class SingletonHolder {
        protected static final NpcData INSTANCE = new NpcData();
    }
}