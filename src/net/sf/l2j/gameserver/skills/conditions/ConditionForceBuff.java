package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectFusion;

public class ConditionForceBuff extends Condition {
    private static final short BATTLE_FORCE = 5104;

    private static final short SPELL_FORCE = 5105;

    private final byte[] _forces;

    public ConditionForceBuff(byte[] forces) {
        this._forces = forces;
    }

    public boolean testImpl(Env env) {
        if (this._forces[0] > 0) {
            L2Effect force = env.getCharacter().getFirstEffect(5104);
            if (force == null || ((EffectFusion) force)._effect < this._forces[0])
                return false;
        }
        if (this._forces[1] > 0) {
            L2Effect force = env.getCharacter().getFirstEffect(5105);
            return force != null && ((EffectFusion) force)._effect >= this._forces[1];
        }
        return true;
    }
}
