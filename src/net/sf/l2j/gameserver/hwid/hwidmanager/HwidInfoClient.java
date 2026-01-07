package net.sf.l2j.gameserver.hwid.hwidmanager;

public class HwidInfoClient {
    private String _playerName = "";

    private String _loginName = "";

    private int _playerId = 0;

    private String _hwid = "";

    private int _revision = 0;

    public final String getPlayerName() {
        return this._playerName;
    }

    public void setPlayerName(String name) {
        this._playerName = name;
    }

    public int getPlayerId() {
        return this._playerId;
    }

    public void setPlayerId(int plId) {
        this._playerId = plId;
    }

    public final String getHWID() {
        return this._hwid;
    }

    public void setHWID(String hwid) {
        this._hwid = hwid;
    }

    public int getRevision() {
        return this._revision;
    }

    public void setRevision(int revision) {
        this._revision = revision;
    }

    public final String getLoginName() {
        return this._loginName;
    }

    public void setLoginName(String name) {
        this._loginName = name;
    }
}
