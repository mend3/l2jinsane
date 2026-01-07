package net.sf.l2j.gameserver.hwid.crypt.impl;

import net.sf.l2j.gameserver.hwid.crypt.ProtectionCrypt;

public class L2Server implements ProtectionCrypt {
    private final byte[] _key = new byte[16];

    private byte[] _iv = null;

    private ProtectionCrypt _server;

    public void setup(byte[] key, byte[] iv) {
        System.arraycopy(key, 0, this._key, 0, 16);
        this._iv = iv;
    }

    public void crypt(byte[] raw, int offset, int size) {
        int temp = 0;
        for (int i = 0; i < size; i++) {
            int temp2 = raw[offset + i] & 0xFF;
            temp = temp2 ^ this._key[i & 0xF] ^ temp;
            raw[offset + i] = (byte) temp;
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
        if (this._iv != null) {
            this._server = new VMPC();
            this._server.setup(this._key, this._iv);
            this._server.crypt(raw, offset, size);
        }
    }
}
