package net.sf.l2j.gameserver.model.multisell;

import java.util.List;

public class Entry {
    protected List<Ingredient> _ingredients;

    protected List<Ingredient> _products;

    protected boolean _stackable = true;

    public Entry(List<Ingredient> ingredients, List<Ingredient> products) {
        this._ingredients = ingredients;
        this._products = products;
        this._stackable = products.stream().allMatch(Ingredient::isStackable);
    }

    protected Entry() {
    }

    public List<Ingredient> getProducts() {
        return this._products;
    }

    public List<Ingredient> getIngredients() {
        return this._ingredients;
    }

    public boolean isStackable() {
        return this._stackable;
    }

    public int getTaxAmount() {
        return 0;
    }
}
