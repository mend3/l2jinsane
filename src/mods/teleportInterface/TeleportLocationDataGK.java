package mods.teleportInterface;

import mods.xml.XMLDocument;
import net.sf.l2j.commons.util.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TeleportLocationDataGK extends XMLDocument {
    private final Map<Integer, TeleLocation> _teleports = new HashMap<>();

    protected TeleportLocationDataGK() {
    }

    public static TeleportLocationDataGK getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        loadDocument("./data/xml/gkinterface/teleportLocations.xml");
        LOG.info(String.format("Loaded teleport locations: %d", Integer.valueOf(this._teleports.size())));
    }

    protected void parseDocument(Document doc, File file) {
        StatSet set = new StatSet();
        Node n = doc.getFirstChild();
        for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling()) {
            if ("teleport".equalsIgnoreCase(o.getNodeName())) {
                parseAndFeed(o.getAttributes(), set);
                this._teleports.put(Integer.valueOf(set.getInteger("id")), new TeleLocation(set));
                set.clear();
            }
        }
    }

    public void reload() {
        this._teleports.clear();
        load();
    }

    public TeleLocation getTeleportLocation(int id) {
        return this._teleports.get(Integer.valueOf(id));
    }

    private static class SingletonHolder {
        protected static final TeleportLocationDataGK INSTANCE = new TeleportLocationDataGK();
    }
}
