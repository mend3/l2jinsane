package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class RequestAnswerFriendInvite extends L2GameClientPacket {
    private static final String ADD_FRIEND = "INSERT INTO character_friends (char_id, friend_id) VALUES (?,?), (?,?)";

    private int _response;

    protected void readImpl() {
        this._response = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Player requestor = player.getActiveRequester();
        if (requestor == null)
            return;
        if (this._response == 1) {
            requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addCharName(player));
            requestor.getFriendList().add(Integer.valueOf(player.getObjectId()));
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addCharName(requestor));
            player.getFriendList().add(Integer.valueOf(requestor.getObjectId()));
            requestor.sendPacket(new FriendList(requestor));
            player.sendPacket(new FriendList(player));
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id) VALUES (?,?), (?,?)");
                    try {
                        ps.setInt(1, requestor.getObjectId());
                        ps.setInt(2, player.getObjectId());
                        ps.setInt(3, player.getObjectId());
                        ps.setInt(4, requestor.getObjectId());
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
                LOGGER.error("Couldn't add friendId {} for {}.", e, Integer.valueOf(player.getObjectId()), requestor.toString());
            }
        } else {
            requestor.sendPacket(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
        }
        player.setActiveRequester(null);
        requestor.onTransactionResponse();
    }
}
