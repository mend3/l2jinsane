package net.sf.l2j.gameserver.model.item.kind;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.ArmorType;

public final class Armor extends Item {
    private ArmorType _type;

    public Armor(StatSet set) {
        super(set);
        this._type = ArmorType.valueOf(set.getString("armor_type", "none").toUpperCase());
        int _bodyPart = getBodyPart();
        if (_bodyPart == 8 || _bodyPart == 65536 || _bodyPart == 262144 || _bodyPart == 524288 || (_bodyPart & 0x4) != 0 || (_bodyPart & 0x20) != 0 || (_bodyPart & 0x2000) != 0) {
            this._type1 = 0;
            this._type2 = 2;
        } else {
            if (this._type == ArmorType.NONE && getBodyPart() == 256)
                this._type = ArmorType.SHIELD;
            this._type1 = 1;
            this._type2 = 1;
        }
    }

    public ArmorType getItemType() {
        return this._type;
    }

    public int getItemMask() {
        return getItemType().mask();
    }
}
