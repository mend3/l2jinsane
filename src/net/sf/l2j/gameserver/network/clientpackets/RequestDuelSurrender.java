package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.DuelManager;

public final class RequestDuelSurrender extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        DuelManager.getInstance().doSurrender(getClient().getPlayer());
    }
}
