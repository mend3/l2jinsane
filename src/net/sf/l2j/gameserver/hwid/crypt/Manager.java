/**/
package net.sf.l2j.gameserver.hwid.crypt;

import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.hwid.hwidmanager.HwidPlayer;
import net.sf.l2j.gameserver.network.GameClient;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public final class Manager {
    private static final Logger _log = Logger.getLogger(Manager.class.getName());
    private static final String _logFile = "Manager";
    private static final String _logMainFile = "hwid_logs";
    private static final ScheduledFuture<?> _GGTask = null;
    private static final ConcurrentHashMap<String, Manager.InfoSet> _objects = new ConcurrentHashMap();
    private static Manager _instance;

    public static Manager getInstance() {
        if (_instance == null) {
            _log.info("- HWID Manager read successfully...");
            if (HwidConfig.PROTECT_WINDOWS_COUNT < 0) {
                _log.info("- HWID Manager: PC connection limit =  unlimited  box per PC.");
            } else {
                _log.info("- HWID Manager: PC connection limit = " + HwidConfig.PROTECT_WINDOWS_COUNT + " box per PC.");
            }

            _instance = new Manager();
        }

        return _instance;
    }

    public static void removePlayer(String name) {
        _objects.remove(name);

    }

    public static int getCountByHWID(String HWID) {
        int result = 0;
        Iterator var3 = _objects.values().iterator();

        while (var3.hasNext()) {
            Manager.InfoSet object = (Manager.InfoSet) var3.next();
            if (object._HWID.equals(HWID)) {
                ++result;
            }
        }

        return result;
    }

    public void addPlayer(GameClient client) {
        HwidPlayer.getInstance().updateHWIDInfo(client);
        _objects.put(client.getPlayerName(), new InfoSet(this, client.getPlayerName(), client.getHWID()));
    }

    public static class InfoSet {
        public String _playerName = "";
        public long _lastGGSendTime;
        public long _lastGGRecvTime;
        public int _attempts;
        public String _HWID = "";

        public InfoSet(final Manager param1, String name, String HWID) {
            this._playerName = name;
            this._lastGGSendTime = System.currentTimeMillis();
            this._lastGGRecvTime = this._lastGGSendTime;
            this._attempts = 0;
            this._HWID = HWID;
        }
    }
}