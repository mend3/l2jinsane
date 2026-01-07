package net.sf.l2j.gameserver.model.holder.skillnode;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

public class EnchantSkillNode extends IntIntHolder {
    private final int _exp;

    private final int _sp;

    private final int[] _enchantRates = new int[5];

    private IntIntHolder _item;

    public EnchantSkillNode(StatSet set) {
        super(set.getInteger("id"), set.getInteger("lvl"));
        this._exp = set.getInteger("exp");
        this._sp = set.getInteger("sp");
        this._enchantRates[0] = set.getInteger("rate76");
        this._enchantRates[1] = set.getInteger("rate77");
        this._enchantRates[2] = set.getInteger("rate78");
        this._enchantRates[3] = set.getInteger("rate79");
        this._enchantRates[4] = set.getInteger("rate80");
        if (set.containsKey("itemNeeded"))
            this._item = set.getIntIntHolder("itemNeeded");
    }

    public int getExp() {
        return this._exp;
    }

    public int getSp() {
        return this._sp;
    }

    public int getEnchantRate(int level) {
        return this._enchantRates[level - 76];
    }

    public IntIntHolder getItem() {
        return this._item;
    }
}
