package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class PlayerAuthResponse extends ServerBasePacket {
    public PlayerAuthResponse(String account, boolean response) {
        writeC(3);
        writeS(account);
        writeC(response ? 1 : 0);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
