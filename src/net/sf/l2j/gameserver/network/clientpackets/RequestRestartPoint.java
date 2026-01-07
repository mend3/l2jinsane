package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;

public final class RequestRestartPoint extends L2GameClientPacket {
    private static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);

    private int _requestType;

    protected void readImpl() {
        this._requestType = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isFakeDeath()) {
            player.stopFakeDeath(true);
            return;
        }
        if (!player.isDead())
            return;
        if (player.getClan() != null) {
            Siege siege = CastleManager.getInstance().getActiveSiege(player);
            if (siege != null && siege.checkSide(player.getClan(), SiegeSide.ATTACKER)) {
                ThreadPool.schedule(() -> portPlayer(player), Config.ATTACKERS_RESPAWN_DELAY);
                return;
            }
        }
        portPlayer(player);
    }

    private void portPlayer(Player player) {
        Clan clan = player.getClan();
        Location loc = null;
        if (player.isInJail()) {
            this._requestType = 27;
        } else if (player.isFestivalParticipant()) {
            this._requestType = 4;
        }
        if (this._requestType == 1) {
            if (clan == null || !clan.hasClanHall())
                return;
            loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CLAN_HALL);
            ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (ch != null) {
                ClanHallFunction function = ch.getFunction(5);
                if (function != null)
                    player.restoreExp(function.getLvl());
            }
        } else if (this._requestType == 2) {
            Siege siege = CastleManager.getInstance().getActiveSiege(player);
            if (siege != null) {
                SiegeSide side = siege.getSide(clan);
                if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER) {
                    loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE);
                } else if (side == SiegeSide.ATTACKER) {
                    loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.TOWN);
                } else {
                    return;
                }
            } else {
                if (clan == null || !clan.hasCastle())
                    return;
                loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE);
            }
        } else if (this._requestType == 3) {
            loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.SIEGE_FLAG);
        } else if (this._requestType == 4) {
            if (!player.isGM() && !player.isFestivalParticipant())
                return;
            SpawnLocation spawnLocation = player.getPosition();
        } else if (this._requestType == 27) {
            if (!player.isInJail())
                return;
            loc = JAIL_LOCATION;
        } else {
            loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.TOWN);
        }
        player.setIsIn7sDungeon(false);
        if (player.isDead())
            player.doRevive();
        player.teleportTo(loc, 20);
    }
}
