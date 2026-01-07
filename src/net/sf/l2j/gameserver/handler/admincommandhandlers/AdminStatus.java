package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.GMViewCharacterInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewHennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewItemList;
import net.sf.l2j.gameserver.network.serverpackets.GMViewSkillInfo;

public class AdminStatus implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_statustarget", "admin_inventorytarget", "admin_skillstarget"};

    public boolean useAdminCommand(String command, Player activeChar) {
        if (!activeChar.isGM()) {
            activeChar.sendMessage("You're not a GM.");
            return false;
        }
        if (command.startsWith("admin_statustarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewCharacterInfo(targetPlayer));
            activeChar.sendPacket(new GMViewHennaInfo(targetPlayer));
            return true;
        }
        if (command.startsWith("admin_inventorytarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewItemList(targetPlayer));
            return true;
        }
        if (command.startsWith("admin_skillstarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return false;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewSkillInfo(targetPlayer));
            return true;
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
