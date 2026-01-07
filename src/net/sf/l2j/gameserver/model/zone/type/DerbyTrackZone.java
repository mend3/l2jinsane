package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class DerbyTrackZone extends ZoneType {
    public DerbyTrackZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Playable) {
            character.setInsideZone(ZoneId.MONSTER_TRACK, true);
            character.setInsideZone(ZoneId.PEACE, true);
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof net.sf.l2j.gameserver.model.actor.Playable) {
            character.setInsideZone(ZoneId.MONSTER_TRACK, false);
            character.setInsideZone(ZoneId.PEACE, false);
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
