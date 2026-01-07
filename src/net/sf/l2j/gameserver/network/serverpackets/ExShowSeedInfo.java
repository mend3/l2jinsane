package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.model.manor.SeedProduction;

import java.util.List;

public class ExShowSeedInfo extends L2GameServerPacket {
    private final List<SeedProduction> _seeds;

    private final int _manorId;

    private final boolean _hideButtons;

    public ExShowSeedInfo(int manorId, boolean nextPeriod, boolean hideButtons) {
        this._manorId = manorId;
        this._hideButtons = hideButtons;
        CastleManorManager manor = CastleManorManager.getInstance();
        this._seeds = (nextPeriod && !manor.isManorApproved()) ? null : manor.getSeedProduction(manorId, nextPeriod);
    }

    protected void writeImpl() {
        writeC(254);
        writeH(28);
        writeC(this._hideButtons ? 1 : 0);
        writeD(this._manorId);
        writeD(0);
        if (this._seeds == null) {
            writeD(0);
            return;
        }
        writeD(this._seeds.size());
        for (SeedProduction seed : this._seeds) {
            writeD(seed.getId());
            writeD(seed.getAmount());
            writeD(seed.getStartAmount());
            writeD(seed.getPrice());
            Seed s = CastleManorManager.getInstance().getSeed(seed.getId());
            if (s == null) {
                writeD(0);
                writeC(1);
                writeD(0);
                writeC(1);
                writeD(0);
                continue;
            }
            writeD(s.getLevel());
            writeC(1);
            writeD(s.getReward(1));
            writeC(1);
            writeD(s.getReward(2));
        }
    }
}
