/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SummonItemData implements IXmlReader {
    private final Map<Integer, IntIntHolder> _items = new HashMap<>();

    protected SummonItemData() {
    }

    public static SummonItemData getInstance() {
        return SummonItemData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/summonItems.xml");
        LOGGER.info("Loaded {} summon items.", this._items.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "item", (itemNode) -> {
            NamedNodeMap attrs = itemNode.getAttributes();
            int itemId = this.parseInteger(attrs, "id");
            int npcId = this.parseInteger(attrs, "npcId");
            int summonType = this.parseInteger(attrs, "summonType");
            this._items.put(itemId, new IntIntHolder(npcId, summonType));
        }));
    }

    public IntIntHolder getSummonItem(int itemId) {
        return this._items.get(itemId);
    }

    private static class SingletonHolder {
        protected static final SummonItemData INSTANCE = new SummonItemData();
    }
}