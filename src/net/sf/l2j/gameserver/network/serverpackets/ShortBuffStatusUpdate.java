package net.sf.l2j.gameserver.network.serverpackets;

public class ShortBuffStatusUpdate extends L2GameServerPacket {
    private final int _skillId;

    private final int _skillLvl;

    private final int _duration;

    public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration) {
        this._skillId = skillId;
        this._skillLvl = skillLvl;
        this._duration = duration;
    }

    protected final void writeImpl() {
        writeC(244);
        writeD(this._skillId);
        writeD(this._skillLvl);
        writeD(this._duration);
    }
}
