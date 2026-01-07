package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.PlayFail;
import net.sf.l2j.loginserver.network.serverpackets.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket {
    private int _skey1;

    private int _skey2;

    private int _serverId;

    public int getSessionKey1() {
        return this._skey1;
    }

    public int getSessionKey2() {
        return this._skey2;
    }

    public int getServerID() {
        return this._serverId;
    }

    public boolean readImpl() {
        if (this._buf.remaining() >= 9) {
            this._skey1 = readD();
            this._skey2 = readD();
            this._serverId = readC();
            return true;
        }
        return false;
    }

    public void run() {
        SessionKey sk = getClient().getSessionKey();
        if (!Config.SHOW_LICENCE || sk.checkLoginPair(this._skey1, this._skey2)) {
            if (LoginController.getInstance().isLoginPossible(getClient(), this._serverId)) {
                getClient().setJoinedGS(true);
                getClient().sendPacket(new PlayOk(sk));
            } else {
                getClient().close(PlayFail.REASON_TOO_MANY_PLAYERS);
            }
        } else {
            getClient().close(LoginFail.REASON_ACCESS_FAILED);
        }
    }
}
