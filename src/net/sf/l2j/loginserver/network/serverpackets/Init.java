package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.loginserver.network.LoginClient;

public final class Init extends L2LoginServerPacket {
    private final int _sessionId;

    private final byte[] _publicKey;

    private final byte[] _blowfishKey;

    public Init(LoginClient client) {
        this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
    }

    public Init(byte[] publickey, byte[] blowfishkey, int sessionId) {
        this._sessionId = sessionId;
        this._publicKey = publickey;
        this._blowfishKey = blowfishkey;
    }

    protected void write() {
        writeC(0);
        writeD(this._sessionId);
        writeD(50721);
        writeB(this._publicKey);
        writeD(702387534);
        writeD(2009308412);
        writeD(-1750223328);
        writeD(129884407);
        writeB(this._blowfishKey);
        writeC(0);
    }
}
