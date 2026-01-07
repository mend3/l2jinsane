package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (EngineModsManager.onExitWorld(player))
            return;
        if (player.getActiveEnchantItem() != null || player.isLocked() || player.isInStoreMode()) {
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (player.isInsideZone(ZoneId.MULTI_FUNCTION) && !Config.RESTART_ZONE) {
            player.sendMessage("You cannot restart while inside a Multifunction zone.");
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (player.isInsideZone(ZoneId.NO_RESTART)) {
            player.sendPacket(SystemMessageId.NO_RESTART_HERE);
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (AttackStanceTaskManager.getInstance().isInAttackStance(player)) {
            player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot restart while in an event.");
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot restart while in an event.");
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot restart while in an event.");
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (player.getDungeon() != null) {
            player.sendMessage("You cannot restart while in a dungeon.");
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        if (player.isFestivalParticipant() && FestivalOfDarknessManager.getInstance().isFestivalInitialized()) {
            player.sendPacket(SystemMessageId.NO_RESTART_HERE);
            sendPacket(RestartResponse.valueOf(false));
            return;
        }
        player.removeFromBossZone();
        GameClient client = getClient();
        player.setClient(null);
        player.deleteMe();
        client.setPlayer(null);
        client.setState(GameClient.GameClientState.AUTHED);
        sendPacket(RestartResponse.valueOf(true));
        CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), (client.getSessionId()).playOkID1);
        sendPacket(cl);
        client.setCharSelectSlot(cl.getCharacterSlots());
    }
}
