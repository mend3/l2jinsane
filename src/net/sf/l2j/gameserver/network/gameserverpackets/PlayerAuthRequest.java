package net.sf.l2j.gameserver.network.gameserverpackets;

import net.sf.l2j.gameserver.network.SessionKey;

public class PlayerAuthRequest extends GameServerBasePacket {
    public PlayerAuthRequest(String account, SessionKey key) {
        writeC(5);
        writeS(account);
        writeD(key.playOkID1());
        writeD(key.playOkID2());
        writeD(key.loginOkID1());
        writeD(key.loginOkID2());
    }

    public byte[] getContent() {
        return getBytes();
    }
}
