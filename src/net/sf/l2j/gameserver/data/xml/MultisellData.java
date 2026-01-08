/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.ListContainer;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.network.serverpackets.MultiSellList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultisellData implements IXmlReader {
    public static final int PAGE_SIZE = 40;
    private final Map<Integer, ListContainer> _entries = new HashMap<>();

    public MultisellData() {
    }

    public static MultisellData getInstance() {
        return MultisellData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/multisell");
        this.parseFile("./data/xml/multisell/custom");
        LOGGER.info("Loaded {} multisell.", this._entries.size());
    }

    public void parseDocument(Document doc, Path path) {
        int id = path.toFile().getName().replaceAll(".xml", "").hashCode();
        ListContainer list = new ListContainer(id);
        this.forEach(doc, "list", (listNode) -> {
            NamedNodeMap attrs = listNode.getAttributes();
            list.setApplyTaxes(this.parseBoolean(attrs, "applyTaxes", false));
            list.setMaintainEnchantment(this.parseBoolean(attrs, "maintainEnchantment", false));
            this.forEach(listNode, "item", (itemNode) -> {
                List<Ingredient> ingredients = new ArrayList<>();
                List<Ingredient> products = new ArrayList<>();
                this.forEach(itemNode, "ingredient", (ingredientNode) -> {
                    ingredients.add(new Ingredient(this.parseAttributes(ingredientNode)));
                });
                this.forEach(itemNode, "production", (productionNode) -> {
                    products.add(new Ingredient(this.parseAttributes(productionNode)));
                });
                list.getEntries().add(new Entry(ingredients, products));
            });
            this.forEach(listNode, "npcs", (npcsNode) -> {
                this.forEach(npcsNode, "npc", (npcNode) -> {
                    list.allowNpc(Integer.parseInt(npcNode.getTextContent()));
                });
            });
            this._entries.put(id, list);
        });
    }

    public void reload() {
        this._entries.clear();
        this.load();
    }

    public void separateAndSend(String listName, Player player, Npc npc, boolean inventoryOnly) {
        ListContainer template = this._entries.get(listName.hashCode());
        if (template != null) {
            if ((npc == null || template.isNpcAllowed(npc.getNpcId())) && (npc != null || !template.isNpcOnly())) {
                PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
                int index = 0;

                do {
                    player.sendPacket(new MultiSellList(list, index));
                    index += 40;
                } while (index < list.getEntries().size());

                player.setMultiSell(list);
            }
        }
    }

    public ListContainer getList(String listName) {
        return this._entries.get(listName.hashCode());
    }

    private static class SingletonHolder {
        protected static final MultisellData INSTANCE = new MultisellData();
    }
}