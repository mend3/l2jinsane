package enginemods.main.data;

import enginemods.main.enums.BuffType;
import enginemods.main.holders.BuffHolder;
import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.commons.logging.CLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchemeBuffData {
    private static final CLogger LOGGER = new CLogger(SchemeBuffData.class.getName());
    private static final List<BuffHolder> _generalBuffs = new ArrayList<>();

    private static final List<BuffHolder> _warriorBuffs = new ArrayList<>();

    private static final List<BuffHolder> _mageBuffs = new ArrayList<>();

    public static void load() {
        _generalBuffs.clear();
        _warriorBuffs.clear();
        _mageBuffs.clear();
        loadBuffs();
        loadMageBuffs();
        loadWarriorBuffs();
    }

    private static void loadBuffs() {
        try {
            File f = new File("./data/xml/engine/scheme_buffer/generalBuffs.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("buff")) {
                    NamedNodeMap attrs = d.getAttributes();
                    BuffType type = BuffType.valueOf(attrs.getNamedItem("type").getNodeValue());
                    int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    int lvl = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());
                    _generalBuffs.add(new BuffHolder(type, id, lvl));
                }
            }
            LOGGER.info("SchemeBuffData: Load " + _generalBuffs.size() + " buffs.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadMageBuffs() {
        try {
            File f = new File("./data/xml/engine/scheme_buffer/setMageBuffs.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("buff")) {
                    NamedNodeMap attrs = d.getAttributes();
                    int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    int lvl = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());
                    _mageBuffs.add(new BuffHolder(id, lvl));
                }
            }
            LOGGER.info("SchemeBuffData: Load " + _mageBuffs.size() + " set mage buffs.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadWarriorBuffs() {
        try {
            File f = new File("./data/xml/engine/scheme_buffer/setWarriorBuffs.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("buff")) {
                    NamedNodeMap attrs = d.getAttributes();
                    int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    int lvl = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());
                    _warriorBuffs.add(new BuffHolder(id, lvl));
                }
            }
            LOGGER.info("SchemeBuffData: Load " + _warriorBuffs.size() + " set warrior buffs.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<BuffHolder> getAllMageBuffs() {
        return _mageBuffs;
    }

    public static List<BuffHolder> getAllWarriorBuffs() {
        return _warriorBuffs;
    }

    public static List<BuffHolder> getAllGeneralBuffs() {
        return _generalBuffs;
    }
}
