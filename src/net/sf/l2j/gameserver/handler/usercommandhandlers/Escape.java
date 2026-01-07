package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Escape implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = new int[]{52};

    public boolean useUserCommand(int id, Player activeChar) {
        if (activeChar.isCastingNow() || activeChar.isSitting() || activeChar.isMovementDisabled() || activeChar.isOutOfControl() || activeChar.isInOlympiadMode() || activeChar.isInObserverMode() || activeChar.isFestivalParticipant() || activeChar.isInJail() || activeChar.isInsideZone(ZoneId.BOSS)) {
            activeChar.sendPacket(SystemMessageId.NO_UNSTUCK_PLEASE_SEND_PETITION);
            return false;
        }
        if ((TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(activeChar)) || activeChar.getDungeon() != null) {
            activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
            return false;
        }
        if ((CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(activeChar)) || activeChar.getDungeon() != null) {
            activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
            return false;
        }
        if ((DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(activeChar)) || activeChar.getDungeon() != null) {
            activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
            return false;
        }
        if (activeChar.isInEvent()) {
            activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
            return false;
        }
        if (activeChar.isInsideZone(ZoneId.PVPEVENT) || activeChar.isInsideZone(ZoneId.RANDOMZONE) || activeChar.isInsideZone(ZoneId.AUTOFARMZONE) || activeChar.isInsideZone(ZoneId.PARTYFARMZONE)) {
            activeChar.sendMessage("Use command: .exit");
            return false;
        }
        activeChar.stopMove(null);
        if (activeChar.isGM()) {
            activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
        } else {
            activeChar.sendPacket(new PlaySound("systemmsg_e.809"));
            int unstuckTimer = Config.UNSTUCK_TIME * 1000;
            L2Skill skill = SkillTable.getInstance().getInfo(2099, 1);
            skill.setHitTime(unstuckTimer);
            activeChar.doCast(SkillTable.getInstance().getInfo(2099, 1));
            if (unstuckTimer < 60000) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2).addString("You will unstuck in " + unstuckTimer / 1000 + " seconds."));
            } else {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2).addString("You will unstuck i " + unstuckTimer / 60000 + " minutes."));
            }
        }
        return true;
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
