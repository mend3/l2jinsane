package net.sf.l2j.gameserver.network;

public class GameCrypt {
    private final byte[] _inKey = new byte[16];

    private final byte[] _outKey = new byte[16];

    private boolean _isEnabled;

    public void setKey(byte[] key) {
        System.arraycopy(key, 0, this._inKey, 0, 16);
        System.arraycopy(key, 0, this._outKey, 0, 16);
    }

    public void decrypt(byte[] raw, int offset, int size) {
        if (!this._isEnabled)
            return;
        int temp = 0;
        for (int i = 0; i < size; i++) {
            int temp2 = raw[offset + i] & 0xFF;
            raw[offset + i] = (byte) (temp2 ^ this._inKey[i & 0xF] ^ temp);
            temp = temp2;
        }
        int old = this._inKey[8] & 0xFF;
        old |= this._inKey[9] << 8 & 0xFF00;
        old |= this._inKey[10] << 16 & 0xFF0000;
        old |= this._inKey[11] << 24 & 0xFF000000;
        old += size;
        this._inKey[8] = (byte) (old & 0xFF);
        this._inKey[9] = (byte) (old >> 8 & 0xFF);
        this._inKey[10] = (byte) (old >> 16 & 0xFF);
        this._inKey[11] = (byte) (old >> 24 & 0xFF);
    }

    public void encrypt(byte[] raw, int offset, int size) {
        if (!this._isEnabled) {
            this._isEnabled = true;
            return;
        }
        int temp = 0;
        for (int i = 0; i < size; i++) {
            int temp2 = raw[offset + i] & 0xFF;
            temp = temp2 ^ this._outKey[i & 0xF] ^ temp;
            raw[offset + i] = (byte) temp;
        }
        int old = this._outKey[8] & 0xFF;
        old |= this._outKey[9] << 8 & 0xFF00;
        old |= this._outKey[10] << 16 & 0xFF0000;
        old |= this._outKey[11] << 24 & 0xFF000000;
        old += size;
        this._outKey[8] = (byte) (old & 0xFF);
        this._outKey[9] = (byte) (old >> 8 & 0xFF);
        this._outKey[10] = (byte) (old >> 16 & 0xFF);
        this._outKey[11] = (byte) (old >> 24 & 0xFF);
    }
}
