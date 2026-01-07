package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class Harvester implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof net.sf.l2j.gameserver.model.actor.Player))
            return;
        if (!Config.ALLOW_MANOR)
            return;
        if (!(playable.getTarget() instanceof Monster _target)) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        if (_target == null || !_target.isDead()) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        L2Skill skill = SkillTable.getInstance().getInfo(2098, 1);
        if (skill != null)
            playable.useMagic(skill, false, false);
    }
}
