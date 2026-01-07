package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignet;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;

import java.util.ArrayList;
import java.util.List;

public class EffectSignet extends L2Effect {
    private L2Skill _skill;

    private EffectPoint _actor;

    private boolean _srcInArena;

    public EffectSignet(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SIGNET_EFFECT;
    }

    public boolean onStart() {
        if (getSkill() instanceof L2SkillSignet) {
            this._skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill()).effectId, getLevel());
        } else if (getSkill() instanceof L2SkillSignetCasttime) {
            this._skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
        }
        this._actor = (EffectPoint) getEffected();
        this._srcInArena = getEffector().isInArena();
        return true;
    }

    public boolean onActionTime() {
        if (this._skill == null)
            return true;
        int mpConsume = this._skill.getMpConsume();
        if (mpConsume > getEffector().getCurrentMp()) {
            getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
            return false;
        }
        getEffector().reduceCurrentMp(mpConsume);
        List<Creature> targets = new ArrayList<>();
        for (Creature cha : this._actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius())) {
            if (this._skill.isOffensive() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), cha, this._skill, this._srcInArena))
                continue;
            this._actor.broadcastPacket(new MagicSkillUse(this._actor, cha, this._skill.getId(), this._skill.getLevel(), 0, 0));
            targets.add(cha);
        }
        if (!targets.isEmpty())
            getEffector().callSkill(this._skill, (WorldObject[]) targets.toArray((Object[]) new Creature[targets.size()]));
        return true;
    }

    public void onExit() {
        if (this._actor != null)
            this._actor.deleteMe();
    }
}
