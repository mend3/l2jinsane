package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class L2PvpEventZone extends SpawnZoneType {

    public L2PvpEventZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player activeChar) {
            if (!PvPEvent.getInstance().isActive()) {
                character.teleportTo(82725, 148596, -3468, 0);
                activeChar.sendMessage("PvP Event is not active !! .");
                return;
            }
            if (activeChar.isInsideZone(ZoneId.PVPEVENT))
                activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
            SkillTable.getInstance().getInfo(1323, 1).getEffects(character, character);
            if (activeChar.getPvpFlag() > 0)
                PvpFlagTaskManager.getInstance().remove(activeChar);
            activeChar.setPvpFlag(1);
            activeChar.broadcastUserInfo();
            activeChar.sendPacket(new CreatureSay(0, 2, character.getName(), ": Remember for exit use command .exit"));
        }
        character.setInsideZone(ZoneId.PVPEVENT, true);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.NO_RESTART, true);
        character.setInsideZone(ZoneId.NO_STORE, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.PVPEVENT, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.NO_RESTART, false);
        character.setInsideZone(ZoneId.NO_STORE, false);
        if (character instanceof Player activeChar) {
            if (activeChar.isInsideZone(ZoneId.PVPEVENT))
                activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
            activeChar.setPvpFlag(0);
            activeChar.broadcastUserInfo();
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }

    public void respawnCharacter(Creature character) {
        if (character == null || !character.isDead() || !(character instanceof Player))
            return;
        character.doRevive();
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentCp(character.getMaxCp());
        character.setCurrentMp(character.getMaxMp());
        SkillTable.getInstance().getInfo(1323, 1).getEffects(character, character);
        character.teleportTo(82635, 148798, -3464, 25);
    }
}
