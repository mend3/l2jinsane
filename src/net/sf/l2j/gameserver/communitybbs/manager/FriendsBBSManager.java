package net.sf.l2j.gameserver.communitybbs.manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FriendsBBSManager extends BaseBBSManager {
    private static final String FRIENDLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";

    private static final String BLOCKLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";

    private static final String DELETE_ALL_FRIENDS = "DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?";

    private static final String DELETE_FRIEND = "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)";

    private static void showFriendsList(Player player, boolean delMsg) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/friend/friend-list.htm");
        if (content == null)
            return;
        List<Integer> list = player.getFriendList();
        List<Integer> selectedList = player.getSelectedFriendList();
        StringBuilder sb = new StringBuilder();
        for (Integer id : list) {
            if (selectedList.contains(id))
                continue;
            String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (friendName == null)
                continue;
            Player friend = World.getInstance().getPlayer(id);
            StringUtil.append(sb, "<a action=\"bypass _friend;select;", id, "\">[Select]</a>&nbsp;", friendName, " ", (friend != null && friend.isOnline()) ? "(on)" : "(off)", "<br1>");
        }
        content = content.replaceAll("%friendslist%", sb.toString());
        sb.setLength(0);
        for (Integer id : selectedList) {
            String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (friendName == null)
                continue;
            Player friend = World.getInstance().getPlayer(id);
            StringUtil.append(sb, "<a action=\"bypass _friend;deselect;", id, "\">[Deselect]</a>&nbsp;", friendName, " ", (friend != null && friend.isOnline()) ? "(on)" : "(off)", "<br1>");
        }
        content = content.replaceAll("%selectedFriendsList%", sb.toString());
        content = content.replaceAll("%deleteMSG%", delMsg ? "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>" : "");
        separateAndSend(content, player);
    }

    private static void showBlockList(Player player, boolean delMsg) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/friend/friend-blocklist.htm");
        if (content == null)
            return;
        List<Integer> list = player.getBlockList().getBlockList();
        List<Integer> selectedList = player.getSelectedBlocksList();
        StringBuilder sb = new StringBuilder();
        for (Integer id : list) {
            if (selectedList.contains(id))
                continue;
            String blockName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (blockName == null)
                continue;
            Player block = World.getInstance().getPlayer(id);
            StringUtil.append(sb, "<a action=\"bypass _block;select;", id, "\">[Select]</a>&nbsp;", blockName, " ", (block != null && block.isOnline()) ? "(on)" : "(off)", "<br1>");
        }
        content = content.replaceAll("%blocklist%", sb.toString());
        sb.setLength(0);
        for (Integer id : selectedList) {
            String blockName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (blockName == null)
                continue;
            Player block = World.getInstance().getPlayer(id);
            StringUtil.append(sb, "<a action=\"bypass _block;deselect;", id, "\">[Deselect]</a>&nbsp;", blockName, " ", (block != null && block.isOnline()) ? "(on)" : "(off)", "<br1>");
        }
        content = content.replaceAll("%selectedBlocksList%", sb.toString());
        content = content.replaceAll("%deleteMSG%", delMsg ? "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>" : "");
        separateAndSend(content, player);
    }

    public static void showMailWrite(Player player) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/friend/friend-mail.htm");
        if (content == null)
            return;
        StringBuilder sb = new StringBuilder();
        for (int id : player.getSelectedFriendList()) {
            String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (friendName == null)
                continue;
            if (!sb.isEmpty())
                sb.append(";");
            sb.append(friendName);
        }
        content = content.replaceAll("%list%", sb.toString());
        separateAndSend(content, player);
    }

    public static FriendsBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (command.startsWith("_friendlist")) {
            showFriendsList(player, false);
        } else if (command.startsWith("_blocklist")) {
            showBlockList(player, false);
        } else if (command.startsWith("_friend")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String action = st.nextToken();
            switch (action) {
                case "select" -> {
                    player.selectFriend(st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);
                    showFriendsList(player, false);
                }
                case "deselect" -> {
                    player.deselectFriend(st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);
                    showFriendsList(player, false);
                }
                case "delall" -> {
                    try {
                        Connection con = ConnectionPool.getConnection();
                        try {
                            PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?");
                            try {
                                ps.setInt(1, player.getObjectId());
                                ps.setInt(2, player.getObjectId());
                                ps.execute();
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
                        LOGGER.error("Couldn't delete friends.", e);
                    }
                    for (int friendId : player.getFriendList()) {
                        Player friend = World.getInstance().getPlayer(friendId);
                        if (friend != null) {
                            friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
                            friend.getSelectedFriendList().remove(Integer.valueOf(player.getObjectId()));
                            friend.sendPacket(new FriendList(friend));
                        }
                    }
                    player.getFriendList().clear();
                    player.getSelectedFriendList().clear();
                    showFriendsList(player, false);
                    player.sendMessage("You have cleared your friends list.");
                    player.sendPacket(new FriendList(player));
                }
                case "delconfirm" -> showFriendsList(player, true);
                case "del" -> {
                    try {
                        Connection con = ConnectionPool.getConnection();
                        try {
                            PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
                            try {
                                ps.setInt(1, player.getObjectId());
                                ps.setInt(4, player.getObjectId());
                                for (int friendId : player.getSelectedFriendList()) {
                                    ps.setInt(2, friendId);
                                    ps.setInt(3, friendId);
                                    ps.addBatch();
                                    Player friend = World.getInstance().getPlayer(friendId);
                                    if (friend != null) {
                                        friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
                                        friend.sendPacket(new FriendList(friend));
                                    }
                                    player.getFriendList().remove(Integer.valueOf(friendId));
                                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(PlayerInfoTable.getInstance().getPlayerName(friendId)));
                                }
                                ps.executeBatch();
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
                        LOGGER.error("Couldn't delete friend.", e);
                    }
                    player.getSelectedFriendList().clear();
                    showFriendsList(player, false);
                    player.sendPacket(new FriendList(player));
                }
                case "mail" -> {
                    if (!player.getSelectedFriendList().isEmpty())
                        showMailWrite(player);
                }
            }
        } else if (command.startsWith("_block")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String action = st.nextToken();
            switch (action) {
                case "select" -> {
                    player.selectBlock(st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);
                    showBlockList(player, false);
                }
                case "deselect" -> {
                    player.deselectBlock(st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);
                    showBlockList(player, false);
                }
                case "delall" -> {
                    List<Integer> list = new ArrayList<>(player.getBlockList().getBlockList());
                    for (Integer blockId : list)
                        BlockList.removeFromBlockList(player, blockId);
                    player.getSelectedBlocksList().clear();
                    showBlockList(player, false);
                }
                case "delconfirm" -> showBlockList(player, true);
                case "del" -> {
                    for (Integer blockId : player.getSelectedBlocksList())
                        BlockList.removeFromBlockList(player, blockId);
                    player.getSelectedBlocksList().clear();
                    showBlockList(player, false);
                }
            }
        } else {
            super.parseCmd(command, player);
        }
    }

    public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
        if (ar1.equalsIgnoreCase("mail")) {
            MailBBSManager.getInstance().sendMail(ar2, ar4, ar5, player);
            showFriendsList(player, false);
        } else {
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
        }
    }

    protected String getFolder() {
        return "friend/";
    }

    private static class SingletonHolder {
        protected static final FriendsBBSManager INSTANCE = new FriendsBBSManager();
    }
}
