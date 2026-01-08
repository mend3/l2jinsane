package net.sf.l2j.gameserver.hwid;

import net.sf.l2j.gameserver.hwid.crypt.Manager;
import net.sf.l2j.gameserver.hwid.hwidmanager.HwidBan;
import net.sf.l2j.gameserver.hwid.hwidmanager.HwidManager;
import net.sf.l2j.gameserver.hwid.hwidmanager.HwidPlayer;
import net.sf.l2j.gameserver.hwid.utils.Util;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.loginserver.crypt.BlowfishEngine;
import net.sf.l2j.util.HWID;
import net.sf.l2j.util.IPLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Hwid {
    private static final byte[] _key = new byte[16];
    protected static final Logger _log = Logger.getLogger(Hwid.class.getName());
    protected static final ConcurrentHashMap<String, Manager.InfoSet> _objects = new ConcurrentHashMap<>();
    static byte version = 11;

    public static void Init() {
        HwidConfig.load();
        if (isProtectionOn()) {
            Manager.getInstance();
            HwidBan.getInstance();
            HwidPlayer.getInstance();
            HwidManager.getInstance();
        }
    }

    public static boolean isProtectionOn() {
        return HwidConfig.ALLOW_GUARD_SYSTEM;
    }

    public static byte[] getKey(byte[] key) {
        byte[] bfkey = {
                110, 36, 2, 15, -5, 17, 24, 23, 18, 45,
                1, 21, 122, 16, -5, 12};
        try {
            BlowfishEngine bf = new BlowfishEngine();
            bf.init(true, bfkey);
            bf.processBlock(key, 0, _key, 0);
            bf.processBlock(key, 8, _key, 8);
        } catch (IOException e) {
            _log.info("Bad key!!!");
        }
        return _key;
    }

    public static void addPlayer(GameClient client) {
        if (isProtectionOn() && client != null)
            Manager.getInstance().addPlayer(client);
    }

    public static void removePlayer(GameClient client) {
        if (isProtectionOn() && client != null)
            Manager.removePlayer(client.getPlayerName());
    }

    public static boolean checkVerfiFlag(GameClient client, int flag) {
        boolean result = true;
        int fl = Integer.reverseBytes(flag);
        if (fl == -1)
            return false;
        if (fl == 1342177280)
            return false;
        if ((fl & 0x1) != 0)
            result = false;
        if ((fl & 0x10) != 0)
            result = false;
        if ((fl & 0x10000000) != 0)
            result = false;
        return result;
    }

    public static int dumpData(int _id, int position, GameClient pi) {
        int code, value = 0;
        position = (position > 4) ? 5 : position;
        boolean isIdZero = _id == 0;
        switch (position) {
            case 0:
                if (_id != 1435233386) {
                    if (!isIdZero) ;
                    value = 1;
                }
                return value;
            case 1:
                if (_id != 16) {
                    if (!isIdZero) ;
                    value = 2;
                }
                return value;
            case 2:
            case 3:
            case 4:
                code = _id & 0xFF000000;
                if (code == 204) ;
                if (code == 233)
                    value = 3;
                return value;
        }
        value = 0;
        return value;
    }

    public static int calcPenalty(byte[] data, GameClient pi) {
        int sum = -1;
        if (Util.verifyChecksum(data, 0, data.length)) {
            ByteBuffer buf = ByteBuffer.wrap(data, 0, data.length - 4);
            sum = 0;
            int lenPenalty = (data.length - 4) / 4;
            for (int i = 0; i < lenPenalty; i++)
                sum += dumpData(buf.getInt(), i, pi);
        }
        return sum;
    }

    public static boolean CheckHWIDs(GameClient client, int LastError1, int LastError2) {
        boolean resultHWID = false;
        boolean resultLastError = false;
        String HWID = client.getHWID();
        if (HWID.equalsIgnoreCase("fab800b1cc9de379c8046519fa841e6"))
            if (HwidConfig.PROTECT_KICK_WITH_EMPTY_HWID)
                resultHWID = true;
        if (LastError1 != 0)
            if (HwidConfig.PROTECT_KICK_WITH_LASTERROR_HWID)
                resultLastError = true;
        return (resultHWID || resultLastError);
    }

    public static String fillHex(int data, int digits) {
        StringBuilder number = new StringBuilder(Integer.toHexString(data));
        for (int i = number.length(); i < digits; i++)
            number.insert(0, "0");
        return number.toString();
    }

    public static String ExtractHWID(byte[] _data) {
        if (!Util.verifyChecksum(_data, 0, _data.length))
            return null;
        StringBuilder resultHWID = new StringBuilder();
        for (int i = 0; i < _data.length - 8; i++)
            resultHWID.append(fillHex(_data[i] & 0xFF, 2));
        return resultHWID.toString();
    }

    public static boolean doAuthLogin(GameClient client, byte[] data, String loginName) {
        if (!isProtectionOn())
            return true;
        client.setLoginName(loginName);
        String fullHWID = ExtractHWID(data);
        if (fullHWID == null) {
            _log.info("AuthLogin CRC Check Fail! May be BOT or unprotected client! Client IP: " + client);
            client.close(ServerClose.STATIC_PACKET);
            return false;
        }
        int LastError1 = ByteBuffer.wrap(data, 16, 4).getInt();
        if (CheckHWIDs(client, LastError1, 0)) {
            _log.info("HWID error, look protection_logs.txt file, from IP: " + client);
            client.close(ServerClose.STATIC_PACKET);
            return false;
        }
        if (HwidBan.getInstance().checkFullHWIDBanned(client)) {
            _log.warning("Client " + client + " is banned. Kicked! |HWID: " + client.getHWID() + " IP: " + client);
            client.close(ServerClose.STATIC_PACKET);
        }
        int VerfiFlag = ByteBuffer.wrap(data, 40, 4).getInt();
        return checkVerfiFlag(client, VerfiFlag);
    }

    public static void doDisconection(GameClient client) {
        removePlayer(client);
    }

    public static boolean checkPlayerWithHWID(GameClient client, int playerID, String playerName) {
        if (!isProtectionOn())
            return true;
        client.setPlayerName(playerName);
        client.setPlayerId(playerID);
        addPlayer(client);
        return true;
    }

    public static void nopath(GameClient client) {
        _log.info("Client " + client + " is no have path kick: " + client.getHWID() + " IP: " + client);
        client.close(ServerClose.STATIC_PACKET);
    }

    public static void enterlog(Player player, GameClient client) {
        if (HwidConfig.ALLOW_GUARD_SYSTEM && HwidConfig.ENABLE_CONSOLE_LOG) {
            _log.info("HWID: [" + client.getHWID() + "], character: [" + player.getName() + "] PlayerId: [" + player.getObjectId() + " ]");
        }
        int boxCanUse = HwidConfig.PROTECT_WINDOWS_COUNT + 1;
        if (boxCanUse < 0 || boxCanUse == 0) {
            boxCanUse = 1000;
            _log.info("HWID: [-Unlimited Boxes.-]");
        }
        HWID.auditGMAction(player.getHWID(), player.getName());
        IPLog.auditGMAction(player.getName(), player.getClient().getConnection().getInetAddress().getHostAddress(), player.getHWID());
        HwidManager.getInstance().validBox(player, boxCanUse, World.getInstance().getPlayers(), Boolean.TRUE);
    }

    public int getCountByHWID(String HWID) {
        int result = 0;
        for (Manager.InfoSet object : _objects.values()) {
            if (object._HWID.equals(HWID))
                result++;
        }
        return result;
    }
}
