package net.sf.l2j.gameserver.network.serverpackets;

public class AutoAttackStart extends L2GameServerPacket {
    private final int _targetObjId;

    public AutoAttackStart(int targetId) {
        this._targetObjId = targetId;
    }

    protected final void writeImpl() {
        writeC(43);
        writeD(this._targetObjId);
    }
}
