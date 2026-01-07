/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.location.WalkerLocation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkerRouteData implements IXmlReader {
    private final Map<Integer, List<WalkerLocation>> _routes = new HashMap();

    protected WalkerRouteData() {
    }

    public static WalkerRouteData getInstance() {
        return WalkerRouteData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/walkerRoutes.xml");
        LOGGER.info("Loaded {} Walker routes.", this._routes.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "route", (routeNode) -> {
                NamedNodeMap attrs = routeNode.getAttributes();
                List<WalkerLocation> list = new ArrayList();
                int npcId = this.parseInteger(attrs, "npcId");
                boolean run = this.parseBoolean(attrs, "run");
                this.forEach(routeNode, "node", (nodeNode) -> {
                    list.add(new WalkerLocation(this.parseAttributes(nodeNode), run));
                });
                this._routes.put(npcId, list);
            });
        });
    }

    public void reload() {
        this._routes.clear();
        this.load();
    }

    public List<WalkerLocation> getWalkerRoute(int npcId) {
        return this._routes.get(npcId);
    }

    private static class SingletonHolder {
        protected static final WalkerRouteData INSTANCE = new WalkerRouteData();
    }
}