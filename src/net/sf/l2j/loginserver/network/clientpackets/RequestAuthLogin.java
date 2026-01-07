package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.model.AccountInfo;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.*;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

public class RequestAuthLogin extends L2LoginClientPacket {
    private final byte[] _raw = new byte[128];

    private String _user;

    private String _password;

    private int _ncotp;

    public String getPassword() {
        return this._password;
    }

    public String getUser() {
        return this._user;
    }

    public int getOneTimePassword() {
        return this._ncotp;
    }

    public boolean readImpl() {
        if (this._buf.remaining() >= 128) {
            readB(this._raw);
            return true;
        }
        return false;
    }

    public void run() {
        LoginClient oldClient;
        GameServerInfo gsi;
        byte[] decrypted = null;
        LoginClient client = getClient();
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(2, client.getRSAPrivateKey());
            decrypted = rsaCipher.doFinal(this._raw, 0, 128);
        } catch (GeneralSecurityException e) {
            LOGGER.error("Failed to generate a cipher.", e);
            return;
        }
        try {
            this._user = (new String(decrypted, 94, 14)).trim().toLowerCase();
            this._password = (new String(decrypted, 108, 16)).trim();
            this._ncotp = decrypted[124];
            this._ncotp |= decrypted[125] << 8;
            this._ncotp |= decrypted[126] << 16;
            this._ncotp |= decrypted[127] << 24;
        } catch (Exception e) {
            LOGGER.error("Failed to decrypt user/password.", e);
            return;
        }
        InetAddress clientAddr = client.getConnection().getInetAddress();
        AccountInfo info = LoginController.getInstance().retrieveAccountInfo(clientAddr, this._user, this._password);
        if (info == null) {
            client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
            return;
        }
        LoginController.AuthLoginResult result = LoginController.getInstance().tryCheckinAccount(client, clientAddr, info);
        switch (result) {
            case AUTH_SUCCESS:
                client.setAccount(info.getLogin());
                client.setState(LoginClient.LoginClientState.AUTHED_LOGIN);
                client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
                client.sendPacket(Config.SHOW_LICENCE ? (L2LoginServerPacket) new LoginOk(client.getSessionKey()) : (L2LoginServerPacket) new ServerList(client));
                break;
            case INVALID_PASSWORD:
                client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
                break;
            case ACCOUNT_BANNED:
                client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
                break;
            case ALREADY_ON_LS:
                oldClient = LoginController.getInstance().getAuthedClient(info.getLogin());
                if (oldClient != null) {
                    oldClient.close(LoginFail.REASON_ACCOUNT_IN_USE);
                    LoginController.getInstance().removeAuthedLoginClient(info.getLogin());
                }
                client.close(LoginFail.REASON_ACCOUNT_IN_USE);
                break;
            case ALREADY_ON_GS:
                gsi = LoginController.getInstance().getAccountOnGameServer(info.getLogin());
                if (gsi != null) {
                    client.close(LoginFail.REASON_ACCOUNT_IN_USE);
                    if (gsi.isAuthed())
                        gsi.getGameServerThread().kickPlayer(info.getLogin());
                }
                break;
        }
    }
}
