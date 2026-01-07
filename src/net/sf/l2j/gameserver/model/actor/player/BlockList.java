package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class BlockList {
    private static final CLogger LOGGER = new CLogger(BlockList.class.getName());

    private static final Map<Integer, List<Integer>> OFFLINE_LIST = new HashMap<>();

    private static final String LOAD_BLOCKLIST = "SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 1";

    private static final String INSERT_BLOCKED_USER = "INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)";

    private static final String DELETE_BLOCKED_USER = "DELETE FROM character_friends WHERE char_id = ? AND friend_id = ? AND relation = 1";

    private final Player _owner;

    private List<Integer> _blockList;

    public BlockList(Player owner) {
        this._owner = owner;
        this._blockList = OFFLINE_LIST.get(owner.getObjectId());
        if (this._blockList == null)
            this._blockList = loadList(this._owner.getObjectId());
    }

    private static List<Integer> loadList(int objectId) {
        List<Integer> list = new ArrayList<>();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 1");
                try {
                    ps.setInt(1, objectId);
                    ResultSet rset = ps.executeQuery();
                    try {
                        while (rset.next()) {
                            int friendId = rset.getInt("friend_id");
                            if (friendId == objectId)
                                continue;
                            list.add(friendId);
                        }
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
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
            LOGGER.error("Couldn't load blocklist for {}.", e, objectId);
        }
        return list;
    }

    public static boolean isBlocked(Player listOwner, Player target) {
        BlockList blockList = listOwner.getBlockList();
        return (blockList.isBlockAll() || blockList.isInBlockList(target));
    }

    public static boolean isBlocked(Player listOwner, int targetId) {
        BlockList blockList = listOwner.getBlockList();
        return (blockList.isBlockAll() || blockList.isInBlockList(targetId));
    }

    public static void addToBlockList(Player listOwner, int targetId) {
        if (listOwner == null)
            return;
        String targetName = PlayerInfoTable.getInstance().getPlayerName(targetId);
        if (listOwner.getFriendList().contains(targetId)) {
            listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(targetName));
            return;
        }
        if (listOwner.getBlockList().getBlockList().contains(targetId)) {
            listOwner.sendMessage(targetName + " is already registered in your ignore list.");
            return;
        }
        listOwner.getBlockList().addToBlockList(targetId);
        listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addString(targetName));
        Player targetPlayer = World.getInstance().getPlayer(targetId);
        if (targetPlayer != null)
            targetPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addString(listOwner.getName()));
    }

    public static void removeFromBlockList(Player listOwner, int targetId) {
        if (listOwner == null)
            return;
        if (!listOwner.getBlockList().getBlockList().contains(targetId)) {
            listOwner.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return;
        }
        listOwner.getBlockList().removeFromBlockList(targetId);
        listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(PlayerInfoTable.getInstance().getPlayerName(targetId)));
    }

    public static boolean isInBlockList(Player listOwner, Player target) {
        return listOwner.getBlockList().isInBlockList(target);
    }

    public static void setBlockAll(Player listOwner, boolean newValue) {
        listOwner.getBlockList().setBlockAll(newValue);
    }

    public static void sendListToOwner(Player listOwner) {
        int i = 1;
        listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
        for (Iterator<Integer> iterator = listOwner.getBlockList().getBlockList().iterator(); iterator.hasNext(); ) {
            int playerId = iterator.next();
            listOwner.sendMessage(i++ + ". " + i++);
        }
        listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
    }

    public static boolean isInBlockList(int ownerId, int targetId) {
        Player player = World.getInstance().getPlayer(ownerId);
        if (player != null)
            return isBlocked(player, targetId);
        if (!OFFLINE_LIST.containsKey(ownerId))
            OFFLINE_LIST.put(ownerId, loadList(ownerId));
        return OFFLINE_LIST.get(ownerId).contains(targetId);
    }

    private synchronized void addToBlockList(int target) {
        this._blockList.add(target);
        updateInDB(target, true);
    }

    private synchronized void removeFromBlockList(int target) {
        this._blockList.remove(Integer.valueOf(target));
        updateInDB(target, false);
    }

    public void playerLogout() {
        OFFLINE_LIST.put(this._owner.getObjectId(), this._blockList);
    }

    private void updateInDB(int targetId, boolean state) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement(state ? "INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)" : "DELETE FROM character_friends WHERE char_id = ? AND friend_id = ? AND relation = 1");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, targetId);
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
            LOGGER.error("Couldn't add/remove block player.", e);
        }
    }

    public boolean isInBlockList(Player target) {
        return this._blockList.contains(target.getObjectId());
    }

    public boolean isInBlockList(int targetId) {
        return this._blockList.contains(targetId);
    }

    private boolean isBlockAll() {
        return this._owner.isInRefusalMode();
    }

    private void setBlockAll(boolean state) {
        this._owner.setInRefusalMode(state);
    }

    public List<Integer> getBlockList() {
        return this._blockList;
    }

    public boolean isBlockAll(Player listOwner) {
        return listOwner.getBlockList().isBlockAll();
    }
}
