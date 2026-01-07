package net.sf.l2j.loginserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.LoginPacketHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.LogManager;

public class LoginServer {
    public static final int PROTOCOL_REV = 258;
    private static final CLogger LOGGER = new CLogger(LoginServer.class.getName());
    private static LoginServer _loginServer;

    private GameServerListener _gameServerListener;

    private SelectorThread<LoginClient> _selectorThread;

    public LoginServer() throws Exception {
        (new File("./log")).mkdir();
        (new File("./log/console")).mkdir();
        (new File("./log/error")).mkdir();
        InputStream is = new FileInputStream(new File("config/logging.properties"));
        try {
            LogManager.getLogManager().readConfiguration(is);
            is.close();
        } catch (Throwable throwable) {
            try {
                is.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
        StringUtil.printSection("aCis");
        Config.loadLoginServer();
        StringUtil.printSection("Poolers");
        ConnectionPool.init();
        StringUtil.printSection("LoginController");
        LoginController.getInstance();
        StringUtil.printSection("GameServerManager");
        GameServerManager.getInstance();
        StringUtil.printSection("Ban List");
        loadBanFile();
        StringUtil.printSection("IP, Ports & Socket infos");
        InetAddress bindAddress = null;
        if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
            try {
                bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
            } catch (UnknownHostException uhe) {
                LOGGER.error("The LoginServer bind address is invalid, using all available IPs.", uhe);
            }
        SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
        LoginPacketHandler lph = new LoginPacketHandler();
        SelectorHelper sh = new SelectorHelper();
        try {
            this._selectorThread = new SelectorThread(sc, sh, lph, sh, sh);
        } catch (IOException ioe) {
            LOGGER.error("Failed to open selector.", ioe);
            System.exit(1);
        }
        try {
            this._gameServerListener = new GameServerListener();
            this._gameServerListener.start();
            LOGGER.info("Listening for gameservers on {}:{}.", Config.GAME_SERVER_LOGIN_HOST, Integer.valueOf(Config.GAME_SERVER_LOGIN_PORT));
        } catch (IOException ioe) {
            LOGGER.error("Failed to start the gameserver listener.", ioe);
            System.exit(1);
        }
        try {
            this._selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
        } catch (IOException ioe) {
            LOGGER.error("Failed to open server socket.", ioe);
            System.exit(1);
        }
        this._selectorThread.start();
        LOGGER.info("Loginserver ready on {}:{}.", (bindAddress == null) ? "*" : bindAddress.getHostAddress(), Integer.valueOf(Config.PORT_LOGIN));
        StringUtil.printSection("Waiting for gameserver answer");
    }

    public static void main(String[] args) throws Exception {
        _loginServer = new LoginServer();
    }

    public static LoginServer getInstance() {
        return _loginServer;
    }

    private static void loadBanFile() {
        File banFile = new File("config/banned_ips.properties");
        if (banFile.exists() && banFile.isFile()) {
            try {
                LineNumberReader reader = new LineNumberReader(new FileReader(banFile));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            String[] parts = line.split("#");
                            line = parts[0];
                            parts = line.split(" ");
                            String address = parts[0];
                            long duration = 0L;
                            if (parts.length > 1)
                                try {
                                    duration = Long.parseLong(parts[1]);
                                } catch (NumberFormatException e) {
                                    LOGGER.error("Incorrect ban duration ({}). Line: {}.", parts[1], Integer.valueOf(reader.getLineNumber()));
                                    continue;
                                }
                            try {
                                LoginController.getInstance().addBanForAddress(address, duration);
                            } catch (UnknownHostException e) {
                                LOGGER.error("Invalid address ({}). Line: {}.", parts[0], Integer.valueOf(reader.getLineNumber()));
                            }
                        }
                    }
                    reader.close();
                } catch (Throwable throwable) {
                    try {
                        reader.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                    throw throwable;
                }
            } catch (IOException e) {
                LOGGER.error("Error while reading banned_ips.properties.", e);
            }
            LOGGER.info("Loaded {} banned IP(s).", Integer.valueOf(LoginController.getInstance().getBannedIps().size()));
        } else {
            LOGGER.warn("banned_ips.properties is missing. Ban listing is skipped.");
        }
    }

    public GameServerListener getGameServerListener() {
        return this._gameServerListener;
    }

    public void shutdown(boolean restart) {
        Runtime.getRuntime().exit(restart ? 2 : 0);
    }
}
