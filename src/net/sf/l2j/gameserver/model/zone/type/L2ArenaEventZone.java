package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class L2ArenaEventZone extends SpawnZoneType {
    public L2ArenaEventZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player player) {
            if (player.isArenaProtection()) {
                if (player.getPvpFlag() > 0)
                    PvpFlagTaskManager.getInstance().remove(player);
                player.updatePvPFlag(1);
                player.broadcastUserInfo();
            }
            player.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
        }
        character.setInsideZone(ZoneId.ARENA_EVENT, true);
        character.setInsideZone(ZoneId.PVP, true);
    }

    protected void onExit(Creature character) {
        if (character instanceof Player player) {
            player.updatePvPFlag(0);
            player.broadcastUserInfo();
            player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
        }
        character.setInsideZone(ZoneId.ARENA_EVENT, false);
        character.setInsideZone(ZoneId.PVP, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
