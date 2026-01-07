package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket {
    private final int _isRemoved;

    public ExDuelEnemyRelation(boolean isRemoved) {
        this._isRemoved = isRemoved ? 1 : 0;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(88);
        writeD(this._isRemoved);
    }
}
