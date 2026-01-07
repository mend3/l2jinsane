package net.sf.l2j.gameserver.network.loginserverpackets;

public class PlayerAuthResponse extends LoginServerBasePacket {
    private final String _account;

    private final boolean _authed;

    public PlayerAuthResponse(byte[] decrypt) {
        super(decrypt);
        this._account = readS();
        this._authed = (readC() != 0);
    }

    public String getAccount() {
        return this._account;
    }

    public boolean isAuthed() {
        return this._authed;
    }
}
