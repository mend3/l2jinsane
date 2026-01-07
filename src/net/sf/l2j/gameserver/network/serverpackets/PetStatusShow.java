package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Summon;

public class PetStatusShow extends L2GameServerPacket {
    private final int _summonType;

    public PetStatusShow(Summon summon) {
        this._summonType = summon.getSummonType();
    }

    protected final void writeImpl() {
        writeC(176);
        writeD(this._summonType);
    }
}
