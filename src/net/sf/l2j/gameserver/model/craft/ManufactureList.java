package net.sf.l2j.gameserver.model.craft;

import java.util.ArrayList;
import java.util.List;

public class ManufactureList {
    private final List<ManufactureItem> _list = new ArrayList<>();

    private boolean _confirmed;

    private String _storeName;

    public void setConfirmedTrade(boolean confirmed) {
        this._confirmed = confirmed;
    }

    public boolean hasConfirmed() {
        return this._confirmed;
    }

    public String getStoreName() {
        return this._storeName;
    }

    public void setStoreName(String storeName) {
        this._storeName = storeName;
    }

    public void add(ManufactureItem item) {
        this._list.add(item);
    }

    public List<ManufactureItem> getList() {
        return this._list;
    }
}
