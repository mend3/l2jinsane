/**/
package net.sf.l2j.gameserver.network;

import enginemods.main.data.PlayerData;
import mods.instance.InstanceManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.MMOClient;
import net.sf.l2j.commons.mmocore.MMOConnection;
import net.sf.l2j.commons.mmocore.ReceivablePacket;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.model.CharSelectSlot;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

public final class GameClient extends MMOClient<MMOConnection<GameClient>> implements Runnable {
    private static final CLogger LOGGER = new CLogger(GameClient.class.getName());
    private static final String SELECT_CLAN = "SELECT clanId FROM characters WHERE obj_id=?";
    private static final String UPDATE_DELETE_TIME = "UPDATE characters SET deletetime=? WHERE obj_id=?";
    private static final String DELETE_CHAR_FRIENDS = "DELETE FROM character_friends WHERE char_id=? OR friend_id=?";
    private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=?";
    private static final String DELETE_CHAR_MACROS = "DELETE FROM character_macroses WHERE char_obj_id=?";
    private static final String DELETE_CHAR_MEMOS = "DELETE FROM character_memo WHERE charId=?";
    private static final String DELETE_CHAR_QUESTS = "DELETE FROM character_quests WHERE charId=?";
    private static final String DELETE_CHAR_RECIPES = "DELETE FROM character_recipebook WHERE charId=?";
    private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=?";
    private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=?";
    private static final String DELETE_CHAR_SKILLS_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=?";
    private static final String DELETE_CHAR_SUBCLASSES = "DELETE FROM character_subclasses WHERE char_obj_id=?";
    private static final String DELETE_CHAR_HERO = "DELETE FROM heroes WHERE char_id=?";
    private static final String DELETE_CHAR_NOBLE = "DELETE FROM olympiad_nobles WHERE char_id=?";
    private static final String DELETE_CHAR_SEVEN_SIGNS = "DELETE FROM seven_signs WHERE char_obj_id=?";
    private static final String DELETE_CHAR_PETS = "DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
    private static final String DELETE_CHAR_AUGMENTS = "DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
    private static final String DELETE_CHAR_ITEMS = "DELETE FROM items WHERE owner_id=?";
    private static final String DELETE_CHAR_RBP = "DELETE FROM character_raid_points WHERE char_id=?";
    private static final String DELETE_CHAR = "DELETE FROM characters WHERE obj_Id=?";
    protected final ScheduledFuture<?> _autoSaveInDB;
    private final long[] _floodProtectors;
    private final ArrayBlockingQueue<ReceivablePacket<GameClient>> _packetQueue;
    private final ReentrantLock _queueLock;
    private final ReentrantLock _activeCharLock;
    private final GameCrypt _crypt;
    private final ClientStats _stats;
    private final long _connectionStartTime;
    public GameClientState _state;
    protected ScheduledFuture<?> _cleanupTask;
    private String _accountName;
    private SessionKey _sessionId;
    private Player _player;
    private boolean _isDetached;
    private boolean _isAuthedGG;
    private CharSelectSlot[] _slots;
    private String _playerName;
    private String _loginName;
    private int _playerId;
    private String _hwid;
    private int revision;

    public GameClient(MMOConnection<GameClient> con) {
        super(con);
        this._floodProtectors = new long[Action.VALUES_LENGTH];
        this._queueLock = new ReentrantLock();
        this._activeCharLock = new ReentrantLock();
        this._playerName = "";
        this._loginName = "";
        this._playerId = 0;
        this._hwid = "";
        this.revision = 0;
        this._state = GameClient.GameClientState.CONNECTED;
        this._connectionStartTime = System.currentTimeMillis();
        this._crypt = new GameCrypt();
        this._stats = new ClientStats();
        this._packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
        this._autoSaveInDB = ThreadPool.scheduleAtFixedRate(() -> {
            if (this.getPlayer() != null && this.getPlayer().isOnline()) {
                this.getPlayer().store();
                if (this.getPlayer().getSummon() != null) {
                    this.getPlayer().getSummon().store();
                }
            }

        }, 300000L, 900000L);
    }

