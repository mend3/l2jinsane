package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Recipe;

import java.util.Collection;

public class RecipeBookItemList extends L2GameServerPacket {
    private final boolean _isDwarven;
    private final int _maxMp;
    private final Collection<Recipe> _recipes;

    public RecipeBookItemList(Player player, boolean isDwarven) {
        this._recipes = isDwarven ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook();
        this._isDwarven = isDwarven;
        this._maxMp = player.getMaxMp();
    }

    protected final void writeImpl() {
        writeC(214);
        writeD(this._isDwarven ? 0 : 1);
        writeD(this._maxMp);
        if (this._recipes == null) {
            writeD(0);
        } else {
            writeD(this._recipes.size());
            int i = 0;
            for (Recipe recipe : this._recipes) {
                writeD(recipe.getId());
                writeD(++i);
            }
        }
    }
}
