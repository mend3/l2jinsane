package net.sf.l2j.gameserver.network.serverpackets;

public class RestartResponse extends L2GameServerPacket {
    private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);

    private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);

    private final boolean _result;

    public RestartResponse(boolean result) {
        this._result = result;
    }

    public static RestartResponse valueOf(boolean result) {
        return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
    }

    protected final void writeImpl() {
        writeC(95);
        writeD(this._result ? 1 : 0);
    }
}
