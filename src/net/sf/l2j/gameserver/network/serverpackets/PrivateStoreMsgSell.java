package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PrivateStoreMsgSell extends L2GameServerPacket {
    private final Player _activeChar;

    private String _storeMsg;

    public PrivateStoreMsgSell(Player player) {
        this._activeChar = player;
        if (this._activeChar.getSellList() != null)
            this._storeMsg = this._activeChar.getSellList().getTitle();
    }

    protected final void writeImpl() {
        writeC(156);
        writeD(this._activeChar.getObjectId());
        writeS(this._storeMsg);
    }
}
