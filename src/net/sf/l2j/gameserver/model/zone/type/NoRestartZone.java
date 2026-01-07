package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class NoRestartZone extends ZoneType {
    public NoRestartZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player)
            character.setInsideZone(ZoneId.NO_RESTART, true);
    }

    protected void onExit(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player)
            character.setInsideZone(ZoneId.NO_RESTART, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
