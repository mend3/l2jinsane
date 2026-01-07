package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.FishingZone;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class Fishing implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.FISHING};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        if (player.isFishing()) {
            player.getFishingStance().end(false);
            player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
            return;
        }
        if (player.getAttackType() != WeaponType.FISHINGROD) {
            player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
            return;
        }
        if (player.isInBoat()) {
            player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
            return;
        }
        if (player.isCrafting() || player.isInStoreMode()) {
            player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
            return;
        }
        if (player.isInsideZone(ZoneId.WATER)) {
            player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
            return;
        }
        ItemInstance lure = player.getInventory().getPaperdollItem(8);
        if (lure == null) {
            player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
            return;
        }
        int rnd = Rnd.get(50) + 250;
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(player.getHeading()));
        int x = player.getX() + (int) (Math.cos(radian) * rnd);
        int y = player.getY() + (int) (Math.sin(radian) * rnd);
        boolean canFish = false;
        int z = 0;
        FishingZone zone = ZoneManager.getInstance().getZone(x, y, FishingZone.class);
        if (zone != null) {
            z = zone.getWaterZ();
            if (GeoEngine.getInstance().canSeeTarget(player, new Location(x, y, z)) && GeoEngine.getInstance().getHeight(x, y, z) < z) {
                z += 10;
                canFish = true;
            }
        }
        if (!canFish) {
            player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
            return;
        }
        if (!player.destroyItem("Consume", lure, 1, player, false)) {
            player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
            return;
        }
        player.getFishingStance().start(x, y, z, lure);
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
