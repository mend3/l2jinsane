package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class SpiritShot implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        ItemInstance weaponInst = player.getActiveWeaponInstance();
        Weapon weaponItem = player.getActiveWeaponItem();
        if (weaponInst == null || weaponItem.getSpiritShotCount() == 0) {
            if (!player.getAutoSoulShot().contains(Integer.valueOf(item.getItemId())))
                player.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
            return;
        }
        if (player.isChargedShot(ShotType.SPIRITSHOT))
            return;
        if (weaponItem.getCrystalType() != item.getItem().getCrystalType()) {
            if (!player.getAutoSoulShot().contains(Integer.valueOf(item.getItemId())))
                player.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
            return;
        }
        if (!Config.INFINITY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false)) {
            if (!player.disableAutoShot(item.getItemId()))
                player.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
            return;
        }
        IntIntHolder[] skills = item.getItem().getSkills();
        player.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
        player.setChargedShot(ShotType.SPIRITSHOT, true);
        player.broadcastPacketInRadius(new MagicSkillUse(player, player, skills[0].getId(), 1, 0, 0), 600);
    }
}
