package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.loginserver.network.SessionKey;

public final class LoginOk extends L2LoginServerPacket {
    private final int _loginOk1;

    private final int _loginOk2;

    public LoginOk(SessionKey sessionKey) {
        this._loginOk1 = sessionKey.loginOkID1;
        this._loginOk2 = sessionKey.loginOkID2;
    }

    protected void write() {
        writeC(3);
        writeD(this._loginOk1);
        writeD(this._loginOk2);
        writeD(0);
        writeD(0);
        writeD(1002);
        writeD(0);
        writeD(0);
        writeD(0);
        writeB(new byte[16]);
    }
}
