package net.sf.l2j.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket {
    private final int _mode;

    public CameraMode(int mode) {
        this._mode = mode;
    }

    public void writeImpl() {
        writeC(241);
        writeD(this._mode);
    }
}
