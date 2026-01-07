package net.sf.l2j.gameserver.network.serverpackets;

public final class KeyPacket extends L2GameServerPacket {
    private final byte[] _key;

    public KeyPacket(byte[] key) {
        this._key = key;
    }

    public void writeImpl() {
        writeC(0);
        writeC(1);
        writeB(this._key);
        writeD(1);
        writeD(1);
    }
}
