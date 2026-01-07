package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class ScriptZone extends ZoneType {
    public ScriptZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.SCRIPT, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.SCRIPT, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
