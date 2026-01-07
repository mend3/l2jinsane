package net.sf.l2j.gameserver.network.serverpackets;

public class MagicSkillCanceled extends L2GameServerPacket {
    private final int _objectId;

    public MagicSkillCanceled(int objectId) {
        this._objectId = objectId;
    }

    protected final void writeImpl() {
        writeC(73);
        writeD(this._objectId);
    }
}
