package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

public class RequestServerList extends L2LoginClientPacket {
    private int _skey1;

    private int _skey2;

    private int _data3;

    public int getSessionKey1() {
        return this._skey1;
    }

    public int getSessionKey2() {
        return this._skey2;
    }

    public int getData3() {
        return this._data3;
    }

    public boolean readImpl() {
        if (this._buf.remaining() >= 8) {
            this._skey1 = readD();
            this._skey2 = readD();
            return true;
        }
        return false;
    }

    public void run() {
        if (getClient().getSessionKey().checkLoginPair(this._skey1, this._skey2)) {
            getClient().sendPacket(new ServerList(getClient()));
        } else {
            getClient().close(LoginFail.REASON_ACCESS_FAILED);
        }
    }
}
