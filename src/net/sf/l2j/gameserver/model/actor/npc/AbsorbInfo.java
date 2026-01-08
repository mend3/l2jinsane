package net.sf.l2j.gameserver.model.actor.npc;

public final class AbsorbInfo {
    private boolean _registered;
    private int _itemId;
    private int _absorbedHpPercent;

    public AbsorbInfo(int itemId) {
        this._itemId = itemId;
    }

    public boolean isRegistered() {
        return this._registered;
    }

    public void setRegistered(boolean state) {
        this._registered = state;
    }

    public int getItemId() {
        return this._itemId;
    }

    public void setItemId(int itemId) {
        this._itemId = itemId;
    }

    public void setAbsorbedHpPercent(int percent) {
        this._absorbedHpPercent = percent;
    }

    public boolean isValid(int itemId) {
        return this._itemId == itemId && this._absorbedHpPercent < 50;
    }
}
