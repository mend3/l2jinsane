package net.sf.l2j.commons.logging;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MasterFormatter extends Formatter {
    protected static final String SHIFT = "\tat ";

    protected static final String CRLF = "\r\n";

    protected static final String SPACE = "\t";

    protected static String getFormatedDate(long timestamp) {
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(timestamp);
    }

    public String format(LogRecord record) {
        return null;
    }
}
