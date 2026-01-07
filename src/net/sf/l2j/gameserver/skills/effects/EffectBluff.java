package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.network.serverpackets.StartRotation;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
import net.sf.l2j.gameserver.skills.Env;

public class EffectBluff extends L2Effect {
    public EffectBluff(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BLUFF;
    }

    public boolean onStart() {
        if (getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeSummon || getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.Folk || getEffected().isRaidRelated() || (getEffected() instanceof Npc && ((Npc) getEffected()).getNpcId() == 35062))
            return false;
        getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
        getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
        getEffected().getPosition().setHeading(getEffector().getHeading());
        return true;
    }

    public boolean onActionTime() {
        return false;
    }
}
