package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class NoSummonFriendZone extends ZoneType {
    public NoSummonFriendZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
