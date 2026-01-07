package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class Fish {
    private final int _id;

    private final int _level;

    private final int _hp;

    private final int _hpRegen;

    private final int _type;

    private final int _group;

    private final int _guts;

    private final int _gutsCheckTime;

    private final int _waitTime;

    private final int _combatTime;

    public Fish(StatSet set) {
        this._id = set.getInteger("id");
        this._level = set.getInteger("level");
        this._hp = set.getInteger("hp");
        this._hpRegen = set.getInteger("hpRegen");
        this._type = set.getInteger("type");
        this._group = set.getInteger("group");
        this._guts = set.getInteger("guts");
        this._gutsCheckTime = set.getInteger("gutsCheckTime");
        this._waitTime = set.getInteger("waitTime");
        this._combatTime = set.getInteger("combatTime");
    }

    public int getId() {
        return this._id;
    }

    public int getLevel() {
        return this._level;
    }

    public int getHp() {
        return this._hp;
    }

    public int getHpRegen() {
        return this._hpRegen;
    }

    public int getType() {
        return this._type;
    }

    public int getType(boolean isLureNight) {
        if (!GameTimeTaskManager.getInstance().isNight() && isLureNight)
            return -1;
        return this._type;
    }

    public int getGroup() {
        return this._group;
    }

    public int getGuts() {
        return this._guts;
    }

    public int getGutsCheckTime() {
        return this._gutsCheckTime;
    }

    public int getWaitTime() {
        return this._waitTime;
    }

    public int getCombatTime() {
        return this._combatTime;
    }
}
