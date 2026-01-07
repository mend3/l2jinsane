package net.sf.l2j.commons.logging.formatter;

import net.sf.l2j.commons.logging.MasterFormatter;

import java.util.logging.LogRecord;

public class FileLogFormatter extends MasterFormatter {
    public String format(LogRecord record) {
        return "[" + getFormatedDate(record.getMillis()) + "]\t" + record.getLevel().getName() + "\t" + record.getMessage() + "\r\n";
    }
}
