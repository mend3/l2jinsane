package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.items.TicketType;

import java.util.Arrays;

public final class MercenaryTicket {
    private final int _itemId;

    private final TicketType _type;

    private final boolean _isStationary;

    private final int _npcId;

    private final int _maxAmount;

    private final CabalType[] _ssq;

    public MercenaryTicket(StatSet set) {
        this._itemId = set.getInteger("itemId");
        this._type = set.getEnum("type", TicketType.class);
        this._isStationary = set.getBool("stationary");
        this._npcId = set.getInteger("npcId");
        this._maxAmount = set.getInteger("maxAmount");
        String[] ssq = set.getStringArray("ssq");
        this._ssq = new CabalType[ssq.length];
        for (int i = 0; i < ssq.length; i++)
            this._ssq[i] = Enum.valueOf(CabalType.class, ssq[i]);
    }

    public int getItemId() {
        return this._itemId;
    }

    public TicketType getType() {
        return this._type;
    }

    public boolean isStationary() {
        return this._isStationary;
    }

    public int getNpcId() {
        return this._npcId;
    }

    public int getMaxAmount() {
        return this._maxAmount;
    }

    public boolean isSsqType(CabalType type) {
        return Arrays.asList(this._ssq).contains(type);
    }
}
