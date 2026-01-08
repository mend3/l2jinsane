package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class ArenaZone extends SpawnZoneType {
    public ArenaZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player) {
            ((Player) character).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
        }

        character.setInsideZone(ZoneId.PVP, true);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.PVP, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        if (character instanceof Player) {
            ((Player) character).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
        }

    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
