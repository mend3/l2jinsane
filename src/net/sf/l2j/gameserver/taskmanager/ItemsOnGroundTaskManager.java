package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemsOnGroundTaskManager implements Runnable {
    private static final CLogger LOGGER = new CLogger(ItemsOnGroundTaskManager.class.getName());

    private static final String LOAD_ITEMS = "SELECT object_id,item_id,count,enchant_level,x,y,z,time FROM items_on_ground";

    private static final String DELETE_ITEMS = "DELETE FROM items_on_ground";

    private static final String SAVE_ITEMS = "INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,time) VALUES(?,?,?,?,?,?,?,?)";

    private final Map<ItemInstance, Long> _items = new ConcurrentHashMap<>();

    public ItemsOnGroundTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 5000L, 5000L);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement st = con.prepareStatement("SELECT object_id,item_id,count,enchant_level,x,y,z,time FROM items_on_ground");
                try {
                    PreparedStatement st2 = con.prepareStatement("DELETE FROM items_on_ground");
                    try {
                        ResultSet rs = st.executeQuery();
                        try {
                            long time = System.currentTimeMillis();
                            while (rs.next()) {
                                ItemInstance item = new ItemInstance(rs.getInt(1), rs.getInt(2));
                                World.getInstance().addObject(item);
                                int count = rs.getInt(3);
                                if (item.isStackable() && count > 1)
                                    item.setCount(count);
                                int enchant = rs.getInt(4);
                                if (enchant > 0)
                                    item.setEnchantLevel(enchant);
                                item.spawnMe(rs.getInt(5), rs.getInt(6), rs.getInt(7));
                                Castle castle = CastleManager.getInstance().getCastle(item);
                                if (castle != null && castle.getTicket(item.getItemId()) != null)
                                    castle.addDroppedTicket(item);
                                long interval = rs.getLong(8);
                                this._items.put(item, Long.valueOf((interval == 0L) ? 0L : (time + interval)));
                            }
                            st2.execute();
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
                        if (st2 != null)
                            st2.close();
                    } catch (Throwable throwable) {
                        if (st2 != null)
                            try {
                                st2.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (st != null)
                        st.close();
                } catch (Throwable throwable) {
                    if (st != null)
                        try {
                            st.close();
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
            LOGGER.error("Error while loading items on ground data.", e);
        }
        LOGGER.info("Restored {} items on ground.", Integer.valueOf(this._items.size()));
    }

    public static ItemsOnGroundTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._items.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<ItemInstance, Long> entry : this._items.entrySet()) {
            long destroyTime = entry.getValue();
            if (destroyTime == 0L || time < destroyTime)
                continue;
            ItemInstance item = entry.getKey();
            item.decayMe();
        }
    }

    public void add(ItemInstance item, Creature actor) {
        if (actor == null || item.isDestroyProtected())
            return;
        long dropTime = 0L;
        Integer special = Config.SPECIAL_ITEM_DESTROY_TIME.get(Integer.valueOf(item.getItemId()));
        if (special != null) {
            dropTime = special;
        } else if (item.isHerb()) {
            dropTime = Config.HERB_AUTO_DESTROY_TIME;
        } else if (item.isEquipable()) {
            dropTime = Config.EQUIPABLE_ITEM_AUTO_DESTROY_TIME;
        } else {
            Castle castle = CastleManager.getInstance().getCastle(item);
            dropTime = (castle != null && castle.getTicket(item.getItemId()) != null) ? 0L : Config.ITEM_AUTO_DESTROY_TIME;
        }
        if (actor instanceof net.sf.l2j.gameserver.model.actor.Playable)
            dropTime *= Config.PLAYER_DROPPED_ITEM_MULTIPLIER;
        if (dropTime != 0L)
            dropTime += System.currentTimeMillis();
        this._items.put(item, Long.valueOf(dropTime));
    }

    public void remove(ItemInstance item) {
        this._items.remove(item);
    }

    public void save() {
        if (this._items.isEmpty()) {
            LOGGER.info("No items on ground to save.");
            return;
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement st = con.prepareStatement("INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,time) VALUES(?,?,?,?,?,?,?,?)");
                try {
                    long time = System.currentTimeMillis();
                    for (Map.Entry<ItemInstance, Long> entry : this._items.entrySet()) {
                        ItemInstance item = entry.getKey();
                        if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
                            continue;
                        st.setInt(1, item.getObjectId());
                        st.setInt(2, item.getItemId());
                        st.setInt(3, item.getCount());
                        st.setInt(4, item.getEnchantLevel());
                        st.setInt(5, item.getX());
                        st.setInt(6, item.getY());
                        st.setInt(7, item.getZ());
                        long left = entry.getValue();
                        st.setLong(8, (left == 0L) ? 0L : (left - time));
                        st.addBatch();
                    }
                    st.executeBatch();
                    if (st != null)
                        st.close();
                } catch (Throwable throwable) {
                    if (st != null)
                        try {
                            st.close();
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
            LOGGER.error("Couldn't save items on ground.", e);
        }
        LOGGER.info("Saved {} items on ground.", Integer.valueOf(this._items.size()));
    }

    private static class SingletonHolder {
        protected static final ItemsOnGroundTaskManager INSTANCE = new ItemsOnGroundTaskManager();
    }
}
