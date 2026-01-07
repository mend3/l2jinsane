/**/
package net.sf.l2j.gameserver.data.xml;

import enginemods.main.enums.ItemIconType;
import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.commons.random.Rnd;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IconsTable {
    private static final Map<Integer, String> Icons = new HashMap<>();
    private static final Logger _log = Logger.getLogger(IconsTable.class.getName());

    public static void load() {
        long startTime = System.currentTimeMillis();

        try {
            File f = new File("./data/xml/icons.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if (d.getNodeName().equalsIgnoreCase("icon")) {
                            NamedNodeMap attrs = d.getAttributes();
                            Node att = attrs.getNamedItem("Id");
                            Node att2 = attrs.getNamedItem("value");
                            Icons.put(Integer.valueOf(att.getNodeValue()), String.valueOf(att2.getNodeValue()));
                        }
                    }
                }
            }

            _log.config("IconsTable: Succesfully loaded " + Icons.size() + " icons, in " + ((double) (System.currentTimeMillis() - startTime)) + " ms.");
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

    public static String getIconByItemId(int itemId) {
        return Icons.get(itemId);
    }

    public static String getRandomItemType(ItemIconType itemIconType, int rnd) {
        String returnIcon = "";
        while (returnIcon.isEmpty()) {
            for (String icon : Icons.values()) {
                if (icon.startsWith(itemIconType.getSearchItem()))
                    if (Rnd.get(rnd) == 0 && !icon.equals("Icon.NOIMAGE") && !icon.equals("Icon.weapon_monster_i00"))
                        returnIcon = icon;
            }
        }
        return returnIcon;
    }

    public static void reload() {
        Icons.clear();
        load();
    }
}