package net.sf.l2j.gameserver.network.loginserverpackets;

public class AuthResponse extends LoginServerBasePacket {
    private final int _serverId;

    private final String _serverName;

    public AuthResponse(byte[] decrypt) {
        super(decrypt);
        this._serverId = readC();
        this._serverName = readS();
    }

    public int getServerId() {
        return this._serverId;
    }

    public String getServerName() {
        return this._serverName;
    }
}
