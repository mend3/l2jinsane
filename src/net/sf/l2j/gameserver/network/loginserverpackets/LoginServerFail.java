package net.sf.l2j.gameserver.network.loginserverpackets;

public class LoginServerFail extends LoginServerBasePacket {
    private static final String[] REASONS = new String[]{"None", "Reason: ip banned", "Reason: ip reserved", "Reason: wrong hexid", "Reason: id reserved", "Reason: no free ID", "Not authed", "Reason: already logged in"};

    private final int _reason;

    public LoginServerFail(byte[] decrypt) {
        super(decrypt);
        this._reason = readC();
    }

    public String getReasonString() {
        return REASONS[this._reason];
    }

    public int getReason() {
        return this._reason;
    }
}
