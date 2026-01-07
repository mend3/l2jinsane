package net.sf.l2j.loginserver.crypt;

import java.io.IOException;

public class NewCrypt {
    private final BlowfishEngine _crypt;

    private final BlowfishEngine _decrypt;

    public NewCrypt(byte[] blowfishKey) {
        this._crypt = new BlowfishEngine();
        this._crypt.init(true, blowfishKey);
        this._decrypt = new BlowfishEngine();
        this._decrypt.init(false, blowfishKey);
    }

    public NewCrypt(String key) {
        this(key.getBytes());
    }

    public static boolean verifyChecksum(byte[] raw) {
        return verifyChecksum(raw, 0, raw.length);
    }

    public static boolean verifyChecksum(byte[] raw, int offset, int size) {
        if ((size & 0x3) != 0 || size <= 4)
            return false;
        long chksum = 0L;
        int count = size - 4;
        long check = -1L;
        int i;
        for (i = offset; i < count; i += 4) {
            check = (raw[i] & 0xFF);
            check |= (raw[i + 1] << 8 & 0xFF00);
            check |= (raw[i + 2] << 16 & 0xFF0000);
            check |= (raw[i + 3] << 24 & 0xFF000000);
            chksum ^= check;
        }
        check = (raw[i] & 0xFF);
        check |= (raw[i + 1] << 8 & 0xFF00);
        check |= (raw[i + 2] << 16 & 0xFF0000);
        check |= (raw[i + 3] << 24 & 0xFF000000);
        return (check == chksum);
    }

    public static void appendChecksum(byte[] raw) {
        appendChecksum(raw, 0, raw.length);
    }

    public static void appendChecksum(byte[] raw, int offset, int size) {
        long chksum = 0L;
        int count = size - 4;
        int i;
        for (i = offset; i < count; i += 4) {
            long l = (raw[i] & 0xFF);
            l |= (raw[i + 1] << 8 & 0xFF00);
            l |= (raw[i + 2] << 16 & 0xFF0000);
            l |= (raw[i + 3] << 24 & 0xFF000000);
            chksum ^= l;
        }
        long ecx = (raw[i] & 0xFF);
        ecx |= (raw[i + 1] << 8 & 0xFF00);
        ecx |= (raw[i + 2] << 16 & 0xFF0000);
        ecx |= (raw[i + 3] << 24 & 0xFF000000);
        raw[i] = (byte) (int) (chksum & 0xFFL);
        raw[i + 1] = (byte) (int) (chksum >> 8L & 0xFFL);
        raw[i + 2] = (byte) (int) (chksum >> 16L & 0xFFL);
        raw[i + 3] = (byte) (int) (chksum >> 24L & 0xFFL);
    }

    public static void encXORPass(byte[] raw, int key) {
        encXORPass(raw, 0, raw.length, key);
    }

    public static void encXORPass(byte[] raw, int offset, int size, int key) {
        int stop = size - 8;
        int pos = 4 + offset;
        int ecx = key;
        while (pos < stop) {
            int edx = raw[pos] & 0xFF;
            edx |= (raw[pos + 1] & 0xFF) << 8;
            edx |= (raw[pos + 2] & 0xFF) << 16;
            edx |= (raw[pos + 3] & 0xFF) << 24;
            ecx += edx;
            edx ^= ecx;
            raw[pos++] = (byte) (edx & 0xFF);
            raw[pos++] = (byte) (edx >> 8 & 0xFF);
            raw[pos++] = (byte) (edx >> 16 & 0xFF);
            raw[pos++] = (byte) (edx >> 24 & 0xFF);
        }
        raw[pos++] = (byte) (ecx & 0xFF);
        raw[pos++] = (byte) (ecx >> 8 & 0xFF);
        raw[pos++] = (byte) (ecx >> 16 & 0xFF);
        raw[pos] = (byte) (ecx >> 24 & 0xFF);
    }

    public byte[] decrypt(byte[] raw) throws IOException {
        byte[] result = new byte[raw.length];
        int count = raw.length / 8;
        for (int i = 0; i < count; i++)
            this._decrypt.processBlock(raw, i * 8, result, i * 8);
        return result;
    }

    public void decrypt(byte[] raw, int offset, int size) throws IOException {
        byte[] result = new byte[size];
        int count = size / 8;
        for (int i = 0; i < count; i++)
            this._decrypt.processBlock(raw, offset + i * 8, result, i * 8);
        System.arraycopy(result, 0, raw, offset, size);
    }

    public byte[] crypt(byte[] raw) throws IOException {
        int count = raw.length / 8;
        byte[] result = new byte[raw.length];
        for (int i = 0; i < count; i++)
            this._crypt.processBlock(raw, i * 8, result, i * 8);
        return result;
    }

    public void crypt(byte[] raw, int offset, int size) throws IOException {
        int count = size / 8;
        byte[] result = new byte[size];
        for (int i = 0; i < count; i++)
            this._crypt.processBlock(raw, offset + i * 8, result, i * 8);
        System.arraycopy(result, 0, raw, offset, size);
    }
}
