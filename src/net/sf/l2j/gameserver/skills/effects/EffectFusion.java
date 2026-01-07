package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectFusion extends L2Effect {
    public int _effect;

    public int _maxEffect;

    public EffectFusion(Env env, EffectTemplate template) {
        super(env, template);
        this._effect = getSkill().getLevel();
        this._maxEffect = SkillTable.getInstance().getMaxLevel(getSkill().getId());
    }

    public boolean onActionTime() {
        return true;
    }

    public L2EffectType getEffectType() {
        return L2EffectType.FUSION;
    }

    public void increaseEffect() {
        if (this._effect < this._maxEffect) {
            this._effect++;
            updateBuff();
        }
    }

    public void decreaseForce() {
        this._effect--;
        if (this._effect < 1) {
            exit();
        } else {
            updateBuff();
        }
    }

    private void updateBuff() {
        exit();
        SkillTable.getInstance().getInfo(getSkill().getId(), this._effect).getEffects(getEffector(), getEffected());
    }
}
