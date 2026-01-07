package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;

public class SetPrivateStoreMsgSell extends L2GameClientPacket {
    private static final int MAX_MSG_LENGTH = 29;

    private String _storeMsg;

    protected void readImpl() {
        this._storeMsg = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null || player.getSellList() == null)
            return;
        if (this._storeMsg != null && this._storeMsg.length() > 29)
            return;
        player.getSellList().setTitle(this._storeMsg);
        sendPacket(new PrivateStoreMsgSell(player));
    }
}
