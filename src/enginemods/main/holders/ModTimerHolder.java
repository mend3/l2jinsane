package enginemods.main.holders;

import enginemods.main.EngineModsManager;
import enginemods.main.engine.AbstractMods;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class ModTimerHolder {
    protected static final Logger LOG = Logger.getLogger(ModTimerHolder.class.getName());
    protected final Integer _mod;
    protected final String _name;
    protected final Npc _npc;
    protected final Player _player;
    protected final boolean _isRepeating;
    private final ScheduledFuture<?> _schedular;

    public ModTimerHolder(AbstractMods mod, String name, Npc npc, Player player, long time, boolean repeating) {
        this._mod = mod.hashCode();
        this._name = name;
        this._npc = npc;
        this._player = player;
        this._isRepeating = repeating;
        if (repeating) {
            this._schedular = ThreadPool.scheduleAtFixedRate(new ModTimerHolder.ScheduleTimerTask(), time, time);
        } else {
            this._schedular = ThreadPool.schedule(new ModTimerHolder.ScheduleTimerTask(), time);
        }

    }

    public final void cancel() {
        if (this._schedular != null) {
            this._schedular.cancel(false);
        }

        EngineModsManager.getMod(this._mod).removeTimer(this);
    }

    public final boolean equals(AbstractMods mod, String name, Npc npc, Player player) {
        if (mod != null && mod.hashCode() == this._mod) {
            if (name != null && name.equals(this._name)) {
                return npc == this._npc && player == this._player;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getName() {
        return this._name;
    }

    protected final class ScheduleTimerTask implements Runnable {
        public void run() {
            if (!ModTimerHolder.this._isRepeating) {
                ModTimerHolder.this.cancel();
            }

            if (EngineModsManager.getMod(ModTimerHolder.this._mod).isStarting()) {
                EngineModsManager.getMod(ModTimerHolder.this._mod).onTimer(ModTimerHolder.this._name, ModTimerHolder.this._npc, ModTimerHolder.this._player);
            }
        }
    }
}