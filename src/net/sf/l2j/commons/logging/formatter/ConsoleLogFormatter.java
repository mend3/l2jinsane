package net.sf.l2j.commons.logging.formatter;

import net.sf.l2j.commons.logging.MasterFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

public class ConsoleLogFormatter extends MasterFormatter {
    public String format(LogRecord record) {
        StringWriter sw = new StringWriter();
        sw.append(record.getMessage());
        sw.append("\r\n");
        Throwable throwable = record.getThrown();
        if (throwable != null)
            throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
