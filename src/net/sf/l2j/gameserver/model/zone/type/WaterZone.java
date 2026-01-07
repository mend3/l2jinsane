package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;

public class WaterZone extends ZoneType {
    public WaterZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.WATER, true);
        if (character instanceof Player) {
            ((Player) character).broadcastUserInfo();
        } else if (character instanceof Npc) {
            for (Player player : character.getKnownType(Player.class)) {
                if (character.getMoveSpeed() == 0) {
                    player.sendPacket(new ServerObjectInfo((Npc) character, player));
                    continue;
                }
                player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) character, player));
            }
        }
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.WATER, false);
        if (character instanceof Player) {
            ((Player) character).broadcastUserInfo();
        } else if (character instanceof Npc) {
            for (Player player : character.getKnownType(Player.class)) {
                if (character.getMoveSpeed() == 0) {
                    player.sendPacket(new ServerObjectInfo((Npc) character, player));
                    continue;
                }
                player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) character, player));
            }
        }
    }

    public int getWaterZ() {
        return getZone().getHighZ();
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
