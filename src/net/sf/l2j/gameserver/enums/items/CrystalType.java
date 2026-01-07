/**/
package net.sf.l2j.gameserver.enums.items;

public enum CrystalType {
    NONE(0, 0, 0, 0),
    D(1, 1458, 11, 90),
    C(2, 1459, 6, 45),
    B(3, 1460, 11, 67),
    A(4, 1461, 19, 144),
    S(5, 1462, 25, 250);

    private final int _id;
    private final int _crystalId;
    private final int _crystalEnchantBonusArmor;
    private final int _crystalEnchantBonusWeapon;

    CrystalType(int id, int crystalId, int crystalEnchantBonusArmor, int crystalEnchantBonusWeapon) {
        this._id = id;
        this._crystalId = crystalId;
        this._crystalEnchantBonusArmor = crystalEnchantBonusArmor;
        this._crystalEnchantBonusWeapon = crystalEnchantBonusWeapon;
    }

    // $FF: synthetic method
    private static CrystalType[] $values() {
        return new CrystalType[]{NONE, D, C, B, A, S};
    }

    public int getId() {
        return this._id;
    }

    public int getCrystalId() {
        return this._crystalId;
    }

    public int getCrystalEnchantBonusArmor() {
        return this._crystalEnchantBonusArmor;
    }

    public int getCrystalEnchantBonusWeapon() {
        return this._crystalEnchantBonusWeapon;
    }

    public boolean isGreater(CrystalType crystalType) {
        return this.getId() > crystalType.getId();
    }

    public boolean isLesser(CrystalType crystalType) {
        return this.getId() < crystalType.getId();
    }
}
