package enginemods.main.data;

import enginemods.main.xmlfactory.XMLDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SkillData {
    private static final Logger LOG = Logger.getLogger(SkillData.class.getName());

    private static final Map<String, String> SKILLS = new HashMap<>();

    public static void load() {
        SKILLS.clear();
        try {
            File f = new File("./data/xml/modsSkill.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("skill")) {
                    NamedNodeMap attrs = d.getAttributes();
                    String id = attrs.getNamedItem("id").getNodeValue();
                    String level = attrs.getNamedItem("level").getNodeValue();
                    String description = attrs.getNamedItem("description").getNodeValue();
                    SKILLS.put(id + " " + level, description);
                }
            }
            LOG.info(SkillData.class.getSimpleName() + " load " + SkillData.class.getSimpleName() + " skills data.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDescription(int id, int lvl) {
        return SKILLS.get(id + " " + lvl);
    }

    public static String getSkillIcon(int id) {
        String formato;
        if (id == 4) {
            formato = "0004";
        } else if (id > 9 && id < 100) {
            formato = "00" + id;
        } else if (id > 99 && id < 1000) {
            formato = "0" + id;
        } else if (id == 1517) {
            formato = "1536";
        } else if (id == 1518) {
            formato = "1537";
        } else if (id == 1547) {
            formato = "0065";
        } else if (id == 2076) {
            formato = "0195";
        } else if (id > 4550 && id < 4555) {
            formato = "5739";
        } else if (id > 4698 && id < 4701) {
            formato = "1331";
        } else if (id > 4701 && id < 4704) {
            formato = "1332";
        } else if (id == 6049) {
            formato = "0094";
        } else {
            formato = String.valueOf(id);
        }
        return "Icon.skill" + formato;
    }
}
