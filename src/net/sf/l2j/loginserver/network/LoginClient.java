/**/
package net.sf.l2j.loginserver.network;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.MMOClient;
import net.sf.l2j.commons.mmocore.MMOConnection;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.crypt.LoginCrypt;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.network.serverpackets.L2LoginServerPacket;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.PlayFail;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

public final class LoginClient extends MMOClient<MMOConnection<LoginClient>> {
    private static final CLogger LOGGER = new CLogger(LoginClient.class.getName());
    private final LoginCrypt _loginCrypt;
    private final ScrambledKeyPair _scrambledPair;
    private final byte[] _blowfishKey;
    private final int _sessionId;
    private final long _connectionStartTime;
    private LoginClient.LoginClientState _state;
    private String _account;
    private int _accessLevel;
    private int _lastServer;
    private SessionKey _sessionKey;
    private boolean _joinedGS;

    public LoginClient(MMOConnection<LoginClient> con) {
        super(con);
        this._state = LoginClient.LoginClientState.CONNECTED;
        this._scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
        this._blowfishKey = LoginController.getInstance().getBlowfishKey();
        this._sessionId = Rnd.nextInt();
        this._connectionStartTime = System.currentTimeMillis();
        this._loginCrypt = new LoginCrypt();
        this._loginCrypt.setKey(this._blowfishKey);
    }

    public String toString() {
        InetAddress address = this.getConnection().getInetAddress();
        String var10000;
        if (this.getState() == LoginClient.LoginClientState.AUTHED_LOGIN) {
            var10000 = this.getAccount();
            return "[" + var10000 + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
        } else {
            var10000 = address == null ? "disconnected" : address.getHostAddress();
            return "[" + var10000 + "]";
        }
    }

    public boolean decrypt(ByteBuffer buf, int size) {
        try {
            if (!this._loginCrypt.decrypt(buf.array(), buf.position(), size)) {
                super.getConnection().close(null);
                return false;
            } else {
                return true;
            }
        } catch (Exception var4) {
            LOGGER.error("Couldn't decrypt LoginClient packet.", var4);
            super.getConnection().close(null);
            return false;
        }
    }

    public void encrypt(ByteBuffer buf, int size) {
        int offset = buf.position();

        try {
            size = this._loginCrypt.encrypt(buf.array(), offset, size);
        } catch (Exception var5) {
            LOGGER.error("Couldn't encrypt LoginClient packet.", var5);
            return;
        }

        buf.position(offset + size);
    }

    public void onDisconnection() {
        if (!this.hasJoinedGS() || this.getConnectionStartTime() + 60000L < System.currentTimeMillis()) {
            LoginController.getInstance().removeAuthedLoginClient(this.getAccount());
        }

    }

    protected void onForcedDisconnection() {
    }

    public LoginClient.LoginClientState getState() {
        return this._state;
    }

    public void setState(LoginClient.LoginClientState state) {
        this._state = state;
    }

    public byte[] getBlowfishKey() {
        return this._blowfishKey;
    }

    public byte[] getScrambledModulus() {
        return this._scrambledPair.getScrambledModulus();
    }

    public RSAPrivateKey getRSAPrivateKey() {
        return (RSAPrivateKey) this._scrambledPair.getKeyPair().getPrivate();
    }

    public String getAccount() {
        return this._account;
    }

    public void setAccount(String account) {
        this._account = account;
    }

    public int getAccessLevel() {
        return this._accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this._accessLevel = accessLevel;
    }

    public int getLastServer() {
        return this._lastServer;
    }

    public void setLastServer(int lastServer) {
        this._lastServer = lastServer;
    }

    public int getSessionId() {
        return this._sessionId;
    }

    public boolean hasJoinedGS() {
        return this._joinedGS;
    }

    public void setJoinedGS(boolean val) {
        this._joinedGS = val;
    }

    public SessionKey getSessionKey() {
        return this._sessionKey;
    }

    public void setSessionKey(SessionKey sessionKey) {
        this._sessionKey = sessionKey;
    }

    public long getConnectionStartTime() {
        return this._connectionStartTime;
    }

    public void sendPacket(L2LoginServerPacket lsp) {
        this.getConnection().sendPacket(lsp);
    }

    public void close(LoginFail reason) {
        this.getConnection().close(reason);
    }

    public void close(PlayFail reason) {
        this.getConnection().close(reason);
    }

    public void close(L2LoginServerPacket lsp) {
        this.getConnection().close(lsp);
    }

    public enum LoginClientState {
        CONNECTED,
        AUTHED_GG,
        AUTHED_LOGIN;
    }
}