package net.sf.l2j.gameserver.model.multisell;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class Ingredient {
    private int _itemId;

    private int _itemCount;

    private int _enchantmentLevel;

    private boolean _isTaxIngredient;

    private boolean _maintainIngredient;

    private Item _template = null;

    public Ingredient(StatSet set) {
        this(set.getInteger("id"), set.getInteger("count"), set.getInteger("enchantmentLevel", 0), set.getBool("isTaxIngredient", false), set.getBool("maintainIngredient", false));
    }

    public Ingredient(int itemId, int itemCount, int enchantmentLevel, boolean isTaxIngredient, boolean maintainIngredient) {
        this._itemId = itemId;
        this._itemCount = itemCount;
        this._enchantmentLevel = enchantmentLevel;
        this._isTaxIngredient = isTaxIngredient;
        this._maintainIngredient = maintainIngredient;
        if (this._itemId > 0)
            this._template = ItemTable.getInstance().getTemplate(this._itemId);
    }

    public Ingredient getCopy() {
        return new Ingredient(this._itemId, this._itemCount, this._enchantmentLevel, this._isTaxIngredient, this._maintainIngredient);
    }

    public final int getItemId() {
        return this._itemId;
    }

    public final void setItemId(int itemId) {
        this._itemId = itemId;
    }

    public final int getItemCount() {
        return this._itemCount;
    }

    public final void setItemCount(int itemCount) {
        this._itemCount = itemCount;
    }

    public final int getEnchantLevel() {
        return this._enchantmentLevel;
    }

    public final void setEnchantLevel(int enchantmentLevel) {
        this._enchantmentLevel = enchantmentLevel;
    }

    public final boolean isTaxIngredient() {
        return this._isTaxIngredient;
    }

    public final void setIsTaxIngredient(boolean isTaxIngredient) {
        this._isTaxIngredient = isTaxIngredient;
    }

    public final boolean getMaintainIngredient() {
        return this._maintainIngredient;
    }

    public final void setMaintainIngredient(boolean maintainIngredient) {
        this._maintainIngredient = maintainIngredient;
    }

    public final Item getTemplate() {
        return this._template;
    }

    public final boolean isStackable() {
        return this._template == null || this._template.isStackable();
    }

    public final boolean isArmorOrWeapon() {
        return this._template != null && ((this._template instanceof net.sf.l2j.gameserver.model.item.kind.Armor || this._template instanceof net.sf.l2j.gameserver.model.item.kind.Weapon));
    }

    public final int getWeight() {
        return (this._template == null) ? 0 : this._template.getWeight();
    }
}
