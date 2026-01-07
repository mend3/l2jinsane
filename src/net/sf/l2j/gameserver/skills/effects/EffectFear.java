package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectFear extends L2Effect {
    public static final int FEAR_RANGE = 500;

    public EffectFear(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.FEAR;
    }

    public boolean onStart() {
        if (getEffected() instanceof net.sf.l2j.gameserver.model.actor.Player && getEffector() instanceof net.sf.l2j.gameserver.model.actor.Player)
            switch (getSkill().getId()) {
                case 65:
                case 98:
                case 763:
                case 1092:
                case 1169:
                case 1272:
                case 1376:
                case 1381:
                    break;
                default:
                    return false;
            }
        if (getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.Folk || getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeFlag || getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeSummon)
            return false;
        if (getEffected().isAfraid())
            return false;
        getEffected().startFear();
        onActionTime();
        return true;
    }

    public void onExit() {
        getEffected().stopFear(true);
    }

    public boolean onActionTime() {
        if (!(getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.Pet))
            getEffected().setRunning();
        int victimX = getEffected().getX();
        int victimY = getEffected().getY();
        int victimZ = getEffected().getZ();
        int posX = victimX + ((victimX > getEffector().getX()) ? 1 : -1) * 500;
        int posY = victimY + ((victimY > getEffector().getY()) ? 1 : -1) * 500;
        getEffected().getAI().setIntention(IntentionType.MOVE_TO, GeoEngine.getInstance().canMoveToTargetLoc(victimX, victimY, victimZ, posX, posY, victimZ));
        return true;
    }

    public boolean onSameEffect(L2Effect effect) {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.FEAR.getMask();
    }
}
