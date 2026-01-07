package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.serverpackets.GGAuth;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

public class AuthGameGuard extends L2LoginClientPacket {
    private int _sessionId;

    private int _data1;

    private int _data2;

    private int _data3;

    private int _data4;

    public int getSessionId() {
        return this._sessionId;
    }

    public int getData1() {
        return this._data1;
    }

    public int getData2() {
        return this._data2;
    }

    public int getData3() {
        return this._data3;
    }

    public int getData4() {
        return this._data4;
    }

    protected boolean readImpl() {
        if (this._buf.remaining() >= 20) {
            this._sessionId = readD();
            this._data1 = readD();
            this._data2 = readD();
            this._data3 = readD();
            this._data4 = readD();
            return true;
        }
        return false;
    }

    public void run() {
        if (this._sessionId == getClient().getSessionId()) {
            getClient().setState(LoginClient.LoginClientState.AUTHED_GG);
            getClient().sendPacket(new GGAuth(getClient().getSessionId()));
        } else {
            getClient().close(LoginFail.REASON_ACCESS_FAILED);
        }
    }
}
