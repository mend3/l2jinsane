package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;

public class L2ExtractableProductItem {
    private final List<IntIntHolder> _items;

    private final double _chance;

    public L2ExtractableProductItem(List<IntIntHolder> items, double chance) {
        this._items = items;
        this._chance = chance;
    }

    public List<IntIntHolder> getItems() {
        return this._items;
    }

    public double getChance() {
        return this._chance;
    }
}
