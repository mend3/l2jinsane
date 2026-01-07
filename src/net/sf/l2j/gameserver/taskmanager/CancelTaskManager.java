package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Vector;

public class CancelTaskManager implements Runnable {
    private final Player _player;

    private final Vector<L2Skill> _buffsCanceled;

    public CancelTaskManager(Player player, Vector<L2Skill> skill) {
        this._player = player;
        this._buffsCanceled = skill;
    }

    public void run() {
        if (this._player == null)
            return;
        for (L2Skill skill : this._buffsCanceled) {
            if (skill == null)
                continue;
            skill.getEffects(this._player, this._player);
        }
    }
}
