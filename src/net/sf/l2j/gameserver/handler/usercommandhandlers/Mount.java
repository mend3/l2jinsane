package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class Mount implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = new int[]{61};

    public boolean useUserCommand(int id, Player activeChar) {
        return activeChar.mountPlayer(activeChar.getSummon());
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
