package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class L2SkillDefault extends L2Skill {
    public L2SkillDefault(StatSet set) {
        super(set);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        caster.sendPacket(ActionFailed.STATIC_PACKET);
        int var10001 = this.getId();
        caster.sendMessage("Skill " + var10001 + " [" + this.getSkillType() + "] isn't implemented.");
    }
}
