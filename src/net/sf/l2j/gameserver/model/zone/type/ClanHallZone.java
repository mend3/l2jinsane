package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;

public class ClanHallZone extends SpawnZoneType {
    private int _clanHallId;

    public ClanHallZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("clanHallId")) {
            this._clanHallId = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.CLAN_HALL, true);
            ClanHall ch = ClanHallManager.getInstance().getClanHall(this._clanHallId);
            if (ch == null)
                return;
            character.sendPacket(new ClanHallDecoration(ch));
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof Player)
            character.setInsideZone(ZoneId.CLAN_HALL, false);
    }

    public void banishForeigners(int clanId) {
        for (Player player : getKnownTypeInside(Player.class)) {
            if (player.getClanId() == clanId)
                continue;
            player.teleportTo(MapRegionData.TeleportType.TOWN);
        }
    }

    public int getClanHallId() {
        return this._clanHallId;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
