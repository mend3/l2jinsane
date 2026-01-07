package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.taskmanager.BuyListTaskManager;
import org.w3c.dom.Document;

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
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `buylists`");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int buyListId = rs.getInt("buylist_id");
                            int itemId = rs.getInt("item_id");
                            int count = rs.getInt("count");
                            long nextRestockTime = rs.getLong("next_restock_time");
                            NpcBuyList buyList = this._buyLists.get(Integer.valueOf(buyListId));
                            if (buyList == null)
                                continue;
                            Product product = buyList.getProductByItemId(itemId);
                            if (product == null)
                                continue;
                            BuyListTaskManager.getInstance().test(product, count, nextRestockTime);
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load buyList data from database.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "buyList", nnn -> {
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
