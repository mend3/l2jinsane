package net.sf.l2j.gameserver.network.loginserverpackets;

public class InitLS extends LoginServerBasePacket {
    private final int _rev;

    private final byte[] _key;

    public InitLS(byte[] decrypt) {
        super(decrypt);
        this._rev = readD();
        int size = readD();
        this._key = readB(size);
    }

    public int getRevision() {
        return this._rev;
    }

    public byte[] getRSAKey() {
        return this._key;
    }
}
