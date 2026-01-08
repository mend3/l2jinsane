/**/
package net.sf.l2j.gameserver.enums;

public enum PunishmentType {
    NONE(""),
    CHAT("chat banned"),
    JAIL("jailed"),
    CHAR("banned"),
    ACC("banned");

    public static final PunishmentType[] VALUES = values();
    private final String _name;

    PunishmentType(String name) {
        this._name = name;
    }

    public String getName() {
        return this._name;
    }
}
