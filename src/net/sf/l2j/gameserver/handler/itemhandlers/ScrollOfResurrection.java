package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class ScrollOfResurrection implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        if (activeChar.isSitting()) {
            activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
            return;
        }
        if (activeChar.isMovementDisabled())
            return;
        Creature target = (Creature) activeChar.getTarget();
        if ((!(target instanceof net.sf.l2j.gameserver.model.actor.instance.Pet) && !(target instanceof Player)) || !target.isDead()) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        if (item.getItemId() == 6387 && target instanceof Player) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        Player targetPlayer = target.getActingPlayer();
        Siege siege = CastleManager.getInstance().getActiveSiege(targetPlayer);
        if (siege != null) {
            activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
            return;
        }
        if (targetPlayer.isFestivalParticipant())
            return;
        if (targetPlayer.isReviveRequested()) {
            if (targetPlayer.isRevivingPet()) {
                activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
            } else {
                activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
            }
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
                if (itemSkill != null)
                    playable.useMagic(itemSkill, false, false);
            }
        }
    }
}
