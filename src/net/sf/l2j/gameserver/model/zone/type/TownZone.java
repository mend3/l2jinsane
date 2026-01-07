package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;

public class TownZone extends SpawnZoneType {
    private int _townId;

    private int _castleId;

    private boolean _isPeaceZone = true;

    public TownZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("townId")) {
            this._townId = Integer.parseInt(value);
        } else if (name.equals("castleId")) {
            this._castleId = Integer.parseInt(value);
        } else if (name.equals("isPeaceZone")) {
            this._isPeaceZone = Boolean.parseBoolean(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        if (Config.ZONE_TOWN == 1 && character instanceof Player && ((Player) character).getSiegeState() != 0)
            return;
        if (this._isPeaceZone && Config.ZONE_TOWN != 2)
            character.setInsideZone(ZoneId.PEACE, true);
        character.setInsideZone(ZoneId.TOWN, true);
    }

    protected void onExit(Creature character) {
        if (this._isPeaceZone)
            character.setInsideZone(ZoneId.PEACE, false);
        character.setInsideZone(ZoneId.TOWN, false);
    }

    public int getTownId() {
        return this._townId;
    }

    public final int getCastleId() {
        return this._castleId;
    }

    public final boolean isPeaceZone() {
        return this._isPeaceZone;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
