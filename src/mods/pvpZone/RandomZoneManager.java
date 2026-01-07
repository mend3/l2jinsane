package mods.pvpZone;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.type.L2RandomZone;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class RandomZoneManager implements Runnable {
    private static final Logger LOG = Logger.getLogger(RandomZoneManager.class.getName());
    private static final Map<Integer, TheHourHolder> _player = new ConcurrentHashMap<>();
    private static TheHourHolder _topPlayer;
    public int _zoneId;
    private int _timer;

    private static String timeToLeft(int timer) {
        long time = timer;
        return String.format("%d mins, %d sec", Long.valueOf(TimeUnit.SECONDS.toMinutes(time)), Long.valueOf(TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time))));
    }

    public static int getTotalZones() {
        int size = 0;
        for (L2RandomZone i : ZoneManager.getInstance().getAllZones(L2RandomZone.class)) {
            if (i == null)
                continue;
            size++;
        }
        return size;
    }

    public static void giveReward(int obj) {
        Player player = World.getInstance().getPlayer(obj);
        if (player != null && player.isOnline()) {
            for (Integer id : L2RandomZone._rewards.keySet())
                player.addItem("PlayerOfTheHour", id, L2RandomZone._rewards.get(id), player, true);
        } else {
            for (Integer id : L2RandomZone._rewards.keySet()) {
                ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), id);
                try {
                    Connection con = ConnectionPool.getConnection();
                    try {
                        PreparedStatement stm_items = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
                        try {
                            stm_items.setInt(1, obj);
                            stm_items.setInt(2, item.getItemId());
                            stm_items.setInt(3, (Integer) L2RandomZone._rewards.get(id));
                            stm_items.setString(4, "INVENTORY");
                            stm_items.setInt(5, 0);
                            stm_items.setInt(6, item.getEnchantLevel());
                            stm_items.setInt(7, item.getObjectId());
                            stm_items.setInt(8, 0);
                            stm_items.setInt(9, 0);
                            stm_items.setInt(10, -60);
                            stm_items.setLong(11, System.currentTimeMillis());
                            stm_items.executeUpdate();
                            stm_items.close();
                            if (stm_items != null)
                                stm_items.close();
                        } catch (Throwable throwable) {
                            if (stm_items != null)
                                try {
                                    stm_items.close();
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addKillsInZone(Player activeChar) {
        if (!_player.containsKey(Integer.valueOf(activeChar.getObjectId()))) {
            _player.put(Integer.valueOf(activeChar.getObjectId()), new TheHourHolder(activeChar.getName(), 1, activeChar.getObjectId()));
        } else {
            _player.get(Integer.valueOf(activeChar.getObjectId())).setPvpKills();
        }
        if (_topPlayer == null) {
            _topPlayer = new TheHourHolder(activeChar.getName(), 1, activeChar.getObjectId());
        } else if (_player.get(Integer.valueOf(activeChar.getObjectId())).getKills() > _topPlayer.getKills()) {
            _topPlayer = _player.get(Integer.valueOf(activeChar.getObjectId()));
        }
    }

    public static void announce(String msg) {
        CreatureSay cs = new CreatureSay(0, 18, "", msg);
        World.toAllOnlinePlayers(cs);
    }

    public static RandomZoneManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        if (getTotalZones() > 1)
            ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
        LOG.info("Loaded Random Zone locations: " + getTotalZones());
    }

    public void run() {
        if (this._timer > 0)
            this._timer--;
        switch (this._timer) {
            case 0:
                selectNextZone();
                announce("PvP zone has been changed to: " + getCurrentZone().getName());
                for (Player player : World.getInstance().getPlayers()) {
                    if (player != null && player.isOnline() && player.isInsideZone(ZoneId.RANDOMZONE))
                        player.teleportTo(getCurrentZone().getLoc(), 20);
                }
                break;
            case 60, 300, 600, 900, 1800:
                announce("PvP Zone: " + this._timer / 60 + " minute(s) remaining until Zone will be changed.");
                break;
            case 3600:
            case 7200:
                announce("PvP Zone: " + this._timer / 60 / 60 + " hour(s) remaining until Zone will be changed.");
                break;
        }
    }

    public int getZoneId() {
        return this._zoneId;
    }

    public void selectNextZone() {
        if (_topPlayer != null &&
                !_topPlayer.getName().equals("")) {
            announce("PvP Zone Most PvP Player was: " + _topPlayer.getName() + " With " + _topPlayer.getKills() + " PvPs");
            giveReward(_topPlayer.getObj());
        }
        int nextZoneId = Rnd.get(1, getTotalZones());
        while (getZoneId() == nextZoneId)
            nextZoneId = Rnd.get(1, getTotalZones());
        this._zoneId = nextZoneId;
        this._timer = getCurrentZone().getTime() + 10;
        _player.clear();
        _topPlayer = null;
    }

    public L2RandomZone getCurrentZone() {
        return ZoneManager.getInstance().getAllZones(L2RandomZone.class).stream().filter(t -> (t.getId() == getZoneId())).findFirst().orElse(null);
    }

    public String getLeftTime() {
        return timeToLeft(this._timer);
    }

    private static class SingletonHolder {
        protected static final RandomZoneManager INSTANCE = new RandomZoneManager();
    }
}
