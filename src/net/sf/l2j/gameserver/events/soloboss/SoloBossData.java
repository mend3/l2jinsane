package net.sf.l2j.gameserver.events.soloboss;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.*;

public class SoloBossData implements IXmlReader {
    private static final CLogger LOGGER = new CLogger(SoloBossData.class.getName());

    private final Map<String, List<SoloBossCreatureHolder>> _templates = new HashMap<>();

    private final Map<String, String> _messages = new HashMap<>();

    public SoloBossData() {
    }

    public static SoloBossData getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {
        this._templates.clear();
        parseFile("./data/xml/soloboss.xml");
        LOGGER.info("Loaded " + this._templates.size() + " solo boss events.");
    }

    public void reload() {
    }

    public void parseDocument(Document doc, Path path) {
        for (Node eventNode = doc.getFirstChild(); eventNode != null; eventNode = eventNode.getNextSibling()) {
            if ("soloboss".equalsIgnoreCase(eventNode.getNodeName()))
                for (Node event = eventNode.getFirstChild(); event != null; event = event.getNextSibling()) {
                    if ("event".equalsIgnoreCase(event.getNodeName())) {
                        String hours = event.getAttributes().getNamedItem("hour").getNodeValue();
                        String msg = event.getAttributes().getNamedItem("message").getNodeValue();
                        List<SoloBossCreatureHolder> bosses = new ArrayList<>();
                        for (Node npc = event.getFirstChild(); npc != null; npc = npc.getNextSibling()) {
                            if ("npc".equalsIgnoreCase(npc.getNodeName())) {
                                NamedNodeMap attributes = npc.getAttributes();
                                int id = Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
                                int x = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
                                int y = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
                                int z = Integer.parseInt(attributes.getNamedItem("z").getNodeValue());
                                bosses.add(new SoloBossCreatureHolder(id, x, y, z));
                            }
                        }
                        this._templates.put(hours, bosses);
                        this._messages.put(hours, msg);
                    }
                }
        }
    }

    public List<SoloBossCreatureHolder> getBossesForHour(String hour) {
        return this._templates.get(hour);
    }

    public Set<String> getEventHours() {
        return this._templates.keySet();
    }

    public String getMessageEvent(String hour) {
        return this._messages.get(hour);
    }

    public Set<String> getMessages() {
        return this._messages.keySet();
    }

    private static class SingletonHolder {
        protected static final SoloBossData _instance = new SoloBossData();
    }
}
