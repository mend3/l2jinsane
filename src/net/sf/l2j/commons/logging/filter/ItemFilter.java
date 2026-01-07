package net.sf.l2j.commons.logging.filter;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ItemFilter implements Filter {
    private static final String EXCLUDE_PROCESS = "Consume";

    private static final EtcItemType[] EXCLUDE_TYPE = new EtcItemType[]{EtcItemType.ARROW, EtcItemType.SHOT, EtcItemType.HERB};

    public boolean isLoggable(LogRecord record) {
        if (!record.getLoggerName().equals("item"))
            return false;
        String[] messageList = record.getMessage().split(":");
        if (messageList.length < 2 || !"Consume".contains(messageList[1]))
            return true;
        ItemInstance item = (ItemInstance) record.getParameters()[1];
        return !ArraysUtil.contains((Object[]) EXCLUDE_TYPE, item.getItemType());
    }
}
