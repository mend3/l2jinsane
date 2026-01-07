package enginemods.main.util.builders.html;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlBuilder {
    protected static final Logger LOG = Logger.getLogger(HtmlBuilder.class.getName());
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

    public void append(Object... args) {
        for (Object o : args) {
            this._html.append(o);
        }
    }

    public String toString() {
        if (this._html.length() >= this._type.getMaxValue()) {
            LOG.log(Level.SEVERE, "Warning html is too long! -> " + this._html.length());
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