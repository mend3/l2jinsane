/**/
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Iterator;

public class ExEnchantSkillInfo extends L2GameServerPacket {
    private final ArrayList<ExEnchantSkillInfo.Req> _reqs = new ArrayList<>();
    private final int _id;
    private final int _level;
    private final int _spCost;
    private final int _xpCost;
    private final int _rate;

    public ExEnchantSkillInfo(int id, int level, int spCost, int xpCost, int rate) {
        this._id = id;
        this._level = level;
        this._spCost = spCost;
        this._xpCost = xpCost;
        this._rate = rate;
    }

    public void addRequirement(int type, int id, int count, int unk) {
        this._reqs.add(new Req(this, type, id, count, unk));
    }

    protected void writeImpl() {
        this.writeC(254);
        this.writeH(24);
        this.writeD(this._id);
        this.writeD(this._level);
        this.writeD(this._spCost);
        this.writeQ(this._xpCost);
        this.writeD(this._rate);
        this.writeD(this._reqs.size());

        for (Req temp : this._reqs) {
            this.writeD(temp.type);
            this.writeD(temp.id);
            this.writeD(temp.count);
            this.writeD(temp.unk);
        }

    }

    static class Req {
        public int id;
        public int count;
        public int type;
        public int unk;

        Req(final ExEnchantSkillInfo param1, int pId, int pType, int pCount, int pUnk) {
            this.id = pId;
            this.type = pType;
            this.count = pCount;
            this.unk = pUnk;
        }
    }
}