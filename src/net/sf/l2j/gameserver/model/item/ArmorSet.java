package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;

public final class ArmorSet {
    private final String _name;
    private final int[] _set = new int[5];
    private final int _skillId;
    private final int _shield;
    private final int _shieldSkillId;
    private final int _enchant6Skill;

    public ArmorSet(StatSet set) {
        this._name = set.getString("name");
        this._set[0] = set.getInteger("chest");
        this._set[1] = set.getInteger("legs");
        this._set[2] = set.getInteger("head");
        this._set[3] = set.getInteger("gloves");
        this._set[4] = set.getInteger("feet");
        this._skillId = set.getInteger("skillId");
        this._shield = set.getInteger("shield");
        this._shieldSkillId = set.getInteger("shieldSkillId");
        this._enchant6Skill = set.getInteger("enchant6Skill");
    }

    public String toString() {
        return this._name;
    }

    public int[] getSetItemsId() {
        return this._set;
    }

    public int getShield() {
        return this._shield;
    }

    public int getSkillId() {
        return this._skillId;
    }

    public int getShieldSkillId() {
        return this._shieldSkillId;
    }

    public int getEnchant6skillId() {
        return this._enchant6Skill;
    }

    public boolean containAll(Player player) {
        Inventory inv = player.getInventory();
        int legs = 0;
        int head = 0;
        int gloves = 0;
        int feet = 0;
        ItemInstance legsItem = inv.getPaperdollItem(11);
        if (legsItem != null) {
            legs = legsItem.getItemId();
        }

        if (this._set[1] != 0 && this._set[1] != legs) {
            return false;
        } else {
            ItemInstance headItem = inv.getPaperdollItem(6);
            if (headItem != null) {
                head = headItem.getItemId();
            }

            if (this._set[2] != 0 && this._set[2] != head) {
                return false;
            } else {
                ItemInstance glovesItem = inv.getPaperdollItem(9);
                if (glovesItem != null) {
                    gloves = glovesItem.getItemId();
                }

                if (this._set[3] != 0 && this._set[3] != gloves) {
                    return false;
                } else {
                    ItemInstance feetItem = inv.getPaperdollItem(12);
                    if (feetItem != null) {
                        feet = feetItem.getItemId();
                    }

                    return this._set[4] == 0 || this._set[4] == feet;
                }
            }
        }
    }

    public boolean containItem(int slot, int itemId) {
        switch (slot) {
            case 6:
                return this._set[2] == itemId;
            case 7:
            case 8:
            default:
                return false;
            case 9:
                return this._set[3] == itemId;
            case 10:
                return this._set[0] == itemId;
            case 11:
                return this._set[1] == itemId;
            case 12:
                return this._set[4] == itemId;
        }
    }

    public boolean containShield(Player player) {
        ItemInstance shieldItem = player.getInventory().getPaperdollItem(8);
        return shieldItem != null && shieldItem.getItemId() == this._shield;
    }

    public boolean containShield(int shieldId) {
        if (this._shield == 0) {
            return false;
        } else {
            return this._shield == shieldId;
        }
    }

    public boolean isEnchanted6(Player player) {
        Inventory inv = player.getInventory();
        ItemInstance chestItem = inv.getPaperdollItem(10);
        if (chestItem.getEnchantLevel() < 6) {
            return false;
        } else {
            int legs = 0;
            int head = 0;
            int gloves = 0;
            int feet = 0;
            ItemInstance legsItem = inv.getPaperdollItem(11);
            if (legsItem != null && legsItem.getEnchantLevel() > 5) {
                legs = legsItem.getItemId();
            }

            if (this._set[1] != 0 && this._set[1] != legs) {
                return false;
            } else {
                ItemInstance headItem = inv.getPaperdollItem(6);
                if (headItem != null && headItem.getEnchantLevel() > 5) {
                    head = headItem.getItemId();
                }

                if (this._set[2] != 0 && this._set[2] != head) {
                    return false;
                } else {
                    ItemInstance glovesItem = inv.getPaperdollItem(9);
                    if (glovesItem != null && glovesItem.getEnchantLevel() > 5) {
                        gloves = glovesItem.getItemId();
                    }

                    if (this._set[3] != 0 && this._set[3] != gloves) {
                        return false;
                    } else {
                        ItemInstance feetItem = inv.getPaperdollItem(12);
                        if (feetItem != null && feetItem.getEnchantLevel() > 5) {
                            feet = feetItem.getItemId();
                        }

                        return this._set[4] == 0 || this._set[4] == feet;
                    }
                }
            }
        }
    }
}
