package net.sf.l2j.gameserver.data;

import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.model.L2EnchantScroll;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EnchantTable {
    private static final Map<Integer, L2EnchantScroll> _map = new HashMap<>();
    private static final Logger _log = Logger.getLogger(EnchantTable.class.getName());

    protected EnchantTable() {
    }

    public static EnchantTable getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {

        try {
            File f = new File("./data/xml/enchants.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName()))
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("enchant".equalsIgnoreCase(d.getNodeName())) {
                            NamedNodeMap attrs = d.getAttributes();
                            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                            byte grade = Byte.parseByte(attrs.getNamedItem("grade").getNodeValue());
                            boolean weapon = Boolean.parseBoolean(attrs.getNamedItem("weapon").getNodeValue());
                            boolean breaks = Boolean.parseBoolean(attrs.getNamedItem("break").getNodeValue());
                            boolean maintain = Boolean.parseBoolean(attrs.getNamedItem("maintain").getNodeValue());
                            String[] list = attrs.getNamedItem("chance").getNodeValue().split(";");
                            byte[] chance = new byte[list.length];
                            for (int i = 0; i < list.length; i++)
                                chance[i] = Byte.parseByte(list[i]);
                            CrystalType grade_test = CrystalType.NONE;
                            grade_test = switch (grade) {
                                case 1 -> CrystalType.D;
                                case 2 -> CrystalType.C;
                                case 3 -> CrystalType.B;
                                case 4 -> CrystalType.A;
                                case 5 -> CrystalType.S;
                                default -> grade_test;
                            };
                            _map.put(id, new L2EnchantScroll(grade_test, weapon, breaks, maintain, chance));
                        }
                    }
            }
            _log.info("EnchantTable: Loaded " + _map.size() + " enchants.");
        } catch (Exception e) {
            _log.warning("EnchantTable: Error while loading enchant table: " + e);
        }
    }

    public L2EnchantScroll getEnchantScroll(ItemInstance item) {
        return _map.get(item.getItemId());
    }

    private static class SingletonHolder {
        protected static final EnchantTable _instance = new EnchantTable();
    }
}
