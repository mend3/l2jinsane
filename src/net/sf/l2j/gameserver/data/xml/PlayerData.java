/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.holder.ItemTemplateHolder;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.location.Location;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData implements IXmlReader {
    private final Map<Integer, PlayerTemplate> _templates = new HashMap<>();

    protected PlayerData() {
    }

    public static PlayerData getInstance() {
        return PlayerData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/classes");

        for (PlayerTemplate template : this._templates.values()) {
            ClassId parentClassId = template.getClassId().getParent();
            if (parentClassId != null) {
                template.getSkills().addAll(this._templates.get(parentClassId.getId()).getSkills());
            }
        }
        LOGGER.info("Loaded {} player classes templates.", this._templates.size());

    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "class", (classNode) -> {
                StatSet set = new StatSet();
                this.forEach(classNode, "set", (setNode) -> {
                    set.putAll(this.parseAttributes(setNode));
                });
                this.forEach(classNode, "items", (itemsNode) -> {
                    List<ItemTemplateHolder> items = new ArrayList<>();
                    this.forEach(itemsNode, "item", (itemNode) -> {
                        items.add(new ItemTemplateHolder(this.parseAttributes(itemNode)));
                    });
                    set.set("items", items);
                });
                this.forEach(classNode, "skills", (skillsNode) -> {
                    List<GeneralSkillNode> skills = new ArrayList<>();
                    this.forEach(skillsNode, "skill", (skillNode) -> {
                        skills.add(new GeneralSkillNode(this.parseAttributes(skillNode)));
                    });
                    set.set("skills", skills);
                });
                this.forEach(classNode, "spawns", (spawnsNode) -> {
                    List<Location> locs = new ArrayList<>();
                    this.forEach(spawnsNode, "spawn", (spawnNode) -> {
                        locs.add(new Location(this.parseAttributes(spawnNode)));
                    });
                    set.set("spawnLocations", locs);
                });
                this._templates.put(set.getInteger("id"), new PlayerTemplate(set));
            });
        });
    }

    public PlayerTemplate getTemplate(ClassId classId) {
        return this._templates.get(classId.getId());
    }

    public PlayerTemplate getTemplate(int classId) {
        return this._templates.get(classId);
    }

    public final String getClassNameById(int classId) {
        PlayerTemplate template = this._templates.get(classId);
        return template != null ? template.getClassName() : "Invalid class";
    }

    private static class SingletonHolder {
        protected static final PlayerData INSTANCE = new PlayerData();
    }
}