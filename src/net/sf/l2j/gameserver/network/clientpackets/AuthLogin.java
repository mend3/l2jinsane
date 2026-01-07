package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.network.SessionKey;

public final class AuthLogin extends L2GameClientPacket {
    private final byte[] _data = new byte[48];
    private String _loginName;
    private int _playKey1;
    private int _playKey2;
    private int _loginKey1;
    private int _loginKey2;

    protected void readImpl() {
        this._loginName = readS().toLowerCase();
        this._playKey2 = readD();
        this._playKey1 = readD();
        this._loginKey1 = readD();
        this._loginKey2 = readD();
    }

    protected void runImpl() {
        if (Hwid.isProtectionOn())
            if (!Hwid.doAuthLogin(getClient(), this._data, this._loginName))
                return;
        if (getClient().getAccountName() != null)
            return;
        getClient().setAccountName(this._loginName);
        getClient().setSessionId(new SessionKey(this._loginKey1, this._loginKey2, this._playKey1, this._playKey2));
        LoginServerThread.getInstance().addClient(this._loginName, getClient());
    }
}
