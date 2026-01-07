package net.sf.l2j.gameserver.network.serverpackets;

public class ChairSit extends L2GameServerPacket {
    private final int _playerId;

    private final int _staticId;

    public ChairSit(int playerId, int staticId) {
        this._playerId = playerId;
        this._staticId = staticId;
    }

    protected final void writeImpl() {
        writeC(225);
        writeD(this._playerId);
        writeD(this._staticId);
    }
}
