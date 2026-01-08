package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.GMViewCharacterInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewHennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewItemList;
import net.sf.l2j.gameserver.network.serverpackets.GMViewSkillInfo;

public class AdminStatus implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_statustarget", "admin_inventorytarget", "admin_skillstarget"};

    public void useAdminCommand(String command, Player activeChar) {
        if (!activeChar.isGM()) {
            activeChar.sendMessage("You're not a GM.");
            return;
        }
        if (command.startsWith("admin_statustarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewCharacterInfo(targetPlayer));
            activeChar.sendPacket(new GMViewHennaInfo(targetPlayer));
            return;
        }
        if (command.startsWith("admin_inventorytarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewItemList(targetPlayer));
            return;
        }
        if (command.startsWith("admin_skillstarget")) {
            if (activeChar.getTarget() == null) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            if (!(activeChar.getTarget() instanceof Player targetCharacter)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            Player targetPlayer = targetCharacter.getActingPlayer();
            activeChar.sendPacket(new GMViewSkillInfo(targetPlayer));
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
