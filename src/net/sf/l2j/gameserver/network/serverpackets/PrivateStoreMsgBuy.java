package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PrivateStoreMsgBuy extends L2GameServerPacket {
    private final Player _activeChar;

    private String _storeMsg;

    public PrivateStoreMsgBuy(Player player) {
        this._activeChar = player;
        if (this._activeChar.getBuyList() != null)
            this._storeMsg = this._activeChar.getBuyList().getTitle();
    }

    protected final void writeImpl() {
        writeC(185);
        writeD(this._activeChar.getObjectId());
        writeS(this._storeMsg);
    }
}
