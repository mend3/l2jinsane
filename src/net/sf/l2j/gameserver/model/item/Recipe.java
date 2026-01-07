package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;

public class Recipe {
    private final List<IntIntHolder> _materials;

    private final IntIntHolder _product;

    private final int _id;

    private final int _level;

    private final int _recipeId;

    private final String _recipeName;

    private final int _successRate;

    private final int _mpCost;

    private final boolean _isDwarven;

    public Recipe(StatSet set) {
        this._materials = set.getIntIntHolderList("material");
        this._product = set.getIntIntHolder("product");
        this._id = set.getInteger("id");
        this._level = set.getInteger("level");
        this._recipeId = set.getInteger("itemId");
        this._recipeName = set.getString("alias");
        this._successRate = set.getInteger("successRate");
        this._mpCost = set.getInteger("mpConsume");
        this._isDwarven = set.getBool("isDwarven");
    }

    public List<IntIntHolder> getMaterials() {
        return this._materials;
    }

    public IntIntHolder getProduct() {
        return this._product;
    }

    public int getId() {
        return this._id;
    }

    public int getLevel() {
        return this._level;
    }

    public int getRecipeId() {
        return this._recipeId;
    }

    public String getRecipeName() {
        return this._recipeName;
    }

    public int getSuccessRate() {
        return this._successRate;
    }

    public int getMpCost() {
        return this._mpCost;
    }

    public boolean isDwarven() {
        return this._isDwarven;
    }
}
