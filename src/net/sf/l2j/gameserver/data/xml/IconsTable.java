/**/
package net.sf.l2j.gameserver.data.xml;

import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.gameserver.GameServer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IconsTable {
    public static final Map<Integer, String> Icons = new HashMap();
    private static final Logger _log = Logger.getLogger(GameServer.class.getName());
    private static int count;
    private static long t0;
    private static double t;

    public static void parseData() {
        count = 0;
        t0 = System.currentTimeMillis();

        try {
            File f = new File("./data/xml/icons.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if (d.getNodeName().equalsIgnoreCase("icon")) {
                            ++count;
                            NamedNodeMap attrs = d.getAttributes();
                            Node att = attrs.getNamedItem("Id");
                            Node att2 = attrs.getNamedItem("value");
                            Icons.put(Integer.valueOf(att.getNodeValue()), String.valueOf(att2.getNodeValue()));
                        }
                    }
                }
            }

            t = (double) (System.currentTimeMillis() - t0);
            _log.config("IconsTable: Succesfully loaded " + count + " icons, in " + t + " Milliseconds.");
        } catch (Exception var7) {
            _log.config("IconsTable: Failed loading IconsTable. Possible error: " + var7.getMessage());
        }

    }

    public static String getIcon(int id) {
        if (Icons.get(id) == null) {
            _log.config("IconsTable: Invalid Icon request: " + id + ", or it doesn't exist, Ignoring ...");
            return "null";
        } else {
            return Icons.get(id);
        }
    }

    public static IconsTable getInstance() {
        return IconsTable.SingletonHolder._instance;
    }

    public void load() {
        parseData();
    }

    public void reload() {
        Icons.clear();
        parseData();
    }

    private static class SingletonHolder {
        protected static final IconsTable _instance = new IconsTable();
    }
}