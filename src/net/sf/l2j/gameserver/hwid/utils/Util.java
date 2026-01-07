package net.sf.l2j.gameserver.hwid.utils;

public class Util {
    public static void intToBytes(int value, byte[] array, int offset) {
        array[offset++] = (byte) (value & 0xFF);
        array[offset++] = (byte) (value >> 8 & 0xFF);
        array[offset++] = (byte) (value >> 16 & 0xFF);
        array[offset++] = (byte) (value >> 24 & 0xFF);
    }

    public static String LastErrorConvertion(Integer LastError) {
        return LastError.toString();
    }

    public static String asHex(byte[] raw) {
        return asHex(raw, 0, raw.length);
    }

    public static String asHex(byte[] raw, int offset, int size) {
        StringBuffer strbuf = new StringBuffer(raw.length * 2);
        for (int i = 0; i < size; i++) {
            if ((raw[offset + i] & 0xFF) < 16)
                strbuf.append("0");
            strbuf.append(Long.toString((raw[offset + i] & 0xFF), 16));
        }
        return strbuf.toString();
    }

    public static int bytesToInt(byte[] array, int offset) {
        return array[offset++] & 0xFF | (array[offset++] & 0xFF) << 8 | (array[offset++] & 0xFF) << 16 | (array[offset++] & 0xFF) << 24;
    }

    public static String asHwidString(String hex) {
        byte[] buf = asByteArray(hex);
        return asHex(buf);
    }

    public static byte[] asByteArray(String hex) {
        byte[] buf = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int j = Integer.parseInt(hex.substring(i, i + 2), 16);
            buf[i / 2] = (byte) (j & 0xFF);
        }
        return buf;
    }

    public static boolean verifyChecksum(byte[] raw, int offset, int size) {
        if ((size & 0x3) == 0 && size > 4) {
            long chksum = 0L;
            int count = size - 4;
            for (int i1 = offset; i1 < count; i1 += 4)
                chksum ^= bytesToInt(raw, i1);
            long check = bytesToInt(raw, count);
            return (check == chksum);
        }
        return false;
    }
}
