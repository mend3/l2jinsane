package net.sf.l2j.loginserver.model;

import net.sf.l2j.commons.network.StatusType;

public class ServerData {
    private final StatusType _status;

    private final String _hostName;

    private final int _serverId;

    private final int _port;

    private final int _currentPlayers;

    private final int _maxPlayers;

    private final int _ageLimit;

    private final boolean _isPvp;

    private final boolean _isTestServer;

    private final boolean _isShowingBrackets;

    private final boolean _isShowingClock;

    public ServerData(StatusType status, String hostName, GameServerInfo gsi) {
        this._status = status;
        this._hostName = hostName;
        this._serverId = gsi.getId();
        this._port = gsi.getPort();
        this._currentPlayers = gsi.getCurrentPlayerCount();
        this._maxPlayers = gsi.getMaxPlayers();
        this._ageLimit = gsi.getAgeLimit();
        this._isPvp = gsi.isPvp();
        this._isTestServer = gsi.isTestServer();
        this._isShowingBrackets = gsi.isShowingBrackets();
        this._isShowingClock = gsi.isShowingClock();
    }

    public StatusType getStatus() {
        return this._status;
    }

    public String getHostName() {
        return this._hostName;
    }

    public int getServerId() {
        return this._serverId;
    }

    public int getPort() {
        return this._port;
    }

    public int getCurrentPlayers() {
        return this._currentPlayers;
    }

    public int getMaxPlayers() {
        return this._maxPlayers;
    }

    public int getAgeLimit() {
        return this._ageLimit;
    }

    public boolean isPvp() {
        return this._isPvp;
    }

    public boolean isTestServer() {
        return this._isTestServer;
    }

    public boolean isShowingBrackets() {
        return this._isShowingBrackets;
    }

    public boolean isShowingClock() {
        return this._isShowingClock;
    }
}
