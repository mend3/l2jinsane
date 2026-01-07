package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.item.Recipe;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RecipeShopManageList extends L2GameServerPacket {
    private final Player _seller;

    private final boolean _isDwarven;

    private final Collection<Recipe> _recipes;

    public RecipeShopManageList(Player seller, boolean isDwarven) {
        this._seller = seller;
        this._isDwarven = isDwarven;
        if (this._isDwarven && seller.hasDwarvenCraft()) {
            this._recipes = seller.getDwarvenRecipeBook();
        } else {
            this._recipes = seller.getCommonRecipeBook();
        }
        if (seller.getCreateList() != null) {
            Iterator<ManufactureItem> it = seller.getCreateList().getList().iterator();
            while (it.hasNext()) {
                ManufactureItem item = it.next();
                if (item.isDwarven() != this._isDwarven || !seller.hasRecipeList(item.getId()))
                    it.remove();
            }
        }
    }

    protected final void writeImpl() {
        writeC(216);
        writeD(this._seller.getObjectId());
        writeD(this._seller.getAdena());
        writeD(this._isDwarven ? 0 : 1);
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
        if (this._seller.getCreateList() == null) {
            writeD(0);
        } else {
            List<ManufactureItem> list = this._seller.getCreateList().getList();
            writeD(list.size());
            for (ManufactureItem item : list) {
                writeD(item.getId());
                writeD(0);
                writeD(item.getValue());
            }
        }
    }
}
