package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.pool.ThreadPool;

import java.util.concurrent.ScheduledFuture;

public class Announcement implements Runnable {
    protected final String _message;

    protected boolean _critical;

    protected boolean _auto;

    protected boolean _unlimited;

    protected int _initialDelay;

    protected int _delay;

    protected int _limit;

    protected int _tempLimit;

    protected ScheduledFuture<?> _task;

    public Announcement(String message, boolean critical) {
        this._message = message;
        this._critical = critical;
    }

    public Announcement(String message, boolean critical, boolean auto, int initialDelay, int delay, int limit) {
        this._message = message;
        this._critical = critical;
        this._auto = auto;
        this._initialDelay = initialDelay;
        this._delay = delay;
        this._limit = limit;
        if (this._auto) {
            switch (this._limit) {
                case 0:
                    this._task = ThreadPool.scheduleAtFixedRate(this, (this._initialDelay * 1000L), (this._delay * 1000L));
                    this._unlimited = true;
                    return;
            }
            this._task = ThreadPool.schedule(this, (this._initialDelay * 1000L));
            this._tempLimit = this._limit;
        }
    }

    public void run() {
        if (!this._unlimited) {
            if (this._tempLimit == 0)
                return;
            this._task = ThreadPool.schedule(this, (this._delay * 1000L));
            this._tempLimit--;
        }
        World.announceToOnlinePlayers(this._message, this._critical);
    }

    public String getMessage() {
        return this._message;
    }

    public boolean isCritical() {
        return this._critical;
    }

    public boolean isAuto() {
        return this._auto;
    }

    public int getInitialDelay() {
        return this._initialDelay;
    }

    public int getDelay() {
        return this._delay;
    }

    public int getLimit() {
        return this._limit;
    }

    public void stopTask() {
        if (this._task != null) {
            this._task.cancel(true);
            this._task = null;
        }
    }

    public void reloadTask() {
        stopTask();
        if (this._auto) {
            switch (this._limit) {
                case 0:
                    this._task = ThreadPool.scheduleAtFixedRate(this, (this._initialDelay * 1000L), (this._delay * 1000L));
                    this._unlimited = true;
                    return;
            }
            this._task = ThreadPool.schedule(this, (this._initialDelay * 1000L));
            this._tempLimit = this._limit;
        }
    }
}
