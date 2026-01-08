/**/
package net.sf.l2j.gameserver.enums.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect {
    NULL("null", 0),
    BLEEDING("bleeding", 1),
    POISON("poison", 2),
    REDCIRCLE("redcircle", 4),
    ICE("ice", 8),
    WIND("wind", 16),
    FEAR("fear", 32),
    STUN("stun", 64),
    SLEEP("sleep", 128),
    MUTED("mute", 256),
    ROOT("root", 512),
    HOLD_1("hold1", 1024),
    HOLD_2("hold2", 2048),
    UNKNOWN_13("unknown13", 4096),
    BIG_HEAD("bighead", 8192),
    FLAME("flame", 16384),
    CHANGE_TEXTURE("changetexture", 32768),
    GROW("grow", 65536),
    FLOATING_ROOT("floatroot", 131072),
    DANCE_STUNNED("dancestun", 262144),
    FIREROOT_STUN("firerootstun", 524288),
    STEALTH("stealth", 1048576),
    IMPRISIONING_1("imprison1", 2097152),
    IMPRISIONING_2("imprison2", 4194304),
    MAGIC_CIRCLE("magiccircle", 8388608);

    private final int _mask;
    private final String _name;

    AbnormalEffect(String name, int mask) {
        this._name = name;
        this._mask = mask;
    }

    public static AbnormalEffect getByName(String name) {
        AbnormalEffect[] var1 = values();
        int var2 = var1.length;

        for (AbnormalEffect eff : var1) {
            if (eff.getName().equals(name)) {
                return eff;
            }
        }

        throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
    }

    public final int getMask() {
        return this._mask;
    }

    public final String getName() {
        return this._name;
    }
}
