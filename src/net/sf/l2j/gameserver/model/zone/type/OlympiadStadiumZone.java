package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class OlympiadStadiumZone extends SpawnZoneType {
    OlympiadGameTask _task = null;

    public OlympiadStadiumZone(int id) {
        super(id);
    }

    protected final void onEnter(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.NO_RESTART, true);
        if (this._task != null && this._task.isBattleStarted()) {
            character.setInsideZone(ZoneId.PVP, true);
            if (character instanceof Player) {
                character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
                this._task.getGame().sendOlympiadInfo(character);
            }
        }
        Player player = character.getActingPlayer();
        if (player != null && !player.isGM() && !player.isInOlympiadMode() && !player.isInObserverMode()) {
            Summon summon = player.getSummon();
            if (summon != null)
                summon.unSummon(player);
            player.teleportTo(MapRegionData.TeleportType.TOWN);
        }
    }

    protected final void onExit(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.NO_RESTART, false);
        if (this._task != null && this._task.isBattleStarted()) {
            character.setInsideZone(ZoneId.PVP, false);
            if (character instanceof Player) {
                character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
                character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
            }
        }
    }

    public final void updateZoneStatusForCharactersInside() {
        if (this._task == null)
            return;
        boolean battleStarted = this._task.isBattleStarted();
        SystemMessage sm = SystemMessage.getSystemMessage(battleStarted ? SystemMessageId.ENTERED_COMBAT_ZONE : SystemMessageId.LEFT_COMBAT_ZONE);
        for (Creature character : this._characters.values()) {
            if (battleStarted) {
                character.setInsideZone(ZoneId.PVP, true);
                if (character instanceof Player)
                    character.sendPacket(sm);
                continue;
            }
            character.setInsideZone(ZoneId.PVP, false);
            if (character instanceof Player) {
                character.sendPacket(sm);
                character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
            }
        }
    }

    public final void registerTask(OlympiadGameTask task) {
        this._task = task;
    }

    public final void broadcastStatusUpdate(Player player) {
        ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
        for (Player plyr : getKnownTypeInside(Player.class)) {
            if (plyr.isInObserverMode() || plyr.getOlympiadSide() != player.getOlympiadSide())
                plyr.sendPacket(packet);
        }
    }

    public final void broadcastPacketToObservers(L2GameServerPacket packet) {
        for (Player player : getKnownTypeInside(Player.class)) {
            if (player.isInObserverMode())
                player.sendPacket(packet);
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
