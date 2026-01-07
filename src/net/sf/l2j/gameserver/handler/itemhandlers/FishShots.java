package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class FishShots implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        ItemInstance weaponInst = player.getActiveWeaponInstance();
        Weapon weaponItem = player.getActiveWeaponItem();
        if (weaponInst == null || weaponItem.getItemType() != WeaponType.FISHINGROD)
            return;
        if (player.isChargedShot(ShotType.FISH_SOULSHOT))
            return;
        if (weaponItem.getCrystalType() != item.getItem().getCrystalType()) {
            player.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
            return;
        }
        if (!player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false)) {
            player.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
            return;
        }
        IntIntHolder[] skills = item.getItem().getSkills();
        player.setChargedShot(ShotType.FISH_SOULSHOT, true);
        player.broadcastPacket(new MagicSkillUse(player, skills[0].getId(), 1, 0, 0));
    }
}
