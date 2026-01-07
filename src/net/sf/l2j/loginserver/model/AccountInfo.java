package net.sf.l2j.loginserver.model;

import java.util.Objects;

public final class AccountInfo {
    private final String _login;

    private final String _passHash;

    private final int _accessLevel;

    private final int _lastServer;

    public AccountInfo(String login, String passHash, int accessLevel, int lastServer) {
        Objects.requireNonNull(login, "login");
        Objects.requireNonNull(passHash, "passHash");
        if (login.isEmpty())
            throw new IllegalArgumentException("login");
        if (passHash.isEmpty())
            throw new IllegalArgumentException("passHash");
        this._login = login.toLowerCase();
        this._passHash = passHash;
        this._accessLevel = accessLevel;
        this._lastServer = lastServer;
    }

    public boolean checkPassHash(String passHash) {
        return this._passHash.equals(passHash);
    }

    public String getLogin() {
        return this._login;
    }

    public int getAccessLevel() {
        return this._accessLevel;
    }

    public int getLastServer() {
        return this._lastServer;
    }
}
