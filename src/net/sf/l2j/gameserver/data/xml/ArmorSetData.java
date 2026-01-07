/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArmorSetData implements IXmlReader {
    private final Map<Integer, ArmorSet> _armorSets = new HashMap();

    protected ArmorSetData() {
    }

    public static ArmorSetData getInstance() {
        return ArmorSetData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/armorSets.xml");
        LOGGER.info("Loaded {} armor sets.", this._armorSets.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "armorset", (armorsetNode) -> {
                StatSet set = this.parseAttributes(armorsetNode);
                this._armorSets.put(set.getInteger("chest"), new ArmorSet(set));
            });
        });
    }

    public ArmorSet getSet(int chestId) {
        return this._armorSets.get(chestId);
    }

    public Collection<ArmorSet> getSets() {
        return this._armorSets.values();
    }

    private static class SingletonHolder {
        protected static final ArmorSetData INSTANCE = new ArmorSetData();
    }
}