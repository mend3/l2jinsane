/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.*;

public class HerbDropData implements IXmlReader {
    private final Map<Integer, List<DropCategory>> _herbGroups = new HashMap();

    protected HerbDropData() {
    }

    public static HerbDropData getInstance() {
        return HerbDropData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/herbDrops.xml");
        LOGGER.info("Loaded {} herbs groups.", this._herbGroups.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "group", (groupNode) -> {
                int groupId = this.parseInteger(groupNode.getAttributes(), "id");
                List<DropCategory> category = this._herbGroups.computeIfAbsent(groupId, (k) -> {
                    return new ArrayList();
                });
                this.forEach(groupNode, "item", (itemNode) -> {
                    NamedNodeMap attrs = itemNode.getAttributes();
                    int id = this.parseInteger(attrs, "id");
                    int categoryType = this.parseInteger(attrs, "category");
                    int chance = this.parseInteger(attrs, "chance");
                    DropData dropDat = new DropData();
                    dropDat.setItemId(id);
                    dropDat.setMinDrop(1);
                    dropDat.setMaxDrop(1);
                    dropDat.setChance(chance);
                    boolean catExists = false;
                    Iterator var9 = category.iterator();

                    while (var9.hasNext()) {
                        DropCategory catx = (DropCategory) var9.next();
                        if (catx.getCategoryType() == categoryType) {
                            catx.addDropData(dropDat, false);
                            catExists = true;
                            break;
                        }
                    }

                    if (!catExists) {
                        DropCategory cat = new DropCategory(categoryType);
                        cat.addDropData(dropDat, false);
                        category.add(cat);
                    }

                });
            });
        });
    }

    public List<DropCategory> getHerbDroplist(int groupId) {
        return this._herbGroups.get(groupId);
    }

    private static class SingletonHolder {
        protected static final HerbDropData INSTANCE = new HerbDropData();
    }
}