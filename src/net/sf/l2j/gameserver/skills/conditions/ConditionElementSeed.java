package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectSeed;

public class ConditionElementSeed extends Condition {
    private static final int[] SEED_SKILLS = new int[]{1285, 1286, 1287};

    private final int[] _requiredSeeds;

    public ConditionElementSeed(int[] seeds) {
        this._requiredSeeds = seeds;
    }

    public boolean testImpl(Env env) {
        int[] Seeds = new int[3];
        for (int i = 0; i < Seeds.length; i++) {
            Seeds[i] = (env.getCharacter().getFirstEffect(SEED_SKILLS[i]) instanceof EffectSeed) ? ((EffectSeed) env.getCharacter().getFirstEffect(SEED_SKILLS[i])).getPower() : 0;
            if (Seeds[i] >= this._requiredSeeds[i]) {
                Seeds[i] = Seeds[i] - this._requiredSeeds[i];
            } else {
                return false;
            }
        }
        if (this._requiredSeeds[3] > 0) {
            int count = 0;
            for (int j = 0; j < Seeds.length && count < this._requiredSeeds[3]; j++) {
                if (Seeds[j] > 0) {
                    Seeds[j] = Seeds[j] - 1;
                    count++;
                }
            }
            if (count < this._requiredSeeds[3])
                return false;
        }
        if (this._requiredSeeds[4] > 0) {
            int count = 0;
            for (int j = 0; j < Seeds.length && count < this._requiredSeeds[4]; j++)
                count += Seeds[j];
            return count >= this._requiredSeeds[4];
        }
        return true;
    }
}
