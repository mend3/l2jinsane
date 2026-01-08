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
        Player player = this.getClient().getPlayer();
        if (player != null) {
            if (!EngineModsManager.onExitWorld(player)) {
                if (player.getActiveEnchantItem() == null && !player.isLocked() && !player.isInStoreMode()) {
                    if (player.isInsideZone(ZoneId.MULTI_FUNCTION) && !Config.RESTART_ZONE) {
                        player.sendMessage("You cannot restart while inside a Multifunction zone.");
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (player.isInsideZone(ZoneId.NO_RESTART)) {
                        player.sendPacket(SystemMessageId.NO_RESTART_HERE);
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (AttackStanceTaskManager.getInstance().isInAttackStance(player)) {
                        player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) {
                        player.sendMessage("You cannot restart while in an event.");
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) {
                        player.sendMessage("You cannot restart while in an event.");
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
                        player.sendMessage("You cannot restart while in an event.");
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (player.getDungeon() != null) {
                        player.sendMessage("You cannot restart while in a dungeon.");
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else if (player.isFestivalParticipant() && FestivalOfDarknessManager.getInstance().isFestivalInitialized()) {
                        player.sendPacket(SystemMessageId.NO_RESTART_HERE);
                        this.sendPacket(RestartResponse.valueOf(false));
                    } else {
                        player.removeFromBossZone();
                        GameClient client = this.getClient();
                        player.setClient(null);
                        player.deleteMe();
                        client.setPlayer(null);
                        client.setState(GameClient.GameClientState.AUTHED);
                        this.sendPacket(RestartResponse.valueOf(true));
                        CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
                        this.sendPacket(cl);
                        client.setCharSelectSlot(cl.getCharacterSlots());
                    }
                } else {
                    this.sendPacket(RestartResponse.valueOf(false));
                }
            }
        }
    }
}
