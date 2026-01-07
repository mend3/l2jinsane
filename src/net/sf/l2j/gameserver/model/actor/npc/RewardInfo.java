package net.sf.l2j.gameserver.model.actor.npc;

import net.sf.l2j.gameserver.model.actor.Playable;

public final class RewardInfo {
    private final Playable _attacker;

    private int _damage;

    public RewardInfo(Playable attacker) {
        this._attacker = attacker;
    }

    public Playable getAttacker() {
        return this._attacker;
    }

    public void addDamage(int damage) {
        this._damage += damage;
    }

    public int getDamage() {
        return this._damage;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof RewardInfo)
            return (((RewardInfo) obj)._attacker == this._attacker);
        return false;
    }

    public int hashCode() {
        return this._attacker.getObjectId();
    }
}
