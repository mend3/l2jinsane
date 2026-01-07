package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class HqZone extends ZoneType {
    public HqZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player)
            character.setInsideZone(ZoneId.HQ, true);
    }

    protected void onExit(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player)
            character.setInsideZone(ZoneId.HQ, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
