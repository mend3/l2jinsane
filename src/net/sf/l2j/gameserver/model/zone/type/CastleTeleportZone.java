package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class CastleTeleportZone extends ZoneType {
    private final int[] _spawnLoc = new int[5];
    private int _castleId;

    public CastleTeleportZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("castleId")) {
            this._castleId = Integer.parseInt(value);
        } else if (name.equals("spawnMinX")) {
            this._spawnLoc[0] = Integer.parseInt(value);
        } else if (name.equals("spawnMaxX")) {
            this._spawnLoc[1] = Integer.parseInt(value);
        } else if (name.equals("spawnMinY")) {
            this._spawnLoc[2] = Integer.parseInt(value);
        } else if (name.equals("spawnMaxY")) {
            this._spawnLoc[3] = Integer.parseInt(value);
        } else if (name.equals("spawnZ")) {
            this._spawnLoc[4] = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }

    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
    }

    public void oustAllPlayers() {
        for (Player player : this.getKnownTypeInside(Player.class)) {
            player.teleportTo(Rnd.get(this._spawnLoc[0], this._spawnLoc[1]), Rnd.get(this._spawnLoc[2], this._spawnLoc[3]), this._spawnLoc[4], 0);
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
