package net.sf.l2j.gameserver.model.item;

public class DropData {
    public static final int MAX_CHANCE = 1000000;
    private int _itemId;
    private int _minDrop;
    private int _maxDrop;
    private int _chance;

    public int getItemId() {
        return this._itemId;
    }

    public void setItemId(int itemId) {
        this._itemId = itemId;
    }

    public int getMinDrop() {
        return this._minDrop;
    }

    public void setMinDrop(int mindrop) {
        this._minDrop = mindrop;
    }

    public int getMaxDrop() {
        return this._maxDrop;
    }

    public void setMaxDrop(int maxdrop) {
        this._maxDrop = maxdrop;
    }

    public int getChance() {
        return this._chance;
    }

    public void setChance(int chance) {
        this._chance = chance;
    }

    public String toString() {
        return "ItemID: " + this._itemId + " Min: " + this._minDrop + " Max: " + this._maxDrop + " Chance: " + (double) this._chance / (double) 10000.0F + "%";
    }

    public boolean equals(Object o) {
        if (o instanceof DropData drop) {
            return drop.getItemId() == this.getItemId();
        } else {
            return false;
        }
    }
}
