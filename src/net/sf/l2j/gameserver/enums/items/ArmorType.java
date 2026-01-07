/**/
package net.sf.l2j.gameserver.enums.items;

public enum ArmorType implements ItemType {
    NONE,
    LIGHT,
    HEAVY,
    MAGIC,
    PET,
    SHIELD;

    final int _mask = 1 << this.ordinal() + WeaponType.values().length;

    // $FF: synthetic method
    private static ArmorType[] $values() {
        return new ArmorType[]{NONE, LIGHT, HEAVY, MAGIC, PET, SHIELD};
    }

    public int mask() {
        return this._mask;
    }
}
