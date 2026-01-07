package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.skills.Env;

import java.util.List;

public class ConditionTargetNpcId extends Condition {
    private final List<Integer> _npcIds;

    public ConditionTargetNpcId(List<Integer> npcIds) {
        this._npcIds = npcIds;
    }

    public boolean testImpl(Env env) {
        if (env.getTarget() instanceof Npc)
            return this._npcIds.contains(Integer.valueOf(((Npc) env.getTarget()).getNpcId()));
        if (env.getTarget() instanceof Door)
            return this._npcIds.contains(Integer.valueOf(((Door) env.getTarget()).getDoorId()));
        return false;
    }
}
