package net.sf.l2j.commons.util;

public class SysUtil {
    private static final int MEBIOCTET = 1048576;

    public static long getUsedMemory() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory() / 1048576L;
    }
}
