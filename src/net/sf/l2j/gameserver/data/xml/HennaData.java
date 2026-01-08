/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HennaData implements IXmlReader {
    private final Map<Integer, Henna> _hennas = new HashMap<>();

    protected HennaData() {
    }

    public static HennaData getInstance() {
        return HennaData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/hennas.xml");
        LOGGER.info("Loaded {} hennas.", this._hennas.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "henna", (hennaNode) -> {
            StatSet set = this.parseAttributes(hennaNode);
            this._hennas.put(set.getInteger("symbolId"), new Henna(set));
        }));
    }

    public Collection<Henna> getHennas() {
        return this._hennas.values();
    }

    public Henna getHenna(int id) {
        return this._hennas.get(id);
    }

    public List<Henna> getAvailableHennasFor(Player player) {
        return this._hennas.values().stream().filter((h) -> h.canBeUsedBy(player)).collect(Collectors.toList());
    }

    private static class SingletonHolder {
        protected static final HennaData INSTANCE = new HennaData();
    }
}