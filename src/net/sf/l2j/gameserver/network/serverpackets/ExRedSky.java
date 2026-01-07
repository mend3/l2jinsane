package net.sf.l2j.gameserver.network.serverpackets;

public class ExRedSky extends L2GameServerPacket {
    private final int _duration;

    public ExRedSky(int duration) {
        this._duration = duration;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(64);
        writeD(this._duration);
    }
}
