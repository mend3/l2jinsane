package net.sf.l2j.gameserver.network.serverpackets;

public class AutoAttackStop extends L2GameServerPacket {
    private final int _targetObjId;

    public AutoAttackStop(int targetObjId) {
        this._targetObjId = targetObjId;
    }

    protected final void writeImpl() {
        writeC(44);
        writeD(this._targetObjId);
    }
}
