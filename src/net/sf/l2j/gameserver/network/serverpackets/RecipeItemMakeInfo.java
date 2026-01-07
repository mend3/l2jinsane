package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Recipe;

public class RecipeItemMakeInfo extends L2GameServerPacket {
    private final int _id;

    private final Player _activeChar;

    private final int _status;

    public RecipeItemMakeInfo(int id, Player player, int status) {
        this._id = id;
        this._activeChar = player;
        this._status = status;
    }

    public RecipeItemMakeInfo(int id, Player player) {
        this._id = id;
        this._activeChar = player;
        this._status = -1;
    }

    protected final void writeImpl() {
        Recipe recipe = RecipeData.getInstance().getRecipeList(this._id);
        if (recipe != null) {
            writeC(215);
            writeD(this._id);
            writeD(recipe.isDwarven() ? 0 : 1);
            writeD((int) this._activeChar.getCurrentMp());
            writeD(this._activeChar.getMaxMp());
            writeD(this._status);
        }
    }
}
