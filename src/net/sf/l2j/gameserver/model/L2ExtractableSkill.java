package net.sf.l2j.gameserver.model;

import java.util.List;

public class L2ExtractableSkill {
    private final int _hash;

    private final List<L2ExtractableProductItem> _product;

    public L2ExtractableSkill(int hash, List<L2ExtractableProductItem> products) {
        this._hash = hash;
        this._product = products;
    }

    public int getSkillHash() {
        return this._hash;
    }

    public List<L2ExtractableProductItem> getProductItemsArray() {
        return this._product;
    }
}
