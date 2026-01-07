package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.model.L2Skill;

public final class Timestamp extends IntIntHolder {
    private final long _reuse;

    private final long _stamp;

    public Timestamp(L2Skill skill, long reuse) {
        super(skill.getId(), skill.getLevel());
        this._reuse = reuse;
        this._stamp = System.currentTimeMillis() + reuse;
    }

    public Timestamp(L2Skill skill, long reuse, long systime) {
        super(skill.getId(), skill.getLevel());
        this._reuse = reuse;
        this._stamp = systime;
    }

    public long getStamp() {
        return this._stamp;
    }

    public long getReuse() {
        return this._reuse;
    }

    public long getRemaining() {
        return Math.max(this._stamp - System.currentTimeMillis(), 0L);
    }

    public boolean hasNotPassed() {
        return (System.currentTimeMillis() < this._stamp);
    }
}
