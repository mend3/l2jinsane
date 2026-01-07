package net.sf.l2j.commons.util;

import java.util.Arrays;

public class ArraysUtil {
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    public static <T> boolean isEmpty(T[] array) {
        return (array == null || array.length == 0);
    }

    public static <T> boolean contains(T[] array, T obj) {
        if (array == null)
            return false;
        for (T element : array) {
            if (element.equals(obj))
                return true;
        }
        return false;
    }

    public static <T> boolean contains(T[] array1, T[] array2) {
        if (array1 == null || array1.length == 0)
            return false;
        if (array2 == null || array2.length == 0)
            return false;
        for (T element1 : array1) {
            for (T element2 : array2) {
                if (element2.equals(element1))
                    return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] array, int obj) {
        if (array == null)
            return false;
        for (int element : array) {
            if (element == obj)
                return true;
        }
        return false;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @SafeVarargs
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest)
            totalLength += array.length;
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
