package net.sf.l2j.gameserver.data;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.CouponTemplate;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CouponsManager implements IXmlReader {
    private static final Logger _log = Logger.getLogger(CouponsManager.class.getName());
    private static final CouponsManager INSTANCE = new CouponsManager();
    private static final String DEFAULT_OWNER = "NO_OWNER";
    private final Map<String, String> _coupons = new HashMap<>();
    private final List<CouponTemplate> _rewards = new ArrayList<>();

    public static CouponsManager getInstance() {
        return INSTANCE;
    }

    public void addItem(CouponTemplate item) {
        _rewards.add(item);
    }

    public final List<CouponTemplate> getRewards() {
        return _rewards;
    }

    public void clear() {
        _rewards.clear();
    }

    public void load() {
        _rewards.clear();
        parseFile("./data/xml/coupons.xml");
        _log.info("Loaded " + _rewards.size() + " Coupons Rewards.");

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM coupons WHERE coupon_owner = 'NO_OWNER'")) {
            try (ResultSet rset = statement.executeQuery()) {
                while (rset.next())
                    _coupons.put(rset.getString("coupon_id").replace("-", "").toUpperCase(), rset.getString("coupon_owner"));
            }
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Coupon issues: ", e);
        } finally {
            _log.info("Coupons loaded: " + _coupons.size());
        }
    }

    public void parseDocument(Document doc, Path path) {
        Node root = doc.getFirstChild();
        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
            if ("item".equalsIgnoreCase(node.getNodeName())) {
                NamedNodeMap attrs = node.getAttributes();
                int itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                int price = 0;
                try {
                    price = Integer.parseInt(attrs.getNamedItem("price").getNodeValue());
                } catch (Exception e) {
                }
                CouponTemplate template = new CouponTemplate(itemId, count, price);
                if (ItemTable.getInstance().getTemplate(template.getId()) != null) {
                    _rewards.add(template);
                } else {
                    _log.log(Level.WARNING, "CouponsManager: Item ID: " + itemId + " doesn't exists in game.");
                }
            }
        }
    }

    public synchronized final boolean tryUseCoupon(Player player, String id) {
        final String acc = player.getAccountName().toLowerCase();
        final Collection<String> accounts = _coupons.values();
        for (String account : accounts) {
            if (account.equals(acc)) {
                player.sendMessage("You may only claim 1 coupon per account.");
                return false;
            }
        }
        final String owner = _coupons.get(id.toUpperCase());
        if (owner == null) {
            player.sendMessage("Invalid coupon code.");
            return false;
        }
        if (!owner.equals(DEFAULT_OWNER)) {
            player.sendMessage("This coupon code has expired or was already used.");
            return false;
        }
        _coupons.put(id, player.getAccountName());
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement statement = con.prepareStatement("UPDATE coupons SET coupon_owner = ? WHERE coupon_id = ?")) {
            statement.setString(1, acc);
            statement.setString(2, id);
            statement.execute();
        } catch (SQLException e) {
            _log.log(Level.WARNING, "Coupons: update failure: " + e);
            return false;
        }
        player.sendMessage("Coupon accepted! Enjoy!");
        player.broadcastPacket(new SocialAction(player, 16));
        for (CouponTemplate it : getRewards()) {
            player.getInventory().addItem("Coupon", it.getId(), it.getCount(), player, null);
        }
        return true;
    }

}
