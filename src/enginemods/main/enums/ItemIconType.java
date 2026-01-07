package enginemods.main.enums;

public enum ItemIconType {
    WEAPON("Icon.weapon_"),
    ARMOR("Icon.armor_"),
    ETCITEM("Icon.etc_"),
    SHIELD("Icon.shield_"),
    RECIPE("Icon.etc_recipe"),
    POTION("Icon.etc_potion_");

    final String _searchItem;

    ItemIconType(String searchItem) {
        this._searchItem = searchItem;
    }

    public String getSearchItem() {
        return this._searchItem;
    }
}