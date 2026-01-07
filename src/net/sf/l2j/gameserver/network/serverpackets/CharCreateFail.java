package net.sf.l2j.gameserver.network.serverpackets;

public class CharCreateFail extends L2GameServerPacket {
    public static final CharCreateFail REASON_CREATION_FAILED = new CharCreateFail(0);

    public static final CharCreateFail REASON_TOO_MANY_CHARACTERS = new CharCreateFail(1);

    public static final CharCreateFail REASON_NAME_ALREADY_EXISTS = new CharCreateFail(2);

    public static final CharCreateFail REASON_16_ENG_CHARS = new CharCreateFail(3);

    public static final CharCreateFail REASON_INCORRECT_NAME = new CharCreateFail(4);

    public static final CharCreateFail REASON_CREATE_NOT_ALLOWED = new CharCreateFail(5);

    public static final CharCreateFail REASON_CHOOSE_ANOTHER_SVR = new CharCreateFail(6);

    private final int _error;

    public CharCreateFail(int errorCode) {
        this._error = errorCode;
    }

    protected final void writeImpl() {
        writeC(26);
        writeD(this._error);
    }
}
