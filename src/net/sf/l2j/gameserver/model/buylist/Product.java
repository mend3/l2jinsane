package net.sf.l2j.gameserver.model.buylist;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.taskmanager.BuyListTaskManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private static final CLogger LOGGER = new CLogger(Product.class.getName());

    private static final String ADD_OR_UPDATE_BUYLIST = "INSERT INTO buylists (buylist_id,item_id,count,next_restock_time) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count), next_restock_time=VALUES(next_restock_time)";

    private static final String DELETE_BUYLIST = "DELETE FROM buylists WHERE buylist_id=? AND item_id=?";

    private final int _buyListId;

    private final Item _item;

    private final int _price;

    private final long _restockDelay;

    private final int _maxCount;

    private AtomicInteger _count = null;

    public Product(int buyListId, StatSet set) {
        this._buyListId = buyListId;
        this._item = ItemTable.getInstance().getTemplate(set.getInteger("id"));
        this._price = set.getInteger("price", 0);
        this._restockDelay = set.getLong("restockDelay", -1L) * 60000L;
        this._maxCount = set.getInteger("count", -1);
        if (hasLimitedStock())
            this._count = new AtomicInteger(this._maxCount);
    }

    public int getBuyListId() {
        return this._buyListId;
    }

    public Item getItem() {
        return this._item;
    }

    public int getItemId() {
        return this._item.getItemId();
    }

    public int getPrice() {
        return this._price;
    }

    public long getRestockDelay() {
        return this._restockDelay;
    }

    public int getMaxCount() {
        return this._maxCount;
    }

    public int getCount() {
        if (this._count == null)
            return 0;
        int count = this._count.get();
        return Math.max(count, 0);
    }

    public void setCount(int currentCount) {
        this._count.set(currentCount);
    }

    public boolean decreaseCount(int val) {
        if (this._count == null)
            return false;
        boolean result = (this._count.addAndGet(-val) >= 0);
        if (result)
            BuyListTaskManager.getInstance().add(this, getRestockDelay());
        return result;
    }

    public boolean hasLimitedStock() {
        return (this._maxCount > -1);
    }

    public void save(long nextRestockTime) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO buylists (buylist_id,item_id,count,next_restock_time) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count), next_restock_time=VALUES(next_restock_time)");
                try {
                    ps.setInt(1, getBuyListId());
                    ps.setInt(2, getItemId());
                    ps.setInt(3, getCount());
                    ps.setLong(4, nextRestockTime);
                    ps.executeUpdate();
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
            LOGGER.error("Couldn't save product for buylist id:{} and item id: {}.", e, getBuyListId(), getItemId());
        }
    }

    public void delete() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM buylists WHERE buylist_id=? AND item_id=?");
                try {
                    ps.setInt(1, getBuyListId());
                    ps.setInt(2, getItemId());
                    ps.executeUpdate();
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
            LOGGER.error("Couldn't delete product for buylist id:{} and item id: {}.", e, getBuyListId(), getItemId());
        }
    }
}
