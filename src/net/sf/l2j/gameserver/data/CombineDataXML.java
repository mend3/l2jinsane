package net.sf.l2j.gameserver.data;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CombineDataXML implements IXmlReader {
    private static final CLogger LOGGER = new CLogger(CombineDataXML.class.getName());

    private final Map<Integer, CombineRecipe> _recipes = new HashMap<>();

    public CombineDataXML() {
    }

    public static CombineDataXML getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {
        parseFile("./data/xml/combineItems.xml");
        LOGGER.info("Loaded " + this._recipes.size() + " combine item(s).");
    }

    public void parseDocument(Document doc, Path path) {
        Node listNode = null;
        Node node;
        for (node = doc.getFirstChild(); node != null; node = node.getNextSibling()) {
            if ("list".equalsIgnoreCase(node.getNodeName())) {
                listNode = node;
                break;
            }
        }
        if (listNode == null) {
            LOGGER.warn("No 'list' node found in XML file: {}", path);
            return;
        }
        for (node = listNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1 && "combine".equals(node.getNodeName()))
                parseCombineNode(node);
        }
    }

    private void parseCombineNode(Node combineNode) {
        int[] values = new int[9];
        for (Node node = combineNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null)
                    switch (node.getNodeName()) {
                        case "item1":
                            values[0] = parseInteger(attrs, "id");
                            values[1] = parseInteger(attrs, "count");
                            break;
                        case "item2":
                            values[2] = parseInteger(attrs, "id");
                            values[3] = parseInteger(attrs, "count");
                            break;
                        case "result":
                            values[4] = parseInteger(attrs, "id");
                            values[5] = parseInteger(attrs, "count");
                            break;
                        case "item3":
                            values[6] = parseInteger(attrs, "id");
                            values[7] = parseInteger(attrs, "count");
                            break;
                        case "chance":
                            values[8] = parseInteger(attrs, "value");
                            break;
                    }
            }
        }
        try {
            CombineRecipe recipe = new CombineRecipe(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
            this._recipes.put(this._recipes.size() + 1, recipe);
        } catch (Exception e) {
            LOGGER.error("Error creating recipe in CombineDataXML: ", e);
        }
    }

    public Map<Integer, CombineRecipe> getRecipes() {
        return this._recipes;
    }

    public ArrayList<CombineRecipe> getRecipesAsList() {
        return new ArrayList<>(this._recipes.values());
    }

    public record CombineRecipe(int item1, int count1, int item2, int count2, int result, int countResult, int item3,
                                int count3, int chance) {
        public CombineRecipe {
            if (item1 <= 0 || item2 <= 0 || result <= 0 || item3 <= 0)
                throw new IllegalArgumentException("Item IDs must be positive");
            if (count1 <= 0 || count2 <= 0 || countResult <= 0 || count3 <= 0)
                throw new IllegalArgumentException("Counts must be positive");
            if (chance < 0 || chance > 100)
                throw new IllegalArgumentException("Chance must be between 0 and 100");
        }

        public String toString() {
            return "CombineRecipe[items= %d + %d + %d ->, %d, chance= %d%%]"
                    .formatted(this.item1, this.item2, this.item3, this.result, this.chance);
        }
    }

    private static class SingletonHolder {
        protected static final CombineDataXML _instance = new CombineDataXML();
    }
}
