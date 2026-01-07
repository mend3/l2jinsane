package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class VoicedPvPZoneExit implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = new String[]{"exit"};

    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if (command.equals("exit"))
            if (activeChar.isInsideZone(ZoneId.PVPEVENT) || activeChar.isInsideZone(ZoneId.RANDOMZONE) || activeChar.isInsideZone(ZoneId.AUTOFARMZONE) || activeChar
                    .isInsideZone(ZoneId.PARTYFARMZONE)) {
                if (activeChar.isInCombat())
                    activeChar.sendMessage("You cannot leave while you are in combat!");
                activeChar.getAI().setIntention(IntentionType.IDLE);
                activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
                activeChar.sendPacket(new ExShowScreenMessage("You will be teleported in 3 seconds", 3000, 2, true));
            } else {
                activeChar.sendMessage("You're not in PvP Event, Party Farm, Auto Farm or Random Zone. You can't exit from combat!");
            }
        return true;
    }

    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
