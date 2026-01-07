package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class RecipeShopMsg extends L2GameServerPacket {
    private final Player _activeChar;

    public RecipeShopMsg(Player player) {
        this._activeChar = player;
    }

    protected final void writeImpl() {
        writeC(219);
        writeD(this._activeChar.getObjectId());
        writeS(this._activeChar.getCreateList().getStoreName());
    }
}
