package net.sf.l2j.gameserver.network.serverpackets;

public class PetDelete extends L2GameServerPacket {
    private final int _summonType;

    private final int _objId;

    public PetDelete(int summonType, int objId) {
        this._summonType = summonType;
        this._objId = objId;
    }

    protected final void writeImpl() {
        writeC(182);
        writeD(this._summonType);
        writeD(this._objId);
    }
}
