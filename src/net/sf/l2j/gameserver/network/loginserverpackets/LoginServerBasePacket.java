/**/
package net.sf.l2j.gameserver.network.loginserverpackets;

import java.nio.charset.StandardCharsets;

public abstract class LoginServerBasePacket {
    private final byte[] _decrypt;
    private int _off;

    public LoginServerBasePacket(byte[] decrypt) {
        this._decrypt = decrypt;
        this._off = 1;
    }

    public int readD() {
        int result = this._decrypt[this._off++] & 255;
        result |= this._decrypt[this._off++] << 8 & '\uff00';
        result |= this._decrypt[this._off++] << 16 & 16711680;
        result |= this._decrypt[this._off++] << 24 & -16777216;
        return result;
    }

    public int readC() {
        int result = this._decrypt[this._off++] & 255;
        return result;
    }

    public int readH() {
        int result = this._decrypt[this._off++] & 255;
        result |= this._decrypt[this._off++] << 8 & '\uff00';
        return result;
    }

    public double readF() {
        long result = this._decrypt[this._off++] & 255;
        result |= this._decrypt[this._off++] << 8 & '\uff00';
        result |= this._decrypt[this._off++] << 16 & 16711680;
        result |= this._decrypt[this._off++] << 24 & -16777216;
        result |= ((long) this._decrypt[this._off++] << 32) & 1095216660480L;
        result |= ((long) this._decrypt[this._off++] << 40) & 280375465082880L;
        result |= ((long) this._decrypt[this._off++] << 48) & 71776119061217280L;
        result |= ((long) this._decrypt[this._off++] << 56) & -72057594037927936L;
        return Double.longBitsToDouble(result);
    }

    public String readS() {
        String result = null;

        try {
            result = new String(this._decrypt, this._off, this._decrypt.length - this._off, StandardCharsets.UTF_16LE);
            result = result.substring(0, result.indexOf(0));
            this._off += result.length() * 2 + 2;
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return result;
    }

    public final byte[] readB(int length) {
        byte[] result = new byte[length];

        System.arraycopy(this._decrypt, this._off, result, 0, length);

        this._off += length;
        return result;
    }
}