package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeSkillListAdd extends L2GameServerPacket {
    private final int _id;

    private final int _lvl;

    public PledgeSkillListAdd(int id, int lvl) {
        this._id = id;
        this._lvl = lvl;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(58);
        writeD(this._id);
        writeD(this._lvl);
    }
}
