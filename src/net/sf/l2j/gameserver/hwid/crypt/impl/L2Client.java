package net.sf.l2j.gameserver.hwid.crypt.impl;

import net.sf.l2j.gameserver.hwid.crypt.ProtectionCrypt;

public class L2Client implements ProtectionCrypt {
    private final byte[] _key = new byte[16];
    private ProtectionCrypt _client;
    private byte[] _iv = null;

    public void setup(byte[] key, byte[] iv) {
        System.arraycopy(key, 0, this._key, 0, 16);
        this._iv = iv;
    }

    public void crypt(byte[] raw, int offset, int size) {
        if (this._iv != null) {
            this._client = new VMPC();
            this._client.setup(this._key, this._iv);
            this._client.crypt(raw, offset, size);
        }
        int temp = 0, temp2 = 0;
        for (int i = 0; i < size; i++) {
            temp2 = raw[offset + i] & 0xFF;
            raw[offset + i] = (byte) (temp2 ^ this._key[i & 0xF] ^ temp);
            temp = temp2;
        }
        int old = this._key[8] & 0xFF;
        old |= this._key[9] << 8 & 0xFF00;
        old |= this._key[10] << 16 & 0xFF0000;
        old |= this._key[11] << 24 & 0xFF000000;
        old += size;
        this._key[8] = (byte) (old & 0xFF);
        this._key[9] = (byte) (old >> 8 & 0xFF);
        this._key[10] = (byte) (old >> 16 & 0xFF);
        this._key[11] = (byte) (old >> 24 & 0xFF);
    }
}
