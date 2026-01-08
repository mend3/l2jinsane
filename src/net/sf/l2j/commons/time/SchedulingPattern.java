/**/
package net.sf.l2j.commons.time;

import java.util.*;

public class SchedulingPattern {
    private static final int MINUTE_MIN_VALUE = 0;
    private static final int MINUTE_MAX_VALUE = 59;
    private static final int HOUR_MIN_VALUE = 0;
    private static final int HOUR_MAX_VALUE = 23;
    private static final int DAY_OF_MONTH_MIN_VALUE = 1;
    private static final int DAY_OF_MONTH_MAX_VALUE = 31;
    private static final int MONTH_MIN_VALUE = 1;
    private static final int MONTH_MAX_VALUE = 12;
    private static final int DAY_OF_WEEK_MIN_VALUE = 0;
    private static final int DAY_OF_WEEK_MAX_VALUE = 7;
    private static final SchedulingPattern.ValueParser MINUTE_VALUE_PARSER = new SchedulingPattern.MinuteValueParser();
    private static final SchedulingPattern.ValueParser HOUR_VALUE_PARSER = new SchedulingPattern.HourValueParser();
    private static final SchedulingPattern.ValueParser DAY_OF_MONTH_VALUE_PARSER = new SchedulingPattern.DayOfMonthValueParser();
    private static final SchedulingPattern.ValueParser MONTH_VALUE_PARSER = new SchedulingPattern.MonthValueParser();
    private static final SchedulingPattern.ValueParser DAY_OF_WEEK_VALUE_PARSER = new SchedulingPattern.DayOfWeekValueParser();
    protected final List<SchedulingPattern.ValueMatcher> minuteMatchers = new ArrayList<>();
    protected final List<SchedulingPattern.ValueMatcher> hourMatchers = new ArrayList<>();
    protected final List<SchedulingPattern.ValueMatcher> dayOfMonthMatchers = new ArrayList<>();
    protected final List<SchedulingPattern.ValueMatcher> monthMatchers = new ArrayList<>();
    protected final List<SchedulingPattern.ValueMatcher> dayOfWeekMatchers = new ArrayList<>();
    private final String asString;
    protected int matcherSize = 0;

