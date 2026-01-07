package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition {
    private final ClassRace _race;

    public ConditionPlayerRace(ClassRace race) {
        this._race = race;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        return (env.getPlayer().getRace() == this._race);
    }
}