    public static void deleteCharByObjId(int objectId) {
        if (objectId >= 0) {
            PlayerInfoTable.getInstance().removePlayer(objectId);

            try (Connection con = ConnectionPool.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?")) {
                    ps.setInt(1, objectId);
                    ps.setInt(2, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_memo WHERE charId=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_quests WHERE charId=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM heroes WHERE char_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_raid_points WHERE char_id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?")) {
                    ps.setInt(1, objectId);
                    ps.execute();
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't delete player.", e);
            }

        }
    }

    public void run() {
        if (this._queueLock.tryLock()) {
            try {
                int count = 0;

                do {
                    ReceivablePacket<GameClient> packet = this._packetQueue.poll();
                    if (packet == null) {
                        return;
                    }

                    if (this._isDetached) {
                        this._packetQueue.clear();
                        return;
                    }

                    try {
                        packet.run();
                    } catch (Exception e) {
                        LOGGER.error("Execution failed on {} for {}.", e, packet.getClass().getSimpleName(), this.toString());
                    }

                    ++count;
                } while (!this.getStats().countBurst(count));

            } finally {
                this._queueLock.unlock();
            }
        }
    }

    public String toString() {
        try {
            InetAddress address = this.getConnection().getInetAddress();
            return switch (this.getState().ordinal()) {
                case 0 -> {
                    String var4 = address == null ? "disconnected" : address.getHostAddress();
                    yield "[IP: " + var4 + "]";
                }
                case 1 -> {
                    String var3 = this.getAccountName();
                    yield "[Account: " + var3 + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
                }
                case 2, 3 -> {
                    String var10000 = this.getPlayer() == null ? "disconnected" : this.getPlayer().getName();
                    yield "[Character: " + var10000 + " - Account: " + this.getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
                }
                default -> throw new IllegalStateException("Missing state on switch");
            };
        } catch (NullPointerException var2) {
            return "[Character read failed due to disconnect]";
        }
    }

    public boolean decrypt(ByteBuffer buf, int size) {
        this._crypt.decrypt(buf.array(), buf.position(), size);
        return true;
    }

    public boolean encrypt(ByteBuffer buf, int size) {
        this._crypt.encrypt(buf.array(), buf.position(), size);
        buf.position(buf.position() + size);
        return true;
    }

    protected void onDisconnection() {
        try {
            Player player = this.getActiveChar();
            if (player != null) {
                if (player.isArenaProtection()) {
                    player.setXYZ(ArenaTask.loc1x(), ArenaTask.loc1y(), ArenaTask.loc1z());
                    if (player.isInArenaEvent()) {
                        player.getAppearance().setTitleColor(player._originalTitleColorTournament);
                        player.setTitle(player._originalTitleTournament);
                        player.broadcastUserInfo();
                        player.broadcastTitleInfo();
                    }
                }

                if (player.getInstance() != null && player.getInstance().getId() != 0) {
                    player.setInstance(InstanceManager.getInstance().getInstance(0), true);
                    player.teleportTo(TeleportType.TOWN);
                }
            }

            ThreadPool.execute(() -> {
                boolean fast = true;
                if (this.getPlayer() != null && !this.isDetached()) {
                    this.setDetached(true);
                    if (PlayerData.get(this.getPlayer()).isOffline()) {
                        return;
                    }

                    fast = !this.getPlayer().isInCombat() && !this.getPlayer().isLocked();
                }

                this.cleanMe(fast);
            });
        } catch (RejectedExecutionException ignored) {
        }

    }

    protected void onForcedDisconnection() {
        LOGGER.debug("{} disconnected abnormally.", this.toString());
    }

    public byte[] enableCrypt() {
        byte[] key = BlowFishKeygen.getRandomKey();
        this._crypt.setKey(key);
        if (Hwid.isProtectionOn()) {
            key = Hwid.getKey(key);
        }

        return key;
    }

    public GameClientState getState() {
        return this._state;
    }

    public void setState(GameClientState pState) {
        if (this._state != pState) {
            this._state = pState;
            this._packetQueue.clear();
        }

    }

    public ClientStats getStats() {
        return this._stats;
    }

    public long getConnectionStartTime() {
        return this._connectionStartTime;
    }

    public Player getPlayer() {
        return this._player;
    }

    public void setPlayer(Player player) {
        this._player = player;
    }

    public ReentrantLock getActiveCharLock() {
        return this._activeCharLock;
    }

    public long[] getFloodProtectors() {
        return this._floodProtectors;
    }

    public void setGameGuardOk(boolean val) {
        this.set_isAuthedGG(val);
    }

    public String getAccountName() {
        return this._accountName;
    }

    public void setAccountName(String pAccountName) {
        this._accountName = pAccountName;
    }

    public SessionKey getSessionId() {
        return this._sessionId;
    }

    public void setSessionId(SessionKey sk) {
        this._sessionId = sk;
    }

    public void sendPacket(L2GameServerPacket gsp) {
        if (!this._isDetached) {
            if (this.getConnection() != null) {
                this.getConnection().sendPacket(gsp);
                gsp.runImpl();
            }
        }
    }

    public boolean isDetached() {
        return this._isDetached;
    }

    public void setDetached(boolean b) {
        this._isDetached = b;
    }

    public byte markToDeleteChar(int slot) {
        int objectId = this.getObjectIdForSlot(slot);
        if (objectId < 0) {
            return -1;
        } else {
            byte answer = 0;

            try {
                try (Connection con = ConnectionPool.getConnection()) {
                    try (PreparedStatement ps = con.prepareStatement("SELECT clanId FROM characters WHERE obj_id=?")) {
                        ps.setInt(1, objectId);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            int clanId = rs.getInt(1);
                            if (clanId != 0) {
                                Clan clan = ClanTable.getInstance().getClan(clanId);
                                if (clan == null) {
                                    answer = 0;
                                } else if (clan.getLeaderId() == objectId) {
                                    answer = 2;
                                } else {
                                    answer = 1;
                                }
                            }
                        }
                    }

                    if (answer == 0) {
                        if (Config.DELETE_DAYS == 0) {
                            deleteCharByObjId(objectId);
                        } else {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?")) {
                                ps.setLong(1, System.currentTimeMillis() + (long) Config.DELETE_DAYS * 86400000L);
                                ps.setInt(2, objectId);
                                ps.execute();
                            }
                        }
                    }
                }

                return answer;
            } catch (Exception e) {
                LOGGER.error("Couldn't mark as delete a player.", e);
                return -1;
            }
        }
    }

    public void markRestoredChar(int slot) {
        int objectId = this.getObjectIdForSlot(slot);
        if (objectId >= 0) {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?")
            ) {
                ps.setLong(1, 0L);
                ps.setInt(2, objectId);
                ps.execute();
            } catch (Exception e) {
                LOGGER.error("Couldn't restore player.", e);
            }

        }
    }

    public Player loadCharFromDisk(int slot) {
        int objectId = this.getObjectIdForSlot(slot);
        if (objectId < 0) {
            return null;
        } else {
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null) {
                if (player.getClient() != null) {
                    player.getClient().closeNow();
                } else {
                    player.deleteMe();
                }

                return null;
            } else {
                player = Player.restore(objectId);
                if (player != null) {
                    player.setRunning();
                    player.standUp();
                    player.setOnlineStatus(true, false);
                    World.getInstance().addPlayer(player);
                }

                return player;
            }
        }
    }

    public CharSelectSlot getCharSelectSlot(int id) {
        return this._slots != null && id >= 0 && id < this._slots.length ? this._slots[id] : null;
    }

    public void setCharSelectSlot(CharSelectSlot[] list) {
        this._slots = list;
    }

    public void close(L2GameServerPacket gsp) {
        if (this.getConnection() != null) {
            this.getConnection().close(gsp);
        }
    }

    private int getObjectIdForSlot(int charslot) {
        CharSelectSlot info = this.getCharSelectSlot(charslot);
        return info == null ? -1 : info.getObjectId();
    }

    public synchronized void closeNow() {
        this._isDetached = true;
        this.close(ServerClose.STATIC_PACKET);
        if (this._cleanupTask != null) {
            this._cleanupTask.cancel(true);
            this._cleanupTask = null;
        }

        ThreadPool.schedule(new CleanupTask(), 0L);
    }

    public synchronized void cleanMe(boolean fast) {
        if (this._cleanupTask == null) {
            this._cleanupTask = ThreadPool.schedule(new CleanupTask(), fast ? 100L : 15000L);
        }

    }

    public boolean dropPacket() {
        if (this._isDetached) {
            return true;
        } else if (this.getStats().countPacket(this._packetQueue.size())) {
            this.sendPacket(ActionFailed.STATIC_PACKET);
            return true;
        } else {
            return this.getStats().dropPacket();
        }
    }

    public void onBufferUnderflow() {
        if (this._state == GameClient.GameClientState.CONNECTED) {
            if (Config.PACKET_HANDLER_DEBUG) {
                LOGGER.warn("{} has been disconnected: too many buffer underflows in non-authed state.", this.toString());
            }

            this.closeNow();
        } else if (this.getStats().countUnderflowException()) {
            LOGGER.warn("{} has been disconnected: too many buffer underflows.", this.toString());
            this.closeNow();
        }

    }

    public void onUnknownPacket() {
        if (this._state == GameClient.GameClientState.CONNECTED) {
            if (Config.PACKET_HANDLER_DEBUG) {
                LOGGER.warn("{} has been disconnected: too many unknown packets in non-authed state.", this.toString());
            }

            this.closeNow();
        } else if (this.getStats().countUnknownPacket()) {
            LOGGER.warn("{} has been disconnected: too many unknown packets.", this.toString());
            this.closeNow();
        }

    }

    public void execute(ReceivablePacket<GameClient> packet) {
        if (this.getStats().countFloods()) {
            LOGGER.warn("{} has been disconnected: too many floods ({} long and {} short).", this.toString(), this.getStats().longFloods, this.getStats().shortFloods);
            this.closeNow();
        } else if (!this._packetQueue.offer(packet)) {
            if (this.getStats().countQueueOverflow()) {
                LOGGER.warn("{} has been disconnected: too many queue overflows.", this.toString());
                this.closeNow();
            } else {
                this.sendPacket(ActionFailed.STATIC_PACKET);
            }

        } else if (!this._queueLock.isLocked()) {
            try {
                if (this._state == GameClient.GameClientState.CONNECTED && this.getStats().processedPackets > 3) {
                    if (Config.PACKET_HANDLER_DEBUG) {
                        LOGGER.warn("{} has been disconnected: too many packets in non-authed state.", this.toString());
                    }

                    this.closeNow();
                    return;
                }

                ThreadPool.execute(this);
            } catch (RejectedExecutionException ignored) {
            }

        }
    }

    public Player getActiveChar() {
        return this._player;
    }

    public void setActiveChar(Player pActiveChar) {
        this._player = pActiveChar;
    }

    public String getPlayerName() {
        return this._playerName;
    }

    public void setPlayerName(String name) {
        this._playerName = name;
    }

    public int getPlayerId() {
        return this._playerId;
    }

    public void setPlayerId(int plId) {
        this._playerId = plId;
    }

    public String getHWID() {
        return this._hwid;
    }

    public void setHWID(String hwid) {
        this._hwid = hwid;
    }

    public int getRevision() {
        return this.revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getLoginName() {
        return this._loginName;
    }

    public void setLoginName(String name) {
        this._loginName = name;
    }

    public boolean is_isAuthedGG() {
        return this._isAuthedGG;
    }

    public void set_isAuthedGG(boolean _isAuthedGG) {
        this._isAuthedGG = _isAuthedGG;
    }

    public enum GameClientState {
        CONNECTED,
        AUTHED,
        ENTERING,
        IN_GAME
    }

    protected class CleanupTask implements Runnable {
        public void run() {
            if (GameClient.this._autoSaveInDB != null) {
                GameClient.this._autoSaveInDB.cancel(true);
            }

            Player player = GameClient.this.getActiveChar();
            if (player != null && player.isArenaProtection()) {
                player.setXYZ(ArenaTask.loc1x(), ArenaTask.loc1y(), ArenaTask.loc1z());
                if (player.isInArenaEvent()) {
                    player.getAppearance().setTitleColor(player._originalTitleColorTournament);
                    player.setTitle(player._originalTitleTournament);
                    player.broadcastUserInfo();
                    player.broadcastTitleInfo();
                }
            }

            if (GameClient.this.getPlayer() != null) {
                GameClient.this.getPlayer().setClient(null);
                if (GameClient.this.getPlayer().isOnline()) {
                    GameClient.this.getPlayer().deleteMe();
                }
            }

            GameClient.this.setPlayer(null);
            LoginServerThread.getInstance().sendLogout(GameClient.this.getAccountName());
        }
    }
}
