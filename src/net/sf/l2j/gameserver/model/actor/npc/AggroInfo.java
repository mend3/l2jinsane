package net.sf.l2j.gameserver.model.actor.npc;

import net.sf.l2j.gameserver.model.actor.Creature;

public final class AggroInfo {
    private final Creature _attacker;

    private int _hate;

    private int _damage;

    public AggroInfo(Creature attacker) {
        this._attacker = attacker;
    }

    public Creature getAttacker() {
        return this._attacker;
    }

    public int getHate() {
        return this._hate;
    }

    public int checkHate(Creature owner) {
        if (this._attacker.isAlikeDead() || !this._attacker.isVisible() || !owner.getKnownType(Creature.class).contains(this._attacker))
            this._hate = 0;
        return this._hate;
    }

    public void addHate(int value) {
        this._hate = (int) Math.min(this._hate + value, 999999999L);
    }

    public void stopHate() {
        this._hate = 0;
    }

    public int getDamage() {
        return this._damage;
    }

    public void addDamage(int value) {
        this._damage = (int) Math.min(this._damage + value, 999999999L);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof AggroInfo)
            return (((AggroInfo) obj).getAttacker() == this._attacker);
        return false;
    }

    public int hashCode() {
        return this._attacker.getObjectId();
    }
}