    public SchedulingPattern(String pattern) throws SchedulingPattern.InvalidPatternException {
        this.asString = pattern;
        StringTokenizer st1 = new StringTokenizer(pattern, "|");
        if (st1.countTokens() < 1) {
            throw new InvalidPatternException("invalid pattern: \"" + pattern + "\"");
        } else {
            for (; st1.hasMoreTokens(); ++this.matcherSize) {
                String localPattern = st1.nextToken();
                StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
                if (st2.countTokens() != 5) {
                    throw new InvalidPatternException("invalid pattern: \"" + localPattern + "\"");
                }

                try {
                    this.minuteMatchers.add(buildValueMatcher(st2.nextToken(), MINUTE_VALUE_PARSER));
                } catch (Exception var10) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing minutes field: " + var10.getMessage() + ".");
                }

                try {
                    this.hourMatchers.add(buildValueMatcher(st2.nextToken(), HOUR_VALUE_PARSER));
                } catch (Exception var9) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing hours field: " + var9.getMessage() + ".");
                }

                try {
                    this.dayOfMonthMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_MONTH_VALUE_PARSER));
                } catch (Exception var8) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of month field: " + var8.getMessage() + ".");
                }

                try {
                    this.monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER));
                } catch (Exception var7) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing months field: " + var7.getMessage() + ".");
                }

                try {
                    this.dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
                } catch (Exception var6) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + var6.getMessage() + ".");
                }
            }

        }
    }

    public static boolean validate(String schedulingPattern) {
        try {
            new SchedulingPattern(schedulingPattern);
            return true;
        } catch (SchedulingPattern.InvalidPatternException var2) {
            return false;
        }
    }

    private static SchedulingPattern.ValueMatcher buildValueMatcher(String str, SchedulingPattern.ValueParser parser) throws Exception {
        if (str.equals("*")) {
            return new SchedulingPattern.AlwaysTrueValueMatcher();
        } else {
            List<Integer> values = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(str, ",");

            while (st.hasMoreTokens()) {
                String element = st.nextToken();

                List<Integer> local;
                try {
                    local = parseListElement(element, parser);
                } catch (Exception var8) {
                    throw new Exception("invalid field \"" + str + "\", invalid element \"" + element + "\", " + var8.getMessage());
                }

                for (Integer value : local) {
                    if (!values.contains(value)) {
                        values.add(value);
                    }
                }
            }

            if (values.isEmpty()) {
                throw new Exception("invalid field \"" + str + "\"");
            } else if (parser == DAY_OF_MONTH_VALUE_PARSER) {
                return new SchedulingPattern.DayOfMonthValueMatcher(values);
            } else {
                return new SchedulingPattern.IntArrayValueMatcher(values);
            }
        }
    }

    private static List<Integer> parseListElement(String str, SchedulingPattern.ValueParser parser) throws Exception {
        StringTokenizer st = new StringTokenizer(str, "/");
        int size = st.countTokens();
        if (size >= 1 && size <= 2) {
            List<Integer> values;
            try {
                values = parseRange(st.nextToken(), parser);
            } catch (Exception var10) {
                throw new Exception("invalid range, " + var10.getMessage());
            }

            if (size != 2) {
                return values;
            } else {
                String dStr = st.nextToken();

                int div;
                try {
                    div = Integer.parseInt(dStr);
                } catch (NumberFormatException var9) {
                    throw new Exception("invalid divisor \"" + dStr + "\"");
                }

                if (div < 1) {
                    throw new Exception("non positive divisor \"" + div + "\"");
                } else {
                    List<Integer> values2 = new ArrayList<>();

                    for (int i = 0; i < values.size(); i += div) {
                        values2.add(values.get(i));
                    }

                    return values2;
                }
            }
        } else {
            throw new Exception("syntax error");
        }
    }

    private static List<Integer> parseRange(String str, SchedulingPattern.ValueParser parser) throws Exception {
        int size;
        int v1;
        if (str.equals("*")) {
            int min = parser.getMinValue();
            size = parser.getMaxValue();
            List<Integer> values = new ArrayList<>();

            for (v1 = min; v1 <= size; ++v1) {
                values.add(v1);
            }

            return values;
        } else {
            StringTokenizer st = new StringTokenizer(str, "-");
            size = st.countTokens();
            if (size >= 1 && size <= 2) {
                String v1Str = st.nextToken();

                try {
                    v1 = parser.parse(v1Str);
                } catch (Exception var13) {
                    throw new Exception("invalid value \"" + v1Str + "\", " + var13.getMessage());
                }

                if (size == 1) {
                    List<Integer> values = new ArrayList<>();
                    values.add(v1);
                    return values;
                } else {
                    String v2Str = st.nextToken();

                    int v2;
                    try {
                        v2 = parser.parse(v2Str);
                    } catch (Exception var12) {
                        throw new Exception("invalid value \"" + v2Str + "\", " + var12.getMessage());
                    }

                    List<Integer> values = new ArrayList<>();
                    int min;
                    if (v1 < v2) {
                        for (min = v1; min <= v2; ++min) {
                            values.add(min);
                        }
                    } else if (v1 > v2) {
                        min = parser.getMinValue();
                        int max = parser.getMaxValue();

                        int i;
                        for (i = v1; i <= max; ++i) {
                            values.add(i);
                        }

                        for (i = min; i <= v2; ++i) {
                            values.add(i);
                        }
                    } else {
                        values.add(v1);
                    }

                    return values;
                }
            } else {
                throw new Exception("syntax error");
            }
        }
    }

    private static int parseAlias(String value, String[] aliases, int offset) throws Exception {
        for (int i = 0; i < aliases.length; ++i) {
            if (aliases[i].equalsIgnoreCase(value)) {
                return offset + i;
            }
        }

        throw new Exception("invalid alias \"" + value + "\"");
    }

    public boolean match(TimeZone timezone, long millis) {
        GregorianCalendar gc = new GregorianCalendar(timezone);
        gc.setTimeInMillis(millis);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        int minute = gc.get(Calendar.MINUTE);
        int hour = gc.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = gc.get(Calendar.DATE);
        int month = gc.get(Calendar.MONTH) + 1;
        int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
        int year = gc.get(Calendar.YEAR);

        for (int i = 0; i < this.matcherSize; ++i) {
            boolean var10000;
            label33:
            {
                SchedulingPattern.ValueMatcher minuteMatcher = this.minuteMatchers.get(i);
                SchedulingPattern.ValueMatcher hourMatcher = this.hourMatchers.get(i);
                SchedulingPattern.ValueMatcher dayOfMonthMatcher = this.dayOfMonthMatchers.get(i);
                SchedulingPattern.ValueMatcher monthMatcher = this.monthMatchers.get(i);
                SchedulingPattern.ValueMatcher dayOfWeekMatcher = this.dayOfWeekMatchers.get(i);
                if (minuteMatcher.match(minute) && hourMatcher.match(hour)) {
                    label30:
                    {
                        if (dayOfMonthMatcher instanceof SchedulingPattern.DayOfMonthValueMatcher) {
                            if (!((SchedulingPattern.DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, gc.isLeapYear(year))) {
                                break label30;
                            }
                        } else if (!dayOfMonthMatcher.match(dayOfMonth)) {
                            break label30;
                        }

                        if (monthMatcher.match(month) && dayOfWeekMatcher.match(dayOfWeek)) {
                            var10000 = true;
                            break label33;
                        }
                    }
                }

                var10000 = false;
            }

            boolean eval = var10000;
            if (eval) {
                return true;
            }
        }

        return false;
    }

    public boolean match(long millis) {
        return this.match(TimeZone.getDefault(), millis);
    }

    public long next(TimeZone timezone, long millis) {
        long next = -1L;

        label80:
        for (int i = 0; i < this.matcherSize; ++i) {
            GregorianCalendar gc = new GregorianCalendar(timezone);
            gc.setTimeInMillis(millis);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
            SchedulingPattern.ValueMatcher minuteMatcher = this.minuteMatchers.get(i);
            SchedulingPattern.ValueMatcher hourMatcher = this.hourMatchers.get(i);
            SchedulingPattern.ValueMatcher dayOfMonthMatcher = this.dayOfMonthMatchers.get(i);
            SchedulingPattern.ValueMatcher monthMatcher = this.monthMatchers.get(i);
            SchedulingPattern.ValueMatcher dayOfWeekMatcher = this.dayOfWeekMatchers.get(i);

            while (true) {
                int year = gc.get(Calendar.YEAR);
                boolean isLeapYear = gc.isLeapYear(year);

                for (int month = gc.get(Calendar.MONTH) + 1; month <= 12; ++month) {
                    if (monthMatcher.match(month)) {
                        gc.set(Calendar.MONTH, month - 1);
                        int maxDayOfMonth = SchedulingPattern.DayOfMonthValueMatcher.getLastDayOfMonth(month, isLeapYear);

                        for (int dayOfMonth = gc.get(Calendar.DATE); dayOfMonth <= maxDayOfMonth; ++dayOfMonth) {
                            label85:
                            {
                                if (dayOfMonthMatcher instanceof SchedulingPattern.DayOfMonthValueMatcher) {
                                    if (!((SchedulingPattern.DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, isLeapYear)) {
                                        break label85;
                                    }
                                } else if (!dayOfMonthMatcher.match(dayOfMonth)) {
                                    break label85;
                                }

                                gc.set(Calendar.DATE, dayOfMonth);
                                int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
                                if (dayOfWeekMatcher.match(dayOfWeek)) {
                                    for (int hour = gc.get(Calendar.HOUR_OF_DAY); hour <= 23; ++hour) {
                                        if (hourMatcher.match(hour)) {
                                            gc.set(Calendar.HOUR_OF_DAY, hour);

                                            for (int minute = gc.get(Calendar.MINUTE); minute <= 59; ++minute) {
                                                if (minuteMatcher.match(minute)) {
                                                    gc.set(Calendar.MINUTE, minute);
                                                    long next0 = gc.getTimeInMillis();
                                                    if (next == -1L || next0 < next) {
                                                        next = next0;
                                                    }
                                                    continue label80;
                                                }
                                            }
                                        }

                                        gc.set(Calendar.MINUTE, 0);
                                    }
                                }
                            }

                            gc.set(Calendar.HOUR_OF_DAY, 0);
                            gc.set(Calendar.MINUTE, 0);
                        }
                    }

                    gc.set(Calendar.DATE, 1);
                    gc.set(Calendar.HOUR_OF_DAY, 0);
                    gc.set(Calendar.MINUTE, 0);
                }

                gc.set(Calendar.MONTH, 0);
                gc.set(Calendar.HOUR_OF_DAY, 0);
                gc.set(Calendar.MINUTE, 0);
                gc.roll(Calendar.YEAR, true);
            }
        }

        return next;
    }

    public long next(long millis) {
        return this.next(TimeZone.getDefault(), millis);
    }

    public String toString() {
        return this.asString;
    }

    private interface ValueParser {
        int parse(String var1) throws Exception;

        int getMinValue();

        int getMaxValue();
    }

    public interface ValueMatcher {
        boolean match(int var1);
    }

    private static class AlwaysTrueValueMatcher implements SchedulingPattern.ValueMatcher {
        public boolean match(int value) {
            return true;
        }
    }

    private static class DayOfMonthValueMatcher extends SchedulingPattern.IntArrayValueMatcher {
        private static final int[] lastDays = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        public DayOfMonthValueMatcher(List<Integer> integers) {
            super(integers);
        }

        public static int getLastDayOfMonth(int month, boolean isLeapYear) {
            return isLeapYear && month == 2 ? 29 : lastDays[month - 1];
        }

        public static boolean isLastDayOfMonth(int value, int month, boolean isLeapYear) {
            return value == getLastDayOfMonth(month, isLeapYear);
        }

        public boolean match(int value, int month, boolean isLeapYear) {
            return super.match(value) || value > 27 && this.match(32) && isLastDayOfMonth(value, month, isLeapYear);
        }
    }

    private static class IntArrayValueMatcher implements SchedulingPattern.ValueMatcher {
        private final int[] values;

        public IntArrayValueMatcher(List<Integer> integers) {
            int size = integers.size();
            this.values = new int[size];

            for (int i = 0; i < size; ++i) {
                try {
                    this.values[i] = integers.get(i);
                } catch (Exception var5) {
                    throw new IllegalArgumentException(var5.getMessage());
                }
            }

        }

        public boolean match(int value) {
            for (int j : this.values) {
                if (j == value) {
                    return true;
                }
            }

            return false;
        }
    }

    private static class MinuteValueParser extends SchedulingPattern.SimpleValueParser {
        public MinuteValueParser() {
            super(0, 59);
        }
    }

    private static class HourValueParser extends SchedulingPattern.SimpleValueParser {
        public HourValueParser() {
            super(0, 23);
        }
    }

    private static class DayOfMonthValueParser extends SchedulingPattern.SimpleValueParser {
        public DayOfMonthValueParser() {
            super(1, 31);
        }

        public int parse(String value) throws Exception {
            return value.equalsIgnoreCase("L") ? 32 : super.parse(value);
        }
    }

    private static class MonthValueParser extends SchedulingPattern.SimpleValueParser {
        private static final String[] ALIASES = new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

        public MonthValueParser() {
            super(1, 12);
        }

        public int parse(String value) throws Exception {
            try {
                return super.parse(value);
            } catch (Exception var3) {
                return SchedulingPattern.parseAlias(value, ALIASES, 1);
            }
        }
    }

    private static class DayOfWeekValueParser extends SchedulingPattern.SimpleValueParser {
        private static final String[] ALIASES = new String[]{"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

        public DayOfWeekValueParser() {
            super(0, 7);
        }

        public int parse(String value) throws Exception {
            try {
                return super.parse(value) % 7;
            } catch (Exception var3) {
                return SchedulingPattern.parseAlias(value, ALIASES, 0);
            }
        }
    }

    private static class SimpleValueParser implements SchedulingPattern.ValueParser {
        protected final int minValue;
        protected final int maxValue;

        public SimpleValueParser(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public int parse(String value) throws Exception {
            int i;
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException var4) {
                throw new Exception("invalid integer value");
            }

            if (i >= this.minValue && i <= this.maxValue) {
                return i;
            } else {
                throw new Exception("value out of range");
            }
        }

        public int getMinValue() {
            return this.minValue;
        }

        public int getMaxValue() {
            return this.maxValue;
        }
    }

    public static class InvalidPatternException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InvalidPatternException() {
        }

        InvalidPatternException(String message) {
            super(message);
        }
    }
}