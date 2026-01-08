/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TeleportLocationData implements IXmlReader {
    private final Map<Integer, TeleportLocation> _teleports = new HashMap<>();

    protected TeleportLocationData() {
    }

    public static TeleportLocationData getInstance() {
        return TeleportLocationData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/teleportLocations.xml");
        LOGGER.info("Loaded {} teleport locations.", this._teleports.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "teleport", (teleportNode) -> {
                StatSet set = this.parseAttributes(teleportNode);
                this._teleports.put(set.getInteger("id"), new TeleportLocation(set));
            });
        });
    }

    public void reload() {
        this._teleports.clear();
        this.load();
    }

    public TeleportLocation getTeleportLocation(int id) {
        return this._teleports.get(id);
    }

    private static class SingletonHolder {
        protected static final TeleportLocationData INSTANCE = new TeleportLocationData();
    }
}