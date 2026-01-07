package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;

import java.util.ArrayList;
import java.util.List;

public class DropCategory {
    private final List<DropData> _drops;
    private final int _categoryType;
    private int _categoryChance;
    private int _categoryBalancedChance;

    public DropCategory(int categoryType) {
        this._categoryType = categoryType;
        this._drops = new ArrayList<>(0);
        this._categoryChance = 0;
        this._categoryBalancedChance = 0;
    }

    public void addDropData(DropData drop, boolean raid) {
        this._drops.add(drop);
        this._categoryChance += drop.getChance();
        this._categoryBalancedChance = (int) (this._categoryBalancedChance + Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), 1000000.0D));
    }

    public List<DropData> getAllDrops() {
        return this._drops;
    }

    public void clearAllDrops() {
        this._drops.clear();
    }

    public boolean isSweep() {
        return (getCategoryType() == -1);
    }

    public int getCategoryChance() {
        if (getCategoryType() >= 0)
            return this._categoryChance;
        return 1000000;
    }

    public int getCategoryBalancedChance() {
        if (getCategoryType() >= 0)
            return this._categoryBalancedChance;
        return 1000000;
    }

    public int getCategoryType() {
        return this._categoryType;
    }

    public synchronized DropData dropSeedAllowedDropsOnly() {
        List<DropData> drops = new ArrayList<>();
        int subCatChance = 0;
        for (DropData drop : getAllDrops()) {
            if (drop.getItemId() == 57 || drop.getItemId() == 6360 || drop.getItemId() == 6361 || drop.getItemId() == 6362) {
                drops.add(drop);
                subCatChance += drop.getChance();
            }
        }
        if (subCatChance == 0)
            return null;
        int randomIndex = Rnd.get(subCatChance);
        int sum = 0;
        for (DropData drop : drops) {
            sum += drop.getChance();
            if (sum > randomIndex)
                return drop;
        }
        return null;
    }

    public synchronized DropData dropOne(boolean raid) {
        int randomIndex = Rnd.get(getCategoryBalancedChance());
        int sum = 0;
        for (DropData drop : getAllDrops()) {
            sum = (int) (sum + Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), 1000000.0D));
            if (sum >= randomIndex)
                return drop;
        }
        return null;
    }
}
