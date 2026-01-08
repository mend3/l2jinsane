package net.sf.l2j.commons.lang;

import net.sf.l2j.commons.logging.CLogger;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class StringUtil {
    public static final String DIGITS = "0123456789";

    public static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";

    public static final String UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String LETTERS_AND_DIGITS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final CLogger LOGGER = new CLogger(StringUtil.class.getName());

    public static boolean isEmpty(String... strings) {
        for (String str : strings) {
            if (str == null || str.isEmpty())
                return true;
        }
        return false;
    }

    public static void append(StringBuilder sb, Object... content) {
        for (Object obj : content)
            sb.append((obj == null) ? null : obj.toString());
    }

    public static boolean isDigit(String text) {
        if (text == null)
            return false;
        return text.matches("[0-9]+");
    }

    public static boolean isAlphaNumeric(String text) {
        if (text == null)
            return false;
        for (char chars : text.toCharArray()) {
            if (!Character.isLetterOrDigit(chars))
                return false;
        }
        return true;
    }

    public static String formatNumber(long value) {
        return NumberFormat.getInstance(Locale.ENGLISH).format(value);
    }

    public static String scrambleString(String string) {
        List<String> letters = Arrays.asList(string.split(""));
        Collections.shuffle(letters);
        StringBuilder sb = new StringBuilder(string.length());
        for (String c : letters)
            sb.append(c);
        return sb.toString();
    }

    public static boolean isValidString(String text, String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            pattern = Pattern.compile(".*");
        }
        Matcher regexp = pattern.matcher(text);
        return regexp.matches();
    }

    public static void printSection(String text) {
        StringBuilder sb = new StringBuilder(80);
        sb.append("-".repeat(Math.max(0, 73 - text.length())));
        append(sb, "=[ ", text, " ]");
        LOGGER.info(sb.toString());
    }

    public static String getTimeStamp(int time) {
        int hours = time / 3600;
        time %= 3600;
        int minutes = time / 60;
        time %= 60;
        String result = "";
        if (hours > 0)
            result = result + result + "h";
        if (minutes > 0)
            result = result + " " + result + "m";
        if (time > 0 || result.isEmpty())
            result = result + " " + result + "s";
        return result;
    }

    public static String getNameWithoutExtension(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0)
            fileName = fileName.substring(0, pos);
        return fileName;
    }

    /**
     * Capitalizes the first letter of a string, and returns the result.<br> (Based on ucfirst() function of PHP)
     *
     * @param str the string
     * @return String containing the modified string.
     */
    public static String capitalizeFirst(String str) {
        str = str.trim();

        if (!str.isEmpty() && Character.isLetter(str.charAt(0))) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        return str;
    }
}
