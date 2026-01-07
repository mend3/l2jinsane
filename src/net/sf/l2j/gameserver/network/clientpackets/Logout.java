package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class Logout extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.getActiveEnchantItem() != null || player.isLocked()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInsideZone(ZoneId.MULTI_FUNCTION) && !Config.LOGOUT_ZONE) {
            player.sendMessage("You cannot Logout while inside a Multifunction zone.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInsideZone(ZoneId.NO_RESTART)) {
            player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (AttackStanceTaskManager.getInstance().isInAttackStance(player)) {
            player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot logout while in an event.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot logout while in an event.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot logout while in an event.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.getDungeon() != null) {
            player.sendMessage("You cannot logout while in a dungeon.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isFestivalParticipant() && FestivalOfDarknessManager.getInstance().isFestivalInitialized()) {
            player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (EngineModsManager.onExitWorld(player)) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        player.removeFromBossZone();
        player.logout(true);
    }
}
