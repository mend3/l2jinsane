package net.sf.l2j.commons.data;

import java.util.concurrent.ConcurrentHashMap;

public abstract class MemoSet extends ConcurrentHashMap<String, String> {
    private static final long serialVersionUID = 1L;

    public MemoSet() {
    }

    public MemoSet(int size) {
        super(size);
    }

    protected abstract void onSet(String paramString1, String paramString2);

    protected abstract void onUnset(String paramString);

    public final void set(String key, String value) {
        onSet(key, value);
        put(key, value);
    }

    public void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    public void set(String key, long value) {
        set(key, String.valueOf(value));
    }

    public void set(String key, double value) {
        set(key, String.valueOf(value));
    }

    public void set(String key, Enum<?> value) {
        set(key, String.valueOf(value));
    }

    public final void unset(String key) {
        onUnset(key);
        remove(key);
    }

    public boolean getBool(String key) {
        String val = get(key);
        if (val != null)
            return Boolean.parseBoolean(val);
        throw new IllegalArgumentException("MemoSet : Boolean value required, but found: " + val + " for key: " + key + ".");
    }

    public boolean getBool(String key, boolean defaultValue) {
        String val = get(key);
        if (val != null)
            return Boolean.parseBoolean(val);
        return defaultValue;
    }

    public int getInteger(String key) {
        String val = get(key);
        if (val != null)
            return Integer.parseInt(val);
        throw new IllegalArgumentException("MemoSet : Integer value required, but found: " + val + " for key: " + key + ".");
    }

    public int getInteger(String key, int defaultValue) {
        String val = get(key);
        if (val != null)
            return Integer.parseInt(val);
        return defaultValue;
    }

    public long getLong(String key) {
        String val = get(key);
        if (val != null)
            return Long.parseLong(val);
        throw new IllegalArgumentException("MemoSet : Long value required, but found: " + val + " for key: " + key + ".");
    }

    public long getLong(String key, long defaultValue) {
        String val = get(key);
        if (val != null)
            return Long.parseLong(val);
        return defaultValue;
    }

    public double getDouble(String key) {
        String val = get(key);
        if (val != null)
            return Double.parseDouble(val);
        throw new IllegalArgumentException("MemoSet : Double value required, but found: " + val + " for key: " + key + ".");
    }

    public double getDouble(String key, double defaultValue) {
        String val = get(key);
        if (val != null)
            return Double.parseDouble(val);
        return defaultValue;
    }

    public <E extends Enum<E>> E getEnum(String name, Class<E> enumClass) {
        String val = get(name);
        if (val != null)
            return Enum.valueOf(enumClass, val);
        throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val + ".");
    }

    public <E extends Enum<E>> E getEnum(String name, Class<E> enumClass, E defaultValue) {
        String val = get(name);
        if (val != null)
            return Enum.valueOf(enumClass, val);
        return defaultValue;
    }
}
