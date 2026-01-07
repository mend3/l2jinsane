package enginemods.main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UtilProperties extends Properties {
    public static final String DEFAULT_DELIMITER = "[\\s,;]+";
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
}
