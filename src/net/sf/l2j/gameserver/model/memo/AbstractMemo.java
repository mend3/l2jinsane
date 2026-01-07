package net.sf.l2j.gameserver.model.memo;

import net.sf.l2j.commons.util.StatSet;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractMemo extends StatSet {
    private final AtomicBoolean _hasChanges = new AtomicBoolean(false);

    public final void set(String name, boolean value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final void set(String name, double value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final void set(String name, Enum<?> value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final void set(String name, int value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final void set(String name, long value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final void set(String name, String value) {
        this._hasChanges.compareAndSet(false, true);
        super.set(name, value);
    }

    public final boolean hasChanges() {
        return this._hasChanges.get();
    }

    public final boolean compareAndSetChanges(boolean expect, boolean update) {
        return this._hasChanges.compareAndSet(expect, update);
    }

    public final void remove(String name) {
        this._hasChanges.compareAndSet(false, true);
        getSet().remove(name);
    }

    protected abstract boolean load();

    protected abstract boolean storeMe();
}
