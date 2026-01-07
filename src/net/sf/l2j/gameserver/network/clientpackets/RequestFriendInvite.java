package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendAddRequest;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket {
    private String _name;

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player friend = World.getInstance().getPlayer(this._name);
        if (friend == null || !friend.isOnline() || friend.getAppearance().getInvisible()) {
            activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
            return;
        }
        if (friend == activeChar) {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
            return;
        }
        if (BlockList.isBlocked(activeChar, friend)) {
            activeChar.sendMessage("You have blocked " + this._name + ".");
            return;
        }
        if (BlockList.isBlocked(friend, activeChar)) {
            activeChar.sendMessage("You are in " + this._name + "'s block list.");
            return;
        }
        if (activeChar.getFriendList().contains(Integer.valueOf(friend.getObjectId()))) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(this._name));
            return;
        }
        if (!friend.isProcessingRequest()) {
            activeChar.onTransactionRequest(friend);
            friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addCharName(activeChar));
            friend.sendPacket(new FriendAddRequest(activeChar.getName()));
        } else {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(this._name));
        }
    }
}
