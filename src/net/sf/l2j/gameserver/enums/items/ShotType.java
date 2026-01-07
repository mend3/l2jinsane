/**/
package net.sf.l2j.gameserver.enums.items;

public enum ShotType {
    SOULSHOT,
    SPIRITSHOT,
    BLESSED_SPIRITSHOT,
    FISH_SOULSHOT;

    private final int _mask = 1 << this.ordinal();


    public int getMask() {
        return this._mask;
    }
}
