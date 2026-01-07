package net.sf.l2j.gameserver.model.multisell;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.ArrayList;

public class PreparedEntry extends Entry {
    private int _taxAmount = 0;

    public PreparedEntry(Entry template, ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate) {
        int adenaAmount = 0;
        this._ingredients = new ArrayList<>(template.getIngredients().size());
        for (Ingredient ing : template.getIngredients()) {
            if (ing.getItemId() == 57) {
                if (ing.isTaxIngredient()) {
                    if (applyTaxes)
                        this._taxAmount = (int) (this._taxAmount + Math.round(ing.getItemCount() * taxRate));
                    continue;
                }
                adenaAmount += ing.getItemCount();
                continue;
            }
            Ingredient newIngredient = ing.getCopy();
            if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
                newIngredient.setEnchantLevel(item.getEnchantLevel());
            this._ingredients.add(newIngredient);
        }
        adenaAmount += this._taxAmount;
        if (adenaAmount > 0)
            this._ingredients.add(new Ingredient(57, adenaAmount, 0, false, false));
        this._products = new ArrayList<>(template.getProducts().size());
        for (Ingredient ing : template.getProducts()) {
            if (!ing.isStackable())
                this._stackable = false;
            Ingredient newProduct = ing.getCopy();
            if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
                newProduct.setEnchantLevel(item.getEnchantLevel());
            this._products.add(newProduct);
        }
    }

    public final int getTaxAmount() {
        return this._taxAmount;
    }
}
