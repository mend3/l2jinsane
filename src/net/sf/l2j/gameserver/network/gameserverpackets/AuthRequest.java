package net.sf.l2j.gameserver.network.gameserverpackets;

public class AuthRequest extends GameServerBasePacket {
    public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, String host, int port, boolean reserveHost, int maxplayer) {
        writeC(1);
        writeC(id);
        writeC(acceptAlternate ? 1 : 0);
        writeC(reserveHost ? 1 : 0);
        writeS(host);
        writeH(port);
        writeD(maxplayer);
        writeD(hexid.length);
        writeB(hexid);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
