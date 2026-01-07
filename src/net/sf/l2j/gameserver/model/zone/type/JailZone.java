package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class JailZone extends ZoneType {
    public JailZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player) {
            character.setInsideZone(ZoneId.JAIL, true);
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
            character.setInsideZone(ZoneId.NO_STORE, true);
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Player) {
            character.setInsideZone(ZoneId.JAIL, false);
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
            character.setInsideZone(ZoneId.NO_STORE, false);
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
