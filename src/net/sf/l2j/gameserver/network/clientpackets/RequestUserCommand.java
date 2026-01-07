package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class RequestUserCommand extends L2GameClientPacket {
    private int _command;

    protected void readImpl() {
        this._command = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(this._command);
        if (handler != null)
            handler.useUserCommand(this._command, getClient().getPlayer());
    }
}
