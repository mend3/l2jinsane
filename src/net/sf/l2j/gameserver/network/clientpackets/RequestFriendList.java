package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);
        for (int id : activeChar.getFriendList()) {
            String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
            if (friendName == null)
                continue;
            Player friend = World.getInstance().getPlayer(id);
            activeChar.sendPacket(SystemMessage.getSystemMessage((friend == null || !friend.isOnline()) ? SystemMessageId.S1_OFFLINE : SystemMessageId.S1_ONLINE).addString(friendName));
        }
        activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
    }
}
