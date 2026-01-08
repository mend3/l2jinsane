package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

public class L2SkillTeleport extends L2Skill {
    private final String _recallType;
    private final Location _loc;

    public L2SkillTeleport(StatSet set) {
        super(set);
        this._recallType = set.getString("recallType", "");
        String coords = set.getString("teleCoords", null);
        if (coords != null) {
            String[] valuesSplit = coords.split(",");
            this._loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
        } else {
            this._loc = null;
        }

    }

    public void useSkill(Creature activeChar, WorldObject[] targets) {
        if (!(activeChar instanceof Player) || !activeChar.isAfraid() && !((Player) activeChar).isInOlympiadMode() && !activeChar.isInsideZone(ZoneId.BOSS)) {
            boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);

            for (WorldObject obj : targets) {
                if (obj instanceof Creature) {
                    Creature target = (Creature) obj;
                    if (target instanceof Player) {
                        Player targetChar = (Player) target;
                        if (targetChar.isFestivalParticipant() || targetChar.isInJail() || targetChar.isInDuel() || targetChar != activeChar && (targetChar.isInOlympiadMode() || targetChar.isInsideZone(ZoneId.BOSS))) {
                            continue;
                        }
                    }

                    Location loc = null;
                    if (this.getSkillType() == L2SkillType.TELEPORT) {
                        if (this._loc != null && (!(target instanceof Player) || !target.isFlying())) {
                            loc = this._loc;
                        }
                    } else if (this._recallType.equalsIgnoreCase("Castle")) {
                        loc = MapRegionData.getInstance().getLocationToTeleport(target, MapRegionData.TeleportType.CASTLE);
                    } else if (this._recallType.equalsIgnoreCase("ClanHall")) {
                        loc = MapRegionData.getInstance().getLocationToTeleport(target, MapRegionData.TeleportType.CLAN_HALL);
                    } else {
                        loc = MapRegionData.getInstance().getLocationToTeleport(target, MapRegionData.TeleportType.TOWN);
                    }

                    if (loc != null) {
                        if (target instanceof Player) {
                            ((Player) target).setIsIn7sDungeon(false);
                        }

                        target.teleportTo(loc, 20);
                    }
                }
            }

            activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, this.isStaticReuse());
        }
    }
}
