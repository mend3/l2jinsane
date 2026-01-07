package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class PeaceZone extends ZoneType {
    public PeaceZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.PEACE, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.PEACE, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
