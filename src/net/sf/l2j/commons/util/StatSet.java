package net.sf.l2j.commons.util;

import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class is used in order to have a set of couples (key,value).
 * <p>
 * Methods deployed are accessors to the set (add/get value from its key) and addition of a whole set in the current
 * one.
 *
 * @author mkizub
 */
public class StatSet extends HashMap<String, Object> {


    public void set(String key, Object value) {
        this.put(key, value);
    }

    public void set(String key, String value) {
        this.put(key, value);
    }

    public void set(String key, boolean value) {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public void set(String key, int value) {
        this.put(key, value);
    }

    public void set(String key, int[] value) {
        this.put(key, value);
    }

    public void set(String key, long value) {
        this.put(key, value);
    }

    public void set(String key, double value) {
        this.put(key, value);
    }

    public void set(String key, Enum<?> value) {
        this.put(key, value);
    }

    public void unset(String key) {
        this.remove(key);
    }

    public StatSet getSet() {
        return this;
    }

    /**
     * Add a set of couple values in the current set
     *
     * @param newSet : L2StatsSet pointing out the list of couples to add in the current set
     */
    public void add(StatSet newSet) {
        Map<String, Object> newMap = newSet.getSet();
        for (String key : newMap.keySet()) {
            Object value = newMap.get(key);
            this.put(key, value);
        }
    }

    /**
     * Return the boolean associated to the key put in parameter ("name")
     *
     * @param name : String designating the key in the set
     * @return boolean : value associated to the key
     */
    public boolean getBool(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Boolean value required, but not specified");
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        try {
            return Boolean.parseBoolean((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Boolean value required, but found: " + val);
        }
    }

    /**
     * Return the boolean associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : boolean designating the default value if value associated with the key is null
     * @return boolean : value of the key
     */
    public boolean getBool(String name, boolean deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        try {
            return Boolean.parseBoolean((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Boolean value required, but found: " + val);
        }
    }

    /**
     * Returns the int associated to the key put in parameter ("name"). If the value associated to the key is null, this
     * method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : byte designating the default value if value associated with the key is null
     * @return byte : value associated to the key
     */
    public byte getByte(String name, byte deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        try {
            return Byte.parseByte((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Byte value required, but found: " + val);
        }
    }

    /**
     * Returns the byte associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return byte : value associated to the key
     */
    public byte getByte(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Byte value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        try {
            return Byte.parseByte((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Byte value required, but found: " + val);
        }
    }

    /**
     * Returns the short associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : short designating the default value if value associated with the key is null
     * @return short : value associated to the key
     */
    public short getShort(String name, short deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).shortValue();
        }
        try {
            return Short.parseShort((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Short value required, but found: " + val);
        }
    }

    /**
     * Returns the short associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return short : value associated to the key
     */
    public short getShort(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Short value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).shortValue();
        }
        try {
            return Short.parseShort((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Short value required, but found: " + val);
        }
    }

    /**
     * Returns the int associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return int : value associated to the key
     */
    public int getInteger(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Integer value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Integer value required, but found: " + val);
        }
    }

    /**
     * Returns the int associated to the key put in parameter ("name"). If the value associated to the key is null, this
     * method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : int designating the default value if value associated with the key is null
     * @return int : value associated to the key
     */
    public int getInteger(String name, int deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Integer value required, but found: " + val);
        }
    }

    /**
     * Returns the int[] associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name : String designating the key in the set
     * @return int[] : value associated to the key
     */
    public int[] getIntegerArray(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Integer value required, but not specified");
        }
        if (val instanceof Number) {
            return new int[]{((Number) val).intValue()};
        }
        int c = 0;
        String[] vals = ((String) val).split(";");
        int[] result = new int[vals.length];
        for (String v : vals) {
            try {
                result[c] = Integer.parseInt(v);
                c++;
            } catch (Exception e) {
                throw new IllegalArgumentException("Integer value required, but found: " + val);
            }
        }
        return result;
    }

    /**
     * Returns the long associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return long : value associated to the key
     */
    public long getLong(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Integer value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Integer value required, but found: " + val);
        }
    }

    /**
     * Returns the long associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : long designating the default value if value associated with the key is null
     * @return long : value associated to the key
     */
    public long getLong(String name, long deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Integer value required, but found: " + val);
        }
    }

    /**
     * Returns the float associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return float : value associated to the key
     */
    public float getFloat(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Float value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        try {
            return (float) Double.parseDouble((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: " + val);
        }
    }

    /**
     * Returns the float associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : float designating the default value if value associated with the key is null
     * @return float : value associated to the key
     */
    public float getFloat(String name, float deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        try {
            return (float) Double.parseDouble((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: " + val);
        }
    }

    /**
     * Returns the double associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return double : value associated to the key
     */
    public double getDouble(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Float value required, but not specified");
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return Double.parseDouble((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: " + val);
        }
    }

    /**
     * Returns the double associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : float designating the default value if value associated with the key is null
     * @return double : value associated to the key
     */
    public double getDouble(String name, double deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return Double.parseDouble((String) val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: " + val);
        }
    }

    /**
     * Returns the String associated to the key put in parameter ("name").
     *
     * @param name : String designating the key in the set
     * @return String : value associated to the key
     */
    public String getString(String name) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("String value required, but not specified");
        }
        return String.valueOf(val);
    }

    /**
     * Returns the String associated to the key put in parameter ("name"). If the value associated to the key is null,
     * this method returns the value of the parameter deflt.
     *
     * @param name  : String designating the key in the set
     * @param deflt : String designating the default value if value associated with the key is null
     * @return String : value associated to the key
     */
    public String getString(String name, String deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        return String.valueOf(val);
    }

    /**
     * Returns an enumeration of &lt;T&gt; from the set
     *
     * @param <T>       : Class of the enumeration returned
     * @param name      : String designating the key in the set
     * @param enumClass : Class designating the class of the value associated with the key in the set
     * @return Enum<T>
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass) {
        Object val = this.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
        }
        if (enumClass.isInstance(val)) {
            return (T) val;
        }
        try {
            return Enum.valueOf(enumClass, String.valueOf(val));
        } catch (Exception e) {
            throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
        }
    }

    /**
     * Returns an enumeration of &lt;T&gt; from the set. If the enumeration is empty, the method returns the value of
     * the parameter "deflt".
     *
     * @param <T>       : Class of the enumeration returned
     * @param name      : String designating the key in the set
     * @param enumClass : Class designating the class of the value associated with the key in the set
     * @param deflt     : <T> designating the value by default
     * @return Enum<T>
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt) {
        Object val = this.get(name);
        if (val == null) {
            return deflt;
        }
        if (enumClass.isInstance(val)) {
            return (T) val;
        }
        try {
            return Enum.valueOf(enumClass, String.valueOf(val));
        } catch (Exception e) {
            throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
        }
    }


    public IntIntHolder getIntIntHolder(String key) {
        Object val = this.get(key);
        String[] toSplit;
        if (val instanceof String[]) {
            toSplit = (String[]) val;
            return new IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]));
        } else if (val instanceof String) {
            toSplit = ((String) val).split("-");
            return new IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]));
        } else {
            String var10002 = String.valueOf(val);
            throw new IllegalArgumentException("StatSet : int-int (IntIntHolder) required, but found: " + var10002 + " for key: " + key + ".");
        }
    }

    public IntIntHolder[] getIntIntHolderArray(String key) {
        Object val = this.get(key);
        int var8;
        if (val instanceof String[] toSplit) {
            IntIntHolder[] tempArray = new IntIntHolder[toSplit.length];
            int index = 0;
            String[] var15 = toSplit;
            int var16 = toSplit.length;

            for (var8 = 0; var8 < var16; ++var8) {
                String splitted = var15[var8];
                String[] splittedHolder = splitted.split("-");
                tempArray[index++] = new IntIntHolder(Integer.parseInt(splittedHolder[0]), Integer.parseInt(splittedHolder[1]));
            }

            return tempArray;
        } else if (!(val instanceof String string)) {
            String var10002 = String.valueOf(val);
            throw new IllegalArgumentException("StatSet : int-int;int-int (int[] IntIntHolder) required, but found: " + var10002 + " for key: " + key + ".");
        } else {
            if (string.isEmpty()) {
                return null;
            } else {
                String[] toSplit;
                IntIntHolder[] tempArray;
                if (!string.contains(";")) {
                    toSplit = string.split("-");
                    tempArray = new IntIntHolder[]{new IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]))};
                    return tempArray;
                } else {
                    toSplit = string.split(";");
                    tempArray = new IntIntHolder[toSplit.length];
                    int index = 0;
                    String[] var7 = toSplit;
                    var8 = toSplit.length;

                    for (int var9 = 0; var9 < var8; ++var9) {
                        String splitted = var7[var9];
                        String[] splittedHolder = splitted.split("-");
                        tempArray[index++] = new IntIntHolder(Integer.parseInt(splittedHolder[0]), Integer.parseInt(splittedHolder[1]));
                    }

                    return tempArray;
                }
            }
        }
    }

    public List<IntIntHolder> getIntIntHolderList(String key) {
        Object val = this.get(key);
        if (!(val instanceof String string)) {
            String var10002 = String.valueOf(val);
            throw new IllegalArgumentException("StatSet : int-int;int-int (List<IntIntHolder>) required, but found: " + var10002 + " for key: " + key + ".");
        } else {
            if (string.isEmpty()) {
                return Collections.emptyList();
            } else {
                String[] entries;
                if (!string.contains(";")) {
                    entries = string.split("-");
                    return List.of(new IntIntHolder(Integer.parseInt(entries[0]), Integer.parseInt(entries[1])));
                } else {
                    entries = string.split(";");
                    List<IntIntHolder> list = new ArrayList<>(entries.length);
                    String[] var6 = entries;
                    int var7 = entries.length;

                    for (int var8 = 0; var8 < var7; ++var8) {
                        String entry = var6[var8];
                        String[] toSplit = entry.split("-");
                        list.add(new IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1])));
                    }

                    return list;
                }
            }
        }
    }

    public double[] getDoubleArray(String key) {
        Object val = this.get(key);
        if (val instanceof double[]) {
            return (double[]) val;
        } else if (val instanceof Number) {
            return new double[]{((Number) val).doubleValue()};
        } else if (val instanceof String) {
            return Stream.of(((String) val).split(";")).mapToDouble(Double::parseDouble).toArray();
        } else {
            String var10002 = String.valueOf(val);
            throw new IllegalArgumentException("StatSet : Double array required, but found: " + var10002 + " for key: " + key + ".");
        }
    }

    public <A> A getObject(String key, Class<A> type) {
        Object val = this.get(key);
        return val != null && type.isAssignableFrom(val.getClass()) ? (A) val : null;
    }

    public <T, U> Map<T, U> getMap(String key) {
        Object val = this.get(key);
        return val == null ? Collections.emptyMap() : (Map) val;
    }

    public <T> List<T> getList(String key) {
        Object val = this.get(key);
        return val == null ? Collections.emptyList() : (List) val;
    }

    public List<IntIntHolder> getIntIntHolderList(String key, List<IntIntHolder> defaultHolder) {
        try {
            return this.getIntIntHolderList(key);
        } catch (IllegalArgumentException var4) {
            return defaultHolder;
        }
    }

    public String[] getStringArray(String key) {
        Object val = this.get(key);
        if (val instanceof String[]) {
            return (String[]) val;
        } else if (val instanceof String) {
            return ((String) val).split(";");
        } else {
            String var10002 = String.valueOf(val);
            throw new IllegalArgumentException("StatSet : String array required, but found: " + var10002 + " for key: " + key + ".");
        }
    }
}
