package enginemods.main.util.builders.html;

public class HtmlBuilder {
    private final StringBuilder _html;
    private final HtmlBuilder.HtmlType _type;

    public HtmlBuilder(HtmlBuilder.HtmlType type) {
        this._html = new StringBuilder(type.getMaxValue());
        this._type = type;
    }

    public HtmlBuilder() {
        this._html = new StringBuilder();
        this._type = HtmlBuilder.HtmlType.HTML_TYPE;
    }

    public void append(String string) {
        this._html.append(string);
    }

    public void append(Object... obj) {
        Object[] var2 = obj;
        int var3 = obj.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Object o = var2[var4];
            this._html.append(o);
        }

    }

    public String toString() {
        if (this._html.length() >= this._type.getMaxValue()) {
            System.out.println("Warning html is too long! -> " + this._html.length());
            return "<html><body><br>Html was too long.</body></html>";
        } else {
            return this._html.toString();
        }
    }

    public void clean() {
        this._html.delete(0, this._html.length());
    }

    public enum HtmlType {
        COMUNITY_TYPE(12270),
        HTML_TYPE(8191);

        final int _length;

        HtmlType(int length) {
            this._length = length;
        }


        public int getMaxValue() {
            return this._length;
        }
    }
}