package net.sf.l2j.commons.config;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ExProperties extends Properties {
    public static final String defaultDelimiter = "[\\s,;]+";
    private static final CLogger LOGGER = new CLogger(ExProperties.class.getName());
    private static final long serialVersionUID = 1L;

    public void load(String fileName) throws IOException {
        load(new File(fileName));
    }

    public void load(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            load(is);
            is.close();
        } catch (Throwable throwable) {
            try {
                is.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
    }

    public boolean getProperty(String name, boolean defaultValue) {
        boolean val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null)
            val = Boolean.parseBoolean(value);
        return val;
    }

    public int getProperty(String name, int defaultValue) {
        int val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null)
            val = Integer.parseInt(value);
        return val;
    }

    public long getProperty(String name, long defaultValue) {
        long val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null)
            val = Long.parseLong(value);
        return val;
    }

    public double getProperty(String name, double defaultValue) {
        double val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null)
            val = Double.parseDouble(value);
        return val;
    }

    public String[] getProperty(String name, String[] defaultValue) {
        return getProperty(name, defaultValue, "[\\s,;]+");
    }

    public String[] getProperty(String name, String[] defaultValue, String delimiter) {
        String[] val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null)
            val = value.split(delimiter);
        return val;
    }

    public boolean[] getProperty(String name, boolean[] defaultValue) {
        return getProperty(name, defaultValue, "[\\s,;]+");
    }

    public boolean[] getProperty(String name, boolean[] defaultValue, String delimiter) {
        boolean[] val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null) {
            String[] values = value.split(delimiter);
            val = new boolean[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Boolean.parseBoolean(values[i]);
        }
        return val;
    }

    public int[] getProperty(String name, int[] defaultValue) {
        return getProperty(name, defaultValue, "[\\s,;]+");
    }

    public int[] getProperty(String name, int[] defaultValue, String delimiter) {
        int[] val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null) {
            String[] values = value.split(delimiter);
            val = new int[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Integer.parseInt(values[i]);
        }
        return val;
    }

    public long[] getProperty(String name, long[] defaultValue) {
        return getProperty(name, defaultValue, "[\\s,;]+");
    }

    public long[] getProperty(String name, long[] defaultValue, String delimiter) {
        long[] val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null) {
            String[] values = value.split(delimiter);
            val = new long[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Long.parseLong(values[i]);
        }
        return val;
    }

    public double[] getProperty(String name, double[] defaultValue) {
        return getProperty(name, defaultValue, "[\\s,;]+");
    }

    public double[] getProperty(String name, double[] defaultValue, String delimiter) {
        double[] val = defaultValue;
        String value;
        if ((value = getProperty(name)) != null) {
            String[] values = value.split(delimiter);
            val = new double[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Double.parseDouble(values[i]);
        }
        return val;
    }

    public final IntIntHolder[] parseIntIntList(String key, String defaultValue) {
        String[] propertySplit = getProperty(key, defaultValue).split(";");
        if (propertySplit.length == 0)
            return null;
        int i = 0;
        IntIntHolder[] result = new IntIntHolder[propertySplit.length];
        for (String value : propertySplit) {
            String[] valueSplit = value.split("-");
            if (valueSplit.length != 2) {
                LOGGER.warn("Error parsing entry '{}', it should be itemId-itemNumber.", key);
                return null;
            }
            try {
                result[i] = new IntIntHolder(Integer.parseInt(valueSplit[0]), Integer.parseInt(valueSplit[1]));
            } catch (Exception e) {
                LOGGER.error("Error parsing entry '{}', one of the value isn't a number.", e, key);
                return null;
            }
            i++;
        }
        return result;
    }
}
