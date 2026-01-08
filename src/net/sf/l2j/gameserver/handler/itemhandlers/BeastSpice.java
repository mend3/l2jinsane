package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class BeastSpice implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        if (!(activeChar.getTarget() instanceof net.sf.l2j.gameserver.model.actor.instance.FeedableBeast)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        int skillId = switch (item.getItemId()) {
            case 6643 -> 2188;
            case 6644 -> 2189;
            default -> 0;
        };
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
        if (skill != null)
            activeChar.useMagic(skill, false, false);
    }
}
