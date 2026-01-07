package net.sf.l2j.gameserver.network.serverpackets;

public class CharDeleteFail extends L2GameServerPacket {
    public static final CharDeleteFail REASON_DELETION_FAILED = new CharDeleteFail(1);

    public static final CharDeleteFail REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = new CharDeleteFail(2);

    public static final CharDeleteFail REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = new CharDeleteFail(3);

    private final int _error;

    public CharDeleteFail(int errorCode) {
        this._error = errorCode;
    }

    protected final void writeImpl() {
        writeC(36);
        writeD(this._error);
    }
}
