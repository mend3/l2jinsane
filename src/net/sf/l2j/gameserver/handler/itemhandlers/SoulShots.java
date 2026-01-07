package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class SoulShots implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        ItemInstance weaponInst = player.getActiveWeaponInstance();
        Weapon weaponItem = player.getActiveWeaponItem();
        if (weaponInst == null || weaponItem.getSoulShotCount() == 0) {
            if (!player.getAutoSoulShot().contains(item.getItemId()))
                player.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
            return;
        }
        if (weaponItem.getCrystalType() != item.getItem().getCrystalType()) {
            if (!player.getAutoSoulShot().contains(item.getItemId()))
                player.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
            return;
        }
        if (player.isChargedShot(ShotType.SOULSHOT))
            return;
        int ssCount = weaponItem.getSoulShotCount();
        if (weaponItem.getReducedSoulShot() > 0 && Rnd.get(100) < weaponItem.getReducedSoulShotChance())
            ssCount = weaponItem.getReducedSoulShot();
        if (!Config.INFINITY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), ssCount, null, false)) {
            if (!player.disableAutoShot(item.getItemId()))
                player.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
            return;
        }
        IntIntHolder[] skills = item.getItem().getSkills();
        weaponInst.setChargedShot(ShotType.SOULSHOT, true);
        player.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
        player.broadcastPacketInRadius(new MagicSkillUse(player, player, skills[0].getId(), 1, 0, 0), 600);
    }
}
