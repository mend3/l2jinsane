package net.sf.l2j.gameserver.model.craft;

import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

public class ManufactureItem extends IntIntHolder {
    private final boolean _isDwarven;

    public ManufactureItem(int recipeId, int cost) {
        super(recipeId, cost);
        this._isDwarven = RecipeData.getInstance().getRecipeList(recipeId).isDwarven();
    }

    public boolean isDwarven() {
        return this._isDwarven;
    }
}
