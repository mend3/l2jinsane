package net.sf.l2j.gameserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.network.AttributeType;
import net.sf.l2j.commons.network.StatusType;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.FailReason;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.gameserverpackets.*;
import net.sf.l2j.gameserver.network.loginserverpackets.*;
import net.sf.l2j.gameserver.network.serverpackets.AuthLoginFail;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.loginserver.crypt.NewCrypt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginServerThread extends Thread {
    protected static final CLogger LOGGER = new CLogger(LoginServerThread.class.getName());
    private static final int REVISION = 258;
    private final Map<String, GameClient> _clients = new ConcurrentHashMap<>();
    private int _serverId;
    private String _serverName;
    private Socket _loginSocket;
    private InputStream _in;
    private OutputStream _out;
    private NewCrypt _blowfish;
    private byte[] _blowfishKey;
    private RSAPublicKey _publicKey;
    private byte[] _hexId;
    private final int _requestId;
    private int _maxPlayers;
    private StatusType _status;

    protected LoginServerThread() {
        super("LoginServerThread");
        this._status = StatusType.AUTO;
        this._hexId = Config.HEX_ID;
        if (this._hexId == null) {
            this._requestId = Config.REQUEST_ID;
            this._hexId = generateHex(16);
        } else {
            this._requestId = Config.SERVER_ID;
        }

        this._maxPlayers = Config.MAXIMUM_ONLINE_USERS;
    }

    public static byte[] generateHex(int size) {
        byte[] array = new byte[size];
        Rnd.nextBytes(array);
        return array;
    }

    public static LoginServerThread getInstance() {
        return LoginServerThread.SingletonHolder.INSTANCE;
    }

    public void run() {
        while (!this.isInterrupted()) {
            try {
                LOGGER.info("Connecting to login on {}:{}.", Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
                this._loginSocket = new Socket(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
                this._in = this._loginSocket.getInputStream();
                this._out = new BufferedOutputStream(this._loginSocket.getOutputStream());
                this._blowfishKey = generateHex(40);
                this._blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\u0000");

                while (!this.isInterrupted()) {
                    int lengthLo = this._in.read();
                    int lengthHi = this._in.read();
                    int length = lengthHi * 256 + lengthLo;
                    if (lengthHi < 0) {
                        break;
                    }

                    byte[] incoming = new byte[length - 2];
                    int receivedBytes = 0;
                    int newBytes = 0;

                    for (int left = length - 2; newBytes != -1 && receivedBytes < length - 2; left -= newBytes) {
                        newBytes = this._in.read(incoming, receivedBytes, left);
                        receivedBytes += newBytes;
                    }

                    if (receivedBytes != length - 2) {
                        LOGGER.warn("Incomplete packet is sent to the server, closing connection.");
                        break;
                    }

                    byte[] decrypt = this._blowfish.decrypt(incoming);
                    if (!NewCrypt.verifyChecksum(decrypt)) {
                        LOGGER.warn("Incorrect packet checksum, ignoring packet.");
                        break;
                    }

                    int packetType = decrypt[0] & 255;
                    switch (packetType) {
                        case 0:
                            InitLS init = new InitLS(decrypt);
                            if (init.getRevision() != 258) {
                                LOGGER.warn("Revision mismatch between LS and GS.");
                            } else {
                                try {
                                    KeyFactory kfac = KeyFactory.getInstance("RSA");
                                    BigInteger modulus = new BigInteger(init.getRSAKey());
                                    RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
                                    this._publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
                                } catch (GeneralSecurityException var31) {
                                    LOGGER.error("Troubles while init the public key sent by login.");
                                    continue;
                                }

                                this.sendPacket(new BlowFishKey(this._blowfishKey, this._publicKey));
                                this._blowfish = new NewCrypt(this._blowfishKey);
                                this.sendPacket(new AuthRequest(this._requestId, Config.ACCEPT_ALTERNATE_ID, this._hexId, Config.HOSTNAME, Config.PORT_GAME, Config.RESERVE_HOST_ON_LOGIN, this._maxPlayers));
                            }
                            break;
                        case 1:
                            LoginServerFail lsf = new LoginServerFail(decrypt);
                            LOGGER.info("LoginServer registration failed: {}.", lsf.getReasonString());
                            break;
                        case 2:
                            AuthResponse aresp = new AuthResponse(decrypt);
                            this._serverId = aresp.getServerId();
                            this._serverName = aresp.getServerName();
                            Config.saveHexid(this._serverId, (new BigInteger(this._hexId)).toString(16));
                            LOGGER.info("Registered as server: [{}] {}.", this._serverId, this._serverName);
                            ServerStatus ss = new ServerStatus();
                            ss.addAttribute(AttributeType.STATUS, Config.SERVER_GMONLY ? StatusType.GM_ONLY.getId() : StatusType.AUTO.getId());
                            ss.addAttribute(AttributeType.CLOCK, Config.SERVER_LIST_CLOCK);
                            ss.addAttribute(AttributeType.BRACKETS, Config.SERVER_LIST_BRACKET);
                            ss.addAttribute(AttributeType.AGE_LIMIT, Config.SERVER_LIST_AGE);
                            ss.addAttribute(AttributeType.TEST_SERVER, Config.SERVER_LIST_TESTSERVER);
                            ss.addAttribute(AttributeType.PVP_SERVER, Config.SERVER_LIST_PVPSERVER);
                            this.sendPacket(ss);
                            Collection<Player> players = World.getInstance().getPlayers();
                            if (players.isEmpty()) {
                                break;
                            }

                            List<String> playerList = new ArrayList<>();

                            for (Player player : players) {
                                playerList.add(player.getAccountName());
                            }

                            this.sendPacket(new PlayerInGame(playerList));
                            break;
                        case 3:
                            PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
                            GameClient client = this._clients.get(par.getAccount());
                            if (client != null) {
                                if (par.isAuthed()) {
                                    this.sendPacket(new PlayerInGame(par.getAccount()));
                                    client.setState(GameClient.GameClientState.AUTHED);
                                    client.sendPacket(new CharSelectInfo(par.getAccount(), client.getSessionId().playOkID1()));
                                } else {
                                    client.sendPacket(new AuthLoginFail(FailReason.SYSTEM_ERROR_LOGIN_LATER));
                                    client.closeNow();
                                }
                            }
                            break;
                        case 4:
                            KickPlayer kp = new KickPlayer(decrypt);
                            this.kickPlayer(kp.getAccount());
                    }
                }
            } catch (UnknownHostException ignored) {
            } catch (IOException var33) {
                LOGGER.error("No connection found with loginserver, next try in 10 seconds.");
            } finally {
                try {
                    this._loginSocket.close();
                } catch (Exception ignored) {
                }

            }
            if (this.isInterrupted()) {
                return;
            }

            try {
                Thread.sleep(10000L);
            } catch (InterruptedException var30) {
                return;
            }
        }

    }

    public void sendLogout(String account) {
        if (account != null) {
            try {
                this.sendPacket(new PlayerLogout(account));
            } catch (IOException var6) {
                LOGGER.error("Error while sending logout packet to login.");
            } finally {
                this._clients.remove(account);
            }

        }
    }

    public void addClient(String account, GameClient client) {
        GameClient existingClient = this._clients.putIfAbsent(account, client);
        if (!client.isDetached()) {
            if (existingClient == null) {
                try {
                    this.sendPacket(new PlayerAuthRequest(client.getAccountName(), client.getSessionId()));
                } catch (IOException var5) {
                    LOGGER.error("Error while sending player auth request.");
                }
            } else {
                client.closeNow();
                existingClient.closeNow();
            }

        }
    }

    public void sendAccessLevel(String account, int level) {
        try {
            this.sendPacket(new ChangeAccessLevel(account, level));
        } catch (IOException ignored) {
        }

    }

    public void kickPlayer(String account) {
        GameClient client = this._clients.get(account);
        if (client != null) {
            client.closeNow();
        }

    }

    private void sendPacket(GameServerBasePacket sl) throws IOException {
        byte[] data = sl.getContent();
        NewCrypt.appendChecksum(data);
        data = this._blowfish.crypt(data);
        int len = data.length + 2;
        synchronized (this._out) {
            this._out.write(len & 255);
            this._out.write(len >> 8 & 255);
            this._out.write(data);
            this._out.flush();
        }
    }

    public void setMaxPlayer(int maxPlayers) {
        this.sendServerStatus(AttributeType.MAX_PLAYERS, maxPlayers);
        this._maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return this._maxPlayers;
    }

    public void sendServerStatus(AttributeType type, int value) {
        try {
            ServerStatus ss = new ServerStatus();
            ss.addAttribute(type, value);
            this.sendPacket(ss);
        } catch (IOException ignored) {
        }

    }

    public String getServerName() {
        return this._serverName;
    }

    public StatusType getServerStatus() {
        return this._status;
    }

    public void setServerStatus(StatusType status) {
        this.sendServerStatus(AttributeType.STATUS, status.getId());
        this._status = status;
    }

    private static class SingletonHolder {
        protected static final LoginServerThread INSTANCE = new LoginServerThread();
    }
}
