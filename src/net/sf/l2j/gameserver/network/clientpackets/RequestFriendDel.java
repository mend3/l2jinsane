package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class RequestFriendDel extends L2GameClientPacket {
    private static final String DELETE_FRIEND = "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)";

    private String _name;

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        int friendId = PlayerInfoTable.getInstance().getPlayerObjectId(this._name);
        if (friendId == -1 || !player.getFriendList().contains(Integer.valueOf(friendId))) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(this._name));
            return;
        }
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(this._name));
        player.getFriendList().remove(Integer.valueOf(friendId));
        player.sendPacket(new FriendList(player));
        Player friend = World.getInstance().getPlayer(this._name);
        if (friend != null) {
            friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
            friend.sendPacket(new FriendList(friend));
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
                try {
                    ps.setInt(1, player.getObjectId());
                    ps.setInt(2, friendId);
                    ps.setInt(3, friendId);
                    ps.setInt(4, player.getObjectId());
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
            LOGGER.error("Couldn't delete friendId {} for {}.", e, Integer.valueOf(friendId), player.toString());
        }
    }
}
