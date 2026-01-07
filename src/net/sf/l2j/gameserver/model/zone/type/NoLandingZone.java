package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class NoLandingZone extends ZoneType {
    public NoLandingZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.NO_LANDING, true);
            ((Player) character).enterOnNoLandingZone();
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.NO_LANDING, false);
            ((Player) character).exitOnNoLandingZone();
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
