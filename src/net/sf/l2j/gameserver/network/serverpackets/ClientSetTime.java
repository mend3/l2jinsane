package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class ClientSetTime extends L2GameServerPacket {
    protected final void writeImpl() {
        writeC(236);
        writeD(GameTimeTaskManager.getInstance().getGameTime());
        writeD(6);
    }
}
