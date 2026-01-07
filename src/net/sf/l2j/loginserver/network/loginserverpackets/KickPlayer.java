package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class KickPlayer extends ServerBasePacket {
    public KickPlayer(String account) {
        writeC(4);
        writeS(account);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
