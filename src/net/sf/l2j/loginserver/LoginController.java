/**/
package net.sf.l2j.loginserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.model.AccountInfo;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginController {
    public static final int LOGIN_TIMEOUT = 60000;
    protected static final CLogger LOGGER = new CLogger(LoginController.class.getName());
    private static final String USER_INFO_SELECT = "SELECT login, password, access_level, lastServer FROM accounts WHERE login=?";
    private static final String AUTOCREATE_ACCOUNTS_INSERT = "INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)";
    private static final String ACCOUNT_INFO_UPDATE = "UPDATE accounts SET lastactive = ? WHERE login = ?";
    private static final String ACCOUNT_LAST_SERVER_UPDATE = "UPDATE accounts SET lastServer = ? WHERE login = ?";
    private static final String ACCOUNT_ACCESS_LEVEL_UPDATE = "UPDATE accounts SET access_level = ? WHERE login = ?";
    private static final int BLOWFISH_KEYS = 20;
    private final Map<InetAddress, Long> _bannedIps = new ConcurrentHashMap();
    private final Map<InetAddress, Integer> _failedAttempts = new ConcurrentHashMap();
    protected Map<String, LoginClient> _clients = new ConcurrentHashMap();
    protected ScrambledKeyPair[] _keyPairs = new ScrambledKeyPair[10];
    protected byte[][] _blowfishKeys;

    protected LoginController() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
            keygen.initialize(spec);

            for (int i = 0; i < 10; ++i) {
                this._keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
            }

            LOGGER.info("Cached 10 KeyPairs for RSA communication.");
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(2, this._keyPairs[0].getKeyPair().getPrivate());
            this._blowfishKeys = new byte[20][16];

            for (int i = 0; i < 20; ++i) {
                for (int j = 0; j < this._blowfishKeys[i].length; ++j) {
                    this._blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
                }
            }

            LOGGER.info("Stored {} keys for Blowfish communication.", this._blowfishKeys.length);
        } catch (GeneralSecurityException var6) {
            LOGGER.error("Failed generating keys.", var6);
        }

        Thread purge = new LoginController.PurgeThread();
        purge.setDaemon(true);
        purge.start();
    }

    private static boolean canCheckin(LoginClient client, InetAddress address, AccountInfo info) {
        client.setAccessLevel(info.getAccessLevel());
        client.setLastServer(info.getLastServer());

        try {
            Connection con = ConnectionPool.getConnection();

            boolean var5;
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET lastactive = ? WHERE login = ?");

                try {
                    ps.setLong(1, System.currentTimeMillis());
                    ps.setString(2, info.getLogin());
                    ps.execute();
                    var5 = true;
                } catch (Throwable var9) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }

                    throw var9;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var10) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }

                throw var10;
            }

            if (con != null) {
                con.close();
            }

            return var5;
        } catch (Exception var11) {
            LOGGER.error("Couldn't finish login process.", var11);
            return false;
        }
    }

    public static LoginController getInstance() {
        return LoginController.SingletonHolder.INSTANCE;
    }

    public byte[] getBlowfishKey() {
        return this._blowfishKeys[(int) (Math.random() * 20.0D)];
    }

    public void removeAuthedLoginClient(String account) {
        if (account != null) {
            this._clients.remove(account);
        }
    }

    public LoginClient getAuthedClient(String account) {
        return this._clients.get(account);
    }

    private void recordFailedAttempt(InetAddress addr) {
        int attempts = this._failedAttempts.merge(addr, 1, (k, v) -> {
            return k + v;
        });
        if (attempts >= Config.LOGIN_TRY_BEFORE_BAN) {
            this.addBanForAddress(addr, Config.LOGIN_BLOCK_AFTER_BAN * 1000L);
            this._failedAttempts.remove(addr);
            LOGGER.info("IP address: {} has been banned due to too many login attempts.", addr.getHostAddress());
        }

    }

    public AccountInfo retrieveAccountInfo(InetAddress addr, String login, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] raw = password.getBytes(StandardCharsets.UTF_8);
            String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
            Connection con = ConnectionPool.getConnection();

            AccountInfo var11;
            label181:
            {
                label182:
                {
                    PreparedStatement ps;
                    try {
                        label183:
                        {
                            label184:
                            {
                                label185:
                                {
                                    ps = con.prepareStatement("SELECT login, password, access_level, lastServer FROM accounts WHERE login=?");

                                    try {
                                        label186:
                                        {
                                            ps.setString(1, login);
                                            ResultSet rset = ps.executeQuery();

                                            label166:
                                            {
                                                label165:
                                                {
                                                    try {
                                                        if (rset.next()) {
                                                            AccountInfo info = new AccountInfo(rset.getString("login"), rset.getString("password"), rset.getInt("access_level"), rset.getInt("lastServer"));
                                                            if (!info.checkPassHash(hashBase64)) {
                                                                this.recordFailedAttempt(addr);
                                                                var11 = null;
                                                                break label166;
                                                            }

                                                            this._failedAttempts.remove(addr);
                                                            var11 = info;
                                                            break label165;
                                                        }
                                                    } catch (Throwable var20) {
                                                        if (rset != null) {
                                                            try {
                                                                rset.close();
                                                            } catch (Throwable var16) {
                                                                var20.addSuppressed(var16);
                                                            }
                                                        }

                                                        throw var20;
                                                    }

                                                    if (rset != null) {
                                                        rset.close();
                                                    }
                                                    break label186;
                                                }

                                                if (rset != null) {
                                                    rset.close();
                                                }
                                                break label184;
                                            }

                                            if (rset != null) {
                                                rset.close();
                                            }
                                            break label185;
                                        }
                                    } catch (Throwable var21) {
                                        if (ps != null) {
                                            try {
                                                ps.close();
                                            } catch (Throwable var15) {
                                                var21.addSuppressed(var15);
                                            }
                                        }

                                        throw var21;
                                    }

                                    if (ps != null) {
                                        ps.close();
                                    }
                                    break label183;
                                }

                                if (ps != null) {
                                    ps.close();
                                }
                                break label181;
                            }

                            if (ps != null) {
                                ps.close();
                            }
                            break label182;
                        }
                    } catch (Throwable var22) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var14) {
                                var22.addSuppressed(var14);
                            }
                        }

                        throw var22;
                    }

                    if (con != null) {
                        con.close();
                    }

                    if (!Config.AUTO_CREATE_ACCOUNTS) {
                        this.recordFailedAttempt(addr);
                        return null;
                    }

                    try {
                        con = ConnectionPool.getConnection();

                        try {
                            ps = con.prepareStatement("INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)");

                            try {
                                ps.setString(1, login);
                                ps.setString(2, hashBase64);
                                ps.setLong(3, System.currentTimeMillis());
                                ps.setInt(4, 0);
                                ps.execute();
                            } catch (Throwable var17) {
                                if (ps != null) {
                                    try {
                                        ps.close();
                                    } catch (Throwable var13) {
                                        var17.addSuppressed(var13);
                                    }
                                }

                                throw var17;
                            }

                            if (ps != null) {
                                ps.close();
                            }
                        } catch (Throwable var18) {
                            if (con != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var18.addSuppressed(var12);
                                }
                            }

                            throw var18;
                        }

                        if (con != null) {
                            con.close();
                        }
                    } catch (Exception var19) {
                        LOGGER.error("Exception auto creating account for {}.", var19, login);
                        return null;
                    }

                    LOGGER.info("Auto created account '{}'.", login);
                    return this.retrieveAccountInfo(addr, login, password);
                }

                if (con != null) {
                    con.close();
                }

                return var11;
            }

            if (con != null) {
                con.close();
            }

            return var11;
        } catch (Exception var23) {
            LOGGER.error("Exception retrieving account info for '{}'.", var23, login);
            return null;
        }
    }

    public LoginController.AuthLoginResult tryCheckinAccount(LoginClient client, InetAddress address, AccountInfo info) {
        if (info.getAccessLevel() < 0) {
            return LoginController.AuthLoginResult.ACCOUNT_BANNED;
        } else {
            LoginController.AuthLoginResult ret = LoginController.AuthLoginResult.INVALID_PASSWORD;
            if (canCheckin(client, address, info)) {
                ret = LoginController.AuthLoginResult.ALREADY_ON_GS;
                if (!this.isAccountInAnyGameServer(info.getLogin())) {
                    ret = LoginController.AuthLoginResult.ALREADY_ON_LS;
                    if (this._clients.putIfAbsent(info.getLogin(), client) == null) {
                        ret = LoginController.AuthLoginResult.AUTH_SUCCESS;
                    }
                }
            }

            return ret;
        }
    }

    public void addBanForAddress(String address, long expiration) throws UnknownHostException {
        this._bannedIps.putIfAbsent(InetAddress.getByName(address), expiration);
    }

    public void addBanForAddress(InetAddress address, long duration) {
        this._bannedIps.putIfAbsent(address, System.currentTimeMillis() + duration);
    }

    public boolean isBannedAddress(InetAddress address) {
        Long time = this._bannedIps.get(address);
        if (time != null) {
            if (time > 0L && time < System.currentTimeMillis()) {
                this._bannedIps.remove(address);
                LOGGER.info("Removed expired ip address ban {}.", address.getHostAddress());
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public Map<InetAddress, Long> getBannedIps() {
        return this._bannedIps;
    }

    public boolean removeBanForAddress(InetAddress address) {
        return this._bannedIps.remove(address) != null;
    }

    public boolean removeBanForAddress(String address) {
        try {
            return this.removeBanForAddress(InetAddress.getByName(address));
        } catch (UnknownHostException var3) {
            return false;
        }
    }

    public SessionKey getKeyForAccount(String account) {
        LoginClient client = this._clients.get(account);
        return client == null ? null : client.getSessionKey();
    }

    public boolean isAccountInAnyGameServer(String account) {
        Iterator var2 = GameServerManager.getInstance().getRegisteredGameServers().values().iterator();

        GameServerThread gst;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            GameServerInfo gsi = (GameServerInfo) var2.next();
            gst = gsi.getGameServerThread();
        } while (gst == null || !gst.hasAccountOnGameServer(account));

        return true;
    }

    public GameServerInfo getAccountOnGameServer(String account) {
        Iterator var2 = GameServerManager.getInstance().getRegisteredGameServers().values().iterator();

        GameServerInfo gsi;
        GameServerThread gst;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            gsi = (GameServerInfo) var2.next();
            gst = gsi.getGameServerThread();
        } while (gst == null || !gst.hasAccountOnGameServer(account));

        return gsi;
    }

    public boolean isLoginPossible(LoginClient client, int serverId) {
        GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServers().get(serverId);
        if (gsi != null && gsi.isAuthed()) {
            boolean canLogin = gsi.canLogin(client);
            if (canLogin && client.getLastServer() != serverId) {
                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("UPDATE accounts SET lastServer = ? WHERE login = ?");

                        try {
                            ps.setInt(1, serverId);
                            ps.setString(2, client.getAccount());
                            ps.executeUpdate();
                        } catch (Throwable var11) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var10) {
                                    var11.addSuppressed(var10);
                                }
                            }

                            throw var11;
                        }

                        if (ps != null) {
                            ps.close();
                        }
                    } catch (Throwable var12) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var9) {
                                var12.addSuppressed(var9);
                            }
                        }

                        throw var12;
                    }

                    if (con != null) {
                        con.close();
                    }
                } catch (Exception var13) {
                    LOGGER.error("Couldn't set lastServer.", var13);
                }
            }

            return canLogin;
        } else {
            return false;
        }
    }

    public void setAccountAccessLevel(String account, int banLevel) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET access_level = ? WHERE login = ?");

                try {
                    ps.setInt(1, banLevel);
                    ps.setString(2, account);
                    ps.executeUpdate();
                } catch (Throwable var9) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }

                    throw var9;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var10) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }

                throw var10;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var11) {
            LOGGER.error("Couldn't set access level {} for {}.", var11, banLevel, account);
        }

    }

    public ScrambledKeyPair getScrambledRSAKeyPair() {
        return Rnd.get(this._keyPairs);
    }

    public enum AuthLoginResult {
        INVALID_PASSWORD,
        ACCOUNT_BANNED,
        ALREADY_ON_LS,
        ALREADY_ON_GS,
        AUTH_SUCCESS;

        // $FF: synthetic method
        private static LoginController.AuthLoginResult[] $values() {
            return new LoginController.AuthLoginResult[]{INVALID_PASSWORD, ACCOUNT_BANNED, ALREADY_ON_LS, ALREADY_ON_GS, AUTH_SUCCESS};
        }
    }

    private static class SingletonHolder {
        protected static final LoginController INSTANCE = new LoginController();
    }

    private class PurgeThread extends Thread {
        public PurgeThread() {
            this.setName("PurgeThread");
        }

        public void run() {
            while (!this.isInterrupted()) {
                Iterator var1 = LoginController.this._clients.values().iterator();

                while (var1.hasNext()) {
                    LoginClient client = (LoginClient) var1.next();
                    if (client.getConnectionStartTime() + 60000L < System.currentTimeMillis()) {
                        client.close(LoginFail.REASON_ACCESS_FAILED);
                    }
                }

                try {
                    Thread.sleep(30000L);
                } catch (InterruptedException var3) {
                    return;
                }
            }

        }
    }
}