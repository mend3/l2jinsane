package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class DailyReward {
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1000L, "K");
        suffixes.put(1000000L, "KK");
        suffixes.put(1000000000L, "KKK");
        suffixes.put(1000000000000L, "T");
        suffixes.put(1000000000000000L, "P");
        suffixes.put(1000000000000000000L, "E");
    }

    private int _day;
    private int _itemId;
    private int _amount;
    private int _enchantLevel;

    public DailyReward(int day, int itemId) {
        this._day = day;
        this._itemId = itemId;
    }

    public static String format(long value) {
        if (value == Long.MIN_VALUE)
            return format(-9223372036854775807L);
        if (value < 0L)
            return "-" + format(-value);
        if (value < 1000L)
            return Long.toString(value);
        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();
        long truncated = value / divideBy / 10L;
        boolean hasDecimal = (truncated < 100L && truncated / 10.0D != (truncated / 10L));
        return hasDecimal ? ("" + truncated / 10.0D + truncated / 10.0D) : ("" + truncated / 10L + truncated / 10L);
    }

    public String getIcon() {
        return getItem().getIcon();
    }

    public String getAmountTxt() {
        return format(getAmount());
    }

    public Item getItem() {
        return ItemTable.getInstance().getTemplate(this._itemId);
    }

    public int getItemId() {
        return this._itemId;
    }

    public void setItemId(int itemId) {
        this._itemId = itemId;
    }

    public int getAmount() {
        return this._amount;
    }

    public void setAmount(int amount) {
        this._amount = amount;
    }

    public int getEnchantLevel() {
        return this._enchantLevel;
    }

    public void setEnchantLevel(int enchantLevel) {
        this._enchantLevel = enchantLevel;
    }

    public int getDay() {
        return this._day;
    }

    public void setDay(int day) {
        this._day = day;
    }
}
