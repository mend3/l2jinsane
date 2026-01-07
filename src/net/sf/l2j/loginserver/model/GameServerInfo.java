package net.sf.l2j.loginserver.model;

import net.sf.l2j.commons.network.StatusType;
import net.sf.l2j.loginserver.GameServerThread;
import net.sf.l2j.loginserver.network.LoginClient;

public class GameServerInfo {
    private final byte[] _hexId;
    private int _id;
    private boolean _isAuthed;

    private GameServerThread _gst;

    private StatusType _status;

    private String _hostName;

    private int _port;

    private boolean _isPvp;

    private boolean _isTestServer;

    private boolean _isShowingClock;

    private boolean _isShowingBrackets;

    private int _ageLimit;

    private int _maxPlayers;

    public GameServerInfo(int id, byte[] hexId, GameServerThread gst) {
        this._id = id;
        this._hexId = hexId;
        this._gst = gst;
        this._status = StatusType.DOWN;
    }

    public GameServerInfo(int id, byte[] hexId) {
        this(id, hexId, null);
    }

    public int getId() {
        return this._id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public byte[] getHexId() {
        return this._hexId;
    }

    public boolean isAuthed() {
        return this._isAuthed;
    }

    public void setAuthed(boolean isAuthed) {
        this._isAuthed = isAuthed;
    }

    public GameServerThread getGameServerThread() {
        return this._gst;
    }

    public void setGameServerThread(GameServerThread gst) {
        this._gst = gst;
    }

    public StatusType getStatus() {
        return this._status;
    }

    public void setStatus(StatusType status) {
        this._status = status;
    }

    public String getHostName() {
        return this._hostName;
    }

    public void setHostName(String hostName) {
        this._hostName = hostName;
    }

    public int getPort() {
        return this._port;
    }

    public void setPort(int port) {
        this._port = port;
    }

    public int getMaxPlayers() {
        return this._maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this._maxPlayers = maxPlayers;
    }

    public boolean isPvp() {
        return this._isPvp;
    }

    public void setPvp(boolean isPvp) {
        this._isPvp = isPvp;
    }

    public boolean isTestServer() {
        return this._isTestServer;
    }

    public void setTestServer(boolean isTestServer) {
        this._isTestServer = isTestServer;
    }

    public boolean isShowingClock() {
        return this._isShowingClock;
    }

    public void setShowingClock(boolean isShowingClock) {
        this._isShowingClock = isShowingClock;
    }

    public boolean isShowingBrackets() {
        return this._isShowingBrackets;
    }

    public void setShowingBrackets(boolean isShowingBrackets) {
        this._isShowingBrackets = isShowingBrackets;
    }

    public int getAgeLimit() {
        return this._ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this._ageLimit = ageLimit;
    }

    public void setDown() {
        setAuthed(false);
        setPort(0);
        setGameServerThread(null);
        setStatus(StatusType.DOWN);
    }

    public int getCurrentPlayerCount() {
        return (this._gst == null) ? 0 : this._gst.getPlayerCount();
    }

    public boolean canLogin(LoginClient client) {
        if (this._status == StatusType.DOWN)
            return false;
        if (this._status == StatusType.GM_ONLY || getCurrentPlayerCount() >= getMaxPlayers())
            return (client.getAccessLevel() > 0);
        return (client.getAccessLevel() >= 0);
    }
}
