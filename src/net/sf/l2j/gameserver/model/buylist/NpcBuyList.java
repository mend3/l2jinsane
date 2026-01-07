package net.sf.l2j.gameserver.model.buylist;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class NpcBuyList {
    private final Map<Integer, Product> _products = new LinkedHashMap<>();

    private final int _listId;

    private int _npcId;

    public NpcBuyList(int listId) {
        this._listId = listId;
    }

    public int getListId() {
        return this._listId;
    }

    public Collection<Product> getProducts() {
        return this._products.values();
    }

    public int getNpcId() {
        return this._npcId;
    }

    public void setNpcId(int id) {
        this._npcId = id;
    }

    public Product getProductByItemId(int itemId) {
        return this._products.get(Integer.valueOf(itemId));
    }

    public void addProduct(Product product) {
        this._products.put(Integer.valueOf(product.getItemId()), product);
    }

    public boolean isNpcAllowed(int npcId) {
        return (this._npcId == npcId);
    }
}
