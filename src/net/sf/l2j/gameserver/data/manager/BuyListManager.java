package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.taskmanager.BuyListTaskManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuyListManager implements IXmlReader {
    private final Map<Integer, NpcBuyList> _buyLists = new HashMap<>();

    protected BuyListManager() {
    }

    public static BuyListManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        parseFile("./data/xml/buyLists.xml");
        LOGGER.info("Loaded {} buyLists.", Integer.valueOf(this._buyLists.size()));

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM `buylists`");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final NpcBuyList buyList = _buyLists.get(rs.getInt("buylist_id"));
                if (buyList == null)
                    continue;

                final Product product = buyList.getProductByItemId(rs.getInt("item_id"));
                if (product == null)
                    continue;

                BuyListTaskManager.getInstance().test(product, rs.getInt("count"), rs.getLong("next_restock_time"));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load buyList data from database.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "buyList", buyListNode -> {
            final NamedNodeMap attrs = buyListNode.getAttributes();
            final int buyListId = parseInteger(attrs, "id");
            final NpcBuyList buyList = new NpcBuyList(buyListId);
            buyList.setNpcId(parseInteger(attrs, "npcId"));
            forEach(buyListNode, "product", productNode -> buyList.addProduct(new Product(buyListId, parseAttributes(productNode))));
            _buyLists.put(buyListId, buyList);
        }));
    }

    public NpcBuyList getBuyList(int listId) {
        return this._buyLists.get(Integer.valueOf(listId));
    }

    public List<NpcBuyList> getBuyListsByNpcId(int npcId) {
        return this._buyLists.values().stream().filter(b -> b.isNpcAllowed(npcId)).collect(Collectors.toList());
    }

    private static class SingletonHolder {
        protected static final BuyListManager INSTANCE = new BuyListManager();
    }
}
