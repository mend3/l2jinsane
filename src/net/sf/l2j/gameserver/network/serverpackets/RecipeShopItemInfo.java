package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class RecipeShopItemInfo extends L2GameServerPacket {
    private final Player _player;

    private final int _recipeId;

    public RecipeShopItemInfo(Player player, int recipeId) {
        this._player = player;
        this._recipeId = recipeId;
    }

    protected final void writeImpl() {
        writeC(218);
        writeD(this._player.getObjectId());
        writeD(this._recipeId);
        writeD((int) this._player.getCurrentMp());
        writeD(this._player.getMaxMp());
        writeD(-1);
    }
}
