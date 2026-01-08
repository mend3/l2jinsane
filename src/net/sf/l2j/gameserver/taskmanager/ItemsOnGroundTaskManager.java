package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
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

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement st = con.prepareStatement("SELECT object_id,item_id,count,enchant_level,x,y,z,time FROM items_on_ground");
                PreparedStatement st2 = con.prepareStatement("DELETE FROM items_on_ground");
                ResultSet rs = st.executeQuery()
        ) {
            long time = System.currentTimeMillis();

            while (rs.next()) {
                ItemInstance item = new ItemInstance(rs.getInt(1), rs.getInt(2));
                World.getInstance().addObject(item);
                int count = rs.getInt(3);
                if (item.isStackable() && count > 1) {
                    item.setCount(count);
                }

                int enchant = rs.getInt(4);
                if (enchant > 0) {
                    item.setEnchantLevel(enchant);
                }

                item.spawnMe(rs.getInt(5), rs.getInt(6), rs.getInt(7));
                Castle castle = CastleManager.getInstance().getCastle(item);
                if (castle != null && castle.getTicket(item.getItemId()) != null) {
                    castle.addDroppedTicket(item);
                }

                long interval = rs.getLong(8);
                this._items.put(item, interval == 0L ? 0L : time + interval);
            }

            st2.execute();
        } catch (Exception e) {
            LOGGER.error("Error while loading items on ground data.", e);
        }

        LOGGER.info("Restored {} items on ground.", this._items.size());
    }

    public static ItemsOnGroundTaskManager getInstance() {
        return ItemsOnGroundTaskManager.SingletonHolder.INSTANCE;
    }

    public void run() {
        if (!this._items.isEmpty()) {
            long time = System.currentTimeMillis();

            for (Map.Entry<ItemInstance, Long> entry : this._items.entrySet()) {
                long destroyTime = entry.getValue();
                if (destroyTime != 0L && time >= destroyTime) {
                    ItemInstance item = entry.getKey();
                    item.decayMe();
                }
            }

        }
    }

    public void add(ItemInstance item, Creature actor) {
        if (actor != null && !item.isDestroyProtected()) {
            long dropTime = 0L;
            Integer special = Config.SPECIAL_ITEM_DESTROY_TIME.get(item.getItemId());
            if (special != null) {
                dropTime = (long) special;
            } else if (item.isHerb()) {
                dropTime = Config.HERB_AUTO_DESTROY_TIME;
            } else if (item.isEquipable()) {
                dropTime = Config.EQUIPABLE_ITEM_AUTO_DESTROY_TIME;
            } else {
                Castle castle = CastleManager.getInstance().getCastle(item);
                dropTime = castle != null && castle.getTicket(item.getItemId()) != null ? 0L : (long) Config.ITEM_AUTO_DESTROY_TIME;
            }

            if (actor instanceof Playable) {
                dropTime *= Config.PLAYER_DROPPED_ITEM_MULTIPLIER;
            }

            if (dropTime != 0L) {
                dropTime += System.currentTimeMillis();
            }

            this._items.put(item, dropTime);
        }
    }

    public void remove(ItemInstance item) {
        this._items.remove(item);
    }

    public void save() {
        if (this._items.isEmpty()) {
            LOGGER.info("No items on ground to save.");
        } else {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement st = con.prepareStatement("INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,time) VALUES(?,?,?,?,?,?,?,?)")
            ) {
                long time = System.currentTimeMillis();

                for (Map.Entry<ItemInstance, Long> entry : this._items.entrySet()) {
                    ItemInstance item = entry.getKey();
                    if (!CursedWeaponManager.getInstance().isCursed(item.getItemId())) {
                        st.setInt(1, item.getObjectId());
                        st.setInt(2, item.getItemId());
                        st.setInt(3, item.getCount());
                        st.setInt(4, item.getEnchantLevel());
                        st.setInt(5, item.getX());
                        st.setInt(6, item.getY());
                        st.setInt(7, item.getZ());
                        long left = entry.getValue();
                        st.setLong(8, left == 0L ? 0L : left - time);
                        st.addBatch();
                    }
                }

                st.executeBatch();
            } catch (Exception e) {
                LOGGER.error("Couldn't save items on ground.", e);
            }

            LOGGER.info("Saved {} items on ground.", this._items.size());
        }
    }

    private static class SingletonHolder {
        protected static final ItemsOnGroundTaskManager INSTANCE = new ItemsOnGroundTaskManager();
    }
}
