package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;

public class SoulCrystals implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        EtcItem etcItem = item.getEtcItem();
        IntIntHolder[] skills = etcItem.getSkills();
        if (skills == null)
            return;
        L2Skill itemSkill = skills[0].getSkill();
        if (itemSkill == null || itemSkill.getId() != 2096)
            return;
        if (player.isCastingNow())
            return;
        if (!itemSkill.checkCondition(player, player.getTarget(), false))
            return;
        if (player.isSkillDisabled(itemSkill))
            return;
        player.getAI().setIntention(IntentionType.IDLE);
        if (!player.useMagic(itemSkill, forceUse, false))
            return;
        int reuseDelay = itemSkill.getReuseDelay();
        if (etcItem.getReuseDelay() > reuseDelay)
            reuseDelay = etcItem.getReuseDelay();
        player.addTimeStamp(itemSkill, reuseDelay);
        if (reuseDelay != 0)
            player.disableSkill(itemSkill, reuseDelay);
    }
}
