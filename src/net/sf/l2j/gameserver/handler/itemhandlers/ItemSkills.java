package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ItemSkills implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (playable instanceof net.sf.l2j.gameserver.model.actor.instance.Servitor)
            return;
        boolean isPet = playable instanceof net.sf.l2j.gameserver.model.actor.instance.Pet;
        Player activeChar = playable.getActingPlayer();
        if (isPet && !item.isTradable()) {
            activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
            return;
        }
        IntIntHolder[] skills = item.getEtcItem().getSkills();
        if (skills == null) {
            LOGGER.warn("{} doesn't have any registered skill for handler.", item.getName());
            return;
        }
        for (IntIntHolder skillInfo : skills) {
            if (skillInfo != null) {
                L2Skill itemSkill = skillInfo.getSkill();
                if (itemSkill != null) {
                    if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
                        return;
                    if (playable.isSkillDisabled(itemSkill))
                        return;
                    if (!itemSkill.isPotion() && playable.isCastingNow())
                        return;
                    int count = (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1;
                    if (itemSkill.isPotion() || itemSkill.isSimultaneousCast()) {
                        if (!item.isHerb())
                            if (!playable.destroyItem("Consume", item.getObjectId(), count, null, false)) {
                                activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                                return;
                            }
                        playable.doSimultaneousCast(itemSkill);
                        if (!isPet && item.getItemType() == EtcItemType.HERB && activeChar.hasServitor())
                            activeChar.getSummon().doSimultaneousCast(itemSkill);
                    } else {
                        if (!playable.destroyItem("Consume", item.getObjectId(), count, null, false)) {
                            activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                            return;
                        }
                        playable.getAI().setIntention(IntentionType.IDLE);
                        if (!playable.useMagic(itemSkill, forceUse, false))
                            return;
                    }
                    if (isPet) {
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(itemSkill));
                    } else {
                        int buffId, skillId = skillInfo.getId();
                        switch (skillId) {
                            case 2031:
                            case 2032:
                            case 2037:
                                buffId = activeChar.getShortBuffTaskSkillId();
                                if (skillId == 2037) {
                                    activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
                                    break;
                                }
                                if (skillId == 2032 && buffId != 2037) {
                                    activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
                                    break;
                                }
                                if (buffId != 2037 && buffId != 2032)
                                    activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
                                break;
                        }
                    }
                    int reuseDelay = itemSkill.getReuseDelay();
                    if (item.isEtcItem()) {
                        if (item.getEtcItem().getReuseDelay() > reuseDelay)
                            reuseDelay = item.getEtcItem().getReuseDelay();
                        playable.addTimeStamp(itemSkill, reuseDelay);
                        if (reuseDelay != 0)
                            playable.disableSkill(itemSkill, reuseDelay);
                        if (!isPet) {
                            int group = item.getEtcItem().getSharedReuseGroup();
                            if (group >= 0)
                                activeChar.sendPacket(new ExUseSharedGroupItem(item.getItemId(), group, reuseDelay, reuseDelay));
                        }
                    } else if (reuseDelay > 0) {
                        playable.addTimeStamp(itemSkill, reuseDelay);
                        playable.disableSkill(itemSkill, reuseDelay);
                    }
                }
            }
        }
    }
}
