/**/
package net.sf.l2j.gameserver.scripting;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.concurrent.ScheduledFuture;

public class QuestTimer {
    protected final Quest _quest;
    protected final String _name;
    protected final Npc _npc;
    protected final Player _player;
    protected final boolean _isRepeating;
    protected ScheduledFuture<?> _schedular;

    public QuestTimer(Quest quest, String name, Npc npc, Player player, long time, boolean repeating) {
        this._quest = quest;
        this._name = name;
        this._npc = npc;
        this._player = player;
        this._isRepeating = repeating;
        if (repeating) {
            this._schedular = ThreadPool.scheduleAtFixedRate(new QuestTimer.ScheduleTimerTask(), time, time);
        } else {
            this._schedular = ThreadPool.schedule(new QuestTimer.ScheduleTimerTask(), time);
        }

    }

    public final String toString() {
        return this._name;
    }

    public final void cancel() {
        if (this._schedular != null) {
            this._schedular.cancel(false);
            this._schedular = null;
        }

        this._quest.removeQuestTimer(this);
    }

    public final boolean equals(Quest quest, String name, Npc npc, Player player) {
        if (quest != null && quest == this._quest) {
            if (name != null && name.equals(this._name)) {
                return npc == this._npc && player == this._player;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected final class ScheduleTimerTask implements Runnable {
        public void run() {
            if (QuestTimer.this._schedular != null) {
                if (!QuestTimer.this._isRepeating) {
                    QuestTimer.this.cancel();
                }

                QuestTimer.this._quest.notifyEvent(QuestTimer.this._name, QuestTimer.this._npc, QuestTimer.this._player);
            }
        }
    }
}