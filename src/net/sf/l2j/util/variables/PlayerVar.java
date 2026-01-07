package net.sf.l2j.util.variables;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.concurrent.ScheduledFuture;

public class PlayerVar {
    private final Player _owner;

    private final String _name;
    private final long _expire_time;
    private String _value;
    private ScheduledFuture _task;

    public PlayerVar(Player owner, String name, String value, long expire_time) {
        this._owner = owner;
        this._name = name;
        this._value = value;
        this._expire_time = expire_time;
        if (expire_time > 0L)
            this._task = ThreadPool.schedule(new PlayerVarExpireTask(this), expire_time - System.currentTimeMillis());
    }

    public String getName() {
        return this._name;
    }

    public Player getOwner() {
        return this._owner;
    }

    public boolean hasExpired() {
        return (this._task == null || this._task.isDone());
    }

    public long getTimeToExpire() {
        return this._expire_time - System.currentTimeMillis();
    }

    public String getValue() {
        return this._value;
    }

    public void setValue(String val) {
        this._value = val;
    }

    public boolean getValueBoolean() {
        if (isNumeric(this._value))
            return (Integer.parseInt(this._value) > 0);
        return this._value.equalsIgnoreCase("true");
    }

    public void stopExpireTask() {
        if (this._task != null && !this._task.isDone())
            this._task.cancel(true);
    }

    public boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private record PlayerVarExpireTask(PlayerVar _pv) implements Runnable {

        public void run() {
            Player pc = this._pv.getOwner();
            if (pc == null)
                return;
            PlayerVariables.unsetVar(pc, this._pv.getName());
        }
    }
}
