package net.sf.l2j.commons.logging.formatter;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.MasterFormatter;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.logging.LogRecord;

public class ItemLogFormatter extends MasterFormatter {
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        StringUtil.append(sb, "[", getFormatedDate(record.getMillis()), "] ", "\t", record.getMessage(), "\t");
        for (Object p : record.getParameters()) {
            if (p != null) {
                if (p instanceof ItemInstance item) {
                    StringUtil.append(sb, Integer.valueOf(item.getCount()), "\t");
                    if (item.getEnchantLevel() > 0)
                        StringUtil.append(sb, "+", Integer.valueOf(item.getEnchantLevel()), " ");
                    StringUtil.append(sb, item.getItem().getName(), "\t", Integer.valueOf(item.getObjectId()));
                } else {
                    sb.append(p);
                }
                sb.append("\t");
            }
        }
        sb.append("\r\n");
        return sb.toString();
    }
}
