package net.sf.l2j.commons.lang;

public final class StringReplacer {
    private static final String DELIM_STR = "{}";

    private final StringBuilder _sb;

    public StringReplacer(String source) {
        this._sb = new StringBuilder(source);
    }

    public void replaceAll(String pattern, String replacement) {
        int point;
        while ((point = this._sb.indexOf(pattern)) != -1)
            this._sb.replace(point, point + pattern.length(), replacement);
    }

    public void replaceAll(Object... args) {
        int newIndex = 0;
        for (Object obj : args) {
            int index = this._sb.indexOf("{}", newIndex);
            if (index == -1)
                break;
            newIndex = index + 2;
            this._sb.replace(index, newIndex, (obj == null) ? null : obj.toString());
        }
    }

    public void replaceFirst(String pattern, String replacement) {
        int point = this._sb.indexOf(pattern);
        this._sb.replace(point, point + pattern.length(), replacement);
    }

    public void replaceLast(String pattern, String replacement) {
        int point = this._sb.lastIndexOf(pattern);
        this._sb.replace(point, point + pattern.length(), replacement);
    }

    public String toString() {
        return this._sb.toString();
    }
}
