package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IUserCommandHandler {
    void useUserCommand(int paramInt, Player paramPlayer);

    int[] getUserCommandList();
}
