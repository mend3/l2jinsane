package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2FriendSay;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class RequestSendFriendMsg extends L2GameClientPacket {
    private static final Logger CHAT_LOG = Logger.getLogger("chat");

    private String _message;

    private String _reciever;

    protected void readImpl() {
        this._message = readS();
        this._reciever = readS();
    }

    protected void runImpl() {
        if (this._message == null || this._message.isEmpty() || this._message.length() > 300)
            return;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player targetPlayer = World.getInstance().getPlayer(this._reciever);
        if (targetPlayer == null || !targetPlayer.getFriendList().contains(Integer.valueOf(activeChar.getObjectId()))) {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            return;
        }
        if (Config.LOG_CHAT) {
            LogRecord record = new LogRecord(Level.INFO, this._message);
            record.setLoggerName("chat");
            record.setParameters(new Object[]{"PRIV_MSG", "[" + activeChar

                    .getName() + " to " + this._reciever + "]"});
            CHAT_LOG.log(record);
        }
        targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), this._reciever, this._message));
    }
}
