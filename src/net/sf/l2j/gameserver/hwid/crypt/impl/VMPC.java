package net.sf.l2j.gameserver.hwid.crypt.impl;

import net.sf.l2j.gameserver.hwid.crypt.ProtectionCrypt;

public class VMPC implements ProtectionCrypt {
    private final byte[] _P = new byte[256];
    private byte _n = 0;
    private byte _s = 0;

    public void setup(byte[] key, byte[] iv) {
        this._s = 0;
        for (int i = 0; i < 256; i++)
            this._P[i] = (byte) (i & 0xFF);
        int m;
        for (m = 0; m < 768; m++) {
            this._s = this._P[this._s + this._P[m & 0xFF] + key[m % 64] & 0xFF];
            byte temp = this._P[m & 0xFF];
            this._P[m & 0xFF] = this._P[this._s & 0xFF];
            this._P[this._s & 0xFF] = temp;
        }
        for (m = 0; m < 768; m++) {
            this._s = this._P[this._s + this._P[m & 0xFF] + iv[m % 64] & 0xFF];
            byte temp = this._P[m & 0xFF];
            this._P[m & 0xFF] = this._P[this._s & 0xFF];
            this._P[this._s & 0xFF] = temp;
        }
        for (m = 0; m < 768; m++) {
            this._s = this._P[this._s + this._P[m & 0xFF] + key[m % 64] & 0xFF];
            byte temp = this._P[m & 0xFF];
            this._P[m & 0xFF] = this._P[this._s & 0xFF];
            this._P[this._s & 0xFF] = temp;
        }
        this._n = 0;
    }

    public void crypt(byte[] raw, int offset, int size) {
        for (int i = 0; i < size; i++) {
            this._s = this._P[this._s + this._P[this._n & 0xFF] & 0xFF];
            byte z = this._P[this._P[this._P[this._s & 0xFF] & 0xFF] + 1 & 0xFF];
            byte temp = this._P[this._n & 0xFF];
            this._P[this._n & 0xFF] = this._P[this._s & 0xFF];
            this._P[this._s & 0xFF] = temp;
            this._n = (byte) (this._n + 1 & 0xFF);
            raw[offset + i] = (byte) (raw[offset + i] ^ z);
        }
    }
}
