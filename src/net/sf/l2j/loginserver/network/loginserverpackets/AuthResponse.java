package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.GameServerManager;
import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket {
    public AuthResponse(int serverId) {
        writeC(2);
        writeC(serverId);
        writeS(GameServerManager.getInstance().getServerNames().get(Integer.valueOf(serverId)));
    }

    public byte[] getContent() {
        return getBytes();
    }
}
