package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillInfo extends L2GameServerPacket {
    private final List<Req> _reqs;

    private final int _id;

    private final int _level;

    private final int _spCost;

    private final int _mode;

    public AcquireSkillInfo(int id, int level, int spCost, int mode) {
        this._reqs = new ArrayList<>();
        this._id = id;
        this._level = level;
        this._spCost = spCost;
        this._mode = mode;
    }

    public void addRequirement(int type, int id, int count, int unk) {
        this._reqs.add(new Req(type, id, count, unk));
    }

    protected final void writeImpl() {
        writeC(139);
        writeD(this._id);
        writeD(this._level);
        writeD(this._spCost);
        writeD(this._mode);
        writeD(this._reqs.size());
        for (Req temp : this._reqs) {
            writeD(temp.type);
            writeD(temp.itemId);
            writeD(temp.count);
            writeD(temp.unk);
        }
    }

    private static class Req {
        public int itemId;

        public int count;

        public int type;

        public int unk;

        public Req(int pType, int pItemId, int pCount, int pUnk) {
            this.itemId = pItemId;
            this.type = pType;
            this.count = pCount;
            this.unk = pUnk;
        }
    }
}
