package net.sf.l2j.gameserver.model.item.kind;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.EtcItemType;

public final class EtcItem extends Item {
    private final String _handler;
    private final int _sharedReuseGroup;
    private final int _reuseDelay;
    private EtcItemType _type;

    public EtcItem(StatSet set) {
        super(set);
        this._type = EtcItemType.valueOf(set.getString("etcitem_type", "none").toUpperCase());
        switch (this.getDefaultAction()) {
            case soulshot:
            case summon_soulshot:
            case summon_spiritshot:
            case spiritshot:
                this._type = EtcItemType.SHOT;
        }

        this._type1 = 4;
        this._type2 = 5;
        if (this.isQuestItem()) {
            this._type2 = 3;
        } else if (this.getItemId() == 57 || this.getItemId() == 5575) {
            this._type2 = 4;
        }

        this._handler = set.getString("handler", null);
        this._sharedReuseGroup = set.getInteger("shared_reuse_group", -1);
        this._reuseDelay = set.getInteger("reuse_delay", 0);
    }

    public EtcItemType getItemType() {
        return this._type;
    }

    public final boolean isConsumable() {
        return this.getItemType() == EtcItemType.SHOT || this.getItemType() == EtcItemType.POTION;
    }

    public int getItemMask() {
        return this.getItemType().mask();
    }

    public String getHandlerName() {
        return this._handler;
    }

    public int getSharedReuseGroup() {
        return this._sharedReuseGroup;
    }

    public int getReuseDelay() {
        return this._reuseDelay;
    }
}
