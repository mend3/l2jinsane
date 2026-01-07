package net.sf.l2j.loginserver.network.serverpackets;

public final class PlayFail extends L2LoginServerPacket {
    public static final PlayFail REASON_SYSTEM_ERROR = new PlayFail(1);

    public static final PlayFail REASON_USER_OR_PASS_WRONG = new PlayFail(2);

    public static final PlayFail REASON3 = new PlayFail(3);

    public static final PlayFail REASON4 = new PlayFail(4);

    public static final PlayFail REASON_TOO_MANY_PLAYERS = new PlayFail(15);

    private final int _reason;

    private PlayFail(int reason) {
        this._reason = reason;
    }

    protected void write() {
        writeC(6);
        writeC(this._reason);
    }
}
