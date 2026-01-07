package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;

public class CastleZone extends SpawnZoneType {
    private int _castleId;

    public CastleZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("castleId")) {
            this._castleId = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.CASTLE, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.CASTLE, false);
    }

    public void banishForeigners(int clanId) {
        for (Player player : getKnownTypeInside(Player.class)) {
            if (player.getClanId() == clanId)
                continue;
            player.teleportTo(getRandomChaoticLoc(), 20);
        }
    }

    public int getCastleId() {
        return this._castleId;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
