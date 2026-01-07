package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.CastleZoneType;

public class SwampZone extends CastleZoneType {
    private int _moveBonus = -50;

    public SwampZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("move_bonus")) {
            this._moveBonus = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress()))
            return;
        character.setInsideZone(ZoneId.SWAMP, true);
        if (character instanceof Player)
            ((Player) character).broadcastUserInfo();
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.SWAMP, false);
        if (character instanceof Player)
            ((Player) character).broadcastUserInfo();
    }

    public int getMoveBonus() {
        return this._moveBonus;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
