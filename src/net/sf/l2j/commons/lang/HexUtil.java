package net.sf.l2j.commons.lang;

public class HexUtil {
    public static String printData(byte[] data, int len) {
        StringBuilder result = new StringBuilder();
        int counter = 0;
        for (int i = 0; i < len; i++) {
            if (counter % 16 == 0)
                result.append(fillHex(i, 4)).append(": ");
            result.append(fillHex(data[i] & 0xFF, 2)).append(" ");
            counter++;
            if (counter == 16) {
                result.append("   ");
                int charpoint = i - 15;
                for (int a = 0; a < 16; a++) {
                    int t1 = data[charpoint++];
                    if (t1 > 31 && t1 < 128) {
                        result.append((char) t1);
                    } else {
                        result.append('.');
                    }
                }
                result.append("\n");
                counter = 0;
            }
        }
        int rest = data.length % 16;
        if (rest > 0) {
            for (int j = 0; j < 17 - rest; j++)
                result.append("   ");
            int charpoint = data.length - rest;
            for (int a = 0; a < rest; a++) {
                int t1 = data[charpoint++];
                if (t1 > 31 && t1 < 128) {
                    result.append((char) t1);
                } else {
                    result.append('.');
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    public static String fillHex(int data, int digits) {
        StringBuilder number = new StringBuilder(Integer.toHexString(data));
        for (int i = number.length(); i < digits; i++)
            number.insert(0, "0");
        return number.toString();
    }

    public static String printData(byte[] raw) {
        return printData(raw, raw.length);
    }
}
