package net.sf.l2j.commons.logging.formatter;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.MasterFormatter;

import java.util.logging.LogRecord;

public class ChatLogFormatter extends MasterFormatter {
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        StringUtil.append(sb, "[", getFormatedDate(record.getMillis()), "] ");
        for (Object p : record.getParameters()) {
            if (p != null)
                StringUtil.append(sb, p, " ");
        }
        StringUtil.append(sb, record.getMessage(), "\r\n");
        return sb.toString();
    }
}
