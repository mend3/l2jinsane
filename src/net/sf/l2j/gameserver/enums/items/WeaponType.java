/**/
package net.sf.l2j.gameserver.enums.items;

public enum WeaponType implements ItemType {
    NONE(40),
    SWORD(40),
    BLUNT(40),
    DAGGER(40),
    BOW(500),
    POLE(66),
    ETC(40),
    FIST(40),
    DUAL(40),
    DUALFIST(40),
    BIGSWORD(40),
    FISHINGROD(40),
    BIGBLUNT(40),
    PET(40);

    private final int _mask = 1 << this.ordinal();
    private final int _range;

    WeaponType(int range) {
        this._range = range;
    }

    // $FF: synthetic method
    private static WeaponType[] $values() {
        return new WeaponType[]{NONE, SWORD, BLUNT, DAGGER, BOW, POLE, ETC, FIST, DUAL, DUALFIST, BIGSWORD, FISHINGROD, BIGBLUNT, PET};
    }

    public int mask() {
        return this._mask;
    }

    public int getRange() {
        return this._range;
    }
}
