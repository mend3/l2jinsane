/**/
package net.sf.l2j.gameserver.enums;

public enum AttackType {
    Normal(0),
    Magic(1),
    Crit(2),
    MCrit(3),
    Blow(4),
    PhysicalSkillDamage(5),
    PhysicalSkillCritical(6);

    public static final AttackType[] VALUES = values();
    private final int _attackId;

    AttackType(int attackId) {
        this._attackId = attackId;
    }

    public int getId() {
        return this._attackId;
    }
}
