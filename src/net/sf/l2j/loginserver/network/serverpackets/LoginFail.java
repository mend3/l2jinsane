package net.sf.l2j.loginserver.network.serverpackets;

public final class LoginFail extends L2LoginServerPacket {
    public static final LoginFail REASON_SYSTEM_ERROR = new LoginFail(1);

    public static final LoginFail REASON_PASS_WRONG = new LoginFail(2);

    public static final LoginFail REASON_USER_OR_PASS_WRONG = new LoginFail(3);

    public static final LoginFail REASON_ACCESS_FAILED = new LoginFail(4);

    public static final LoginFail REASON_ACCOUNT_IN_USE = new LoginFail(7);

    public static final LoginFail REASON_SERVER_OVERLOADED = new LoginFail(15);

    public static final LoginFail REASON_SERVER_MAINTENANCE = new LoginFail(16);

    public static final LoginFail REASON_TEMP_PASS_EXPIRED = new LoginFail(17);

    public static final LoginFail REASON_DUAL_BOX = new LoginFail(35);

    private final int _reason;

    private LoginFail(int reason) {
        this._reason = reason;
    }

    protected void write() {
        writeC(1);
        writeD(this._reason);
    }
}
