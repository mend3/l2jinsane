/**/
package net.sf.l2j.gameserver.enums.actors;

public enum ClassRace {
    HUMAN(1.0D),
    ELF(1.5D),
    DARK_ELF(1.5D),
    ORC(0.9D),
    DWARF(0.8D);

    private final double _breathMultiplier;

    ClassRace(double breathMultiplier) {
        this._breathMultiplier = breathMultiplier;
    }

    // $FF: synthetic method
    private static ClassRace[] $values() {
        return new ClassRace[]{HUMAN, ELF, DARK_ELF, ORC, DWARF};
    }

    public double getBreathMultiplier() {
        return this._breathMultiplier;
    }
}
