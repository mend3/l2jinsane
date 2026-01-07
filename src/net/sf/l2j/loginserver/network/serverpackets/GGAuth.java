package net.sf.l2j.loginserver.network.serverpackets;

public final class GGAuth extends L2LoginServerPacket {
    public static final int SKIP_GG_AUTH_REQUEST = 11;

    private final int _response;

    public GGAuth(int response) {
        this._response = response;
    }

    protected void write() {
        writeC(11);
        writeD(this._response);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
    }
}
