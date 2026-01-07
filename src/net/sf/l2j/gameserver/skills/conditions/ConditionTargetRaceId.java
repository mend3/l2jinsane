package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.skills.Env;

import java.util.List;

public class ConditionTargetRaceId extends Condition {
    private final List<Integer> _raceIds;

    public ConditionTargetRaceId(List<Integer> raceId) {
        this._raceIds = raceId;
    }

    public boolean testImpl(Env env) {
        if (!(env.getTarget() instanceof Npc))
            return false;
        return this._raceIds.contains(((Npc) env.getTarget()).getTemplate().getRace().ordinal());
    }
}
