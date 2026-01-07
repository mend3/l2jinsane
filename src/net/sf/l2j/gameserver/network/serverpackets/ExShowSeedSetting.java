package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.model.manor.SeedProduction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExShowSeedSetting extends L2GameServerPacket {
    private final int _manorId;

    private final Set<Seed> _seeds;

    private final Map<Integer, SeedProduction> _current = new HashMap<>();

    private final Map<Integer, SeedProduction> _next = new HashMap<>();

    public ExShowSeedSetting(int manorId) {
        CastleManorManager manor = CastleManorManager.getInstance();
        this._manorId = manorId;
        this._seeds = manor.getSeedsForCastle(this._manorId);
        for (Seed s : this._seeds) {
            SeedProduction sp = manor.getSeedProduct(manorId, s.getSeedId(), false);
            if (sp != null)
                this._current.put(Integer.valueOf(s.getSeedId()), sp);
            sp = manor.getSeedProduct(manorId, s.getSeedId(), true);
            if (sp != null)
                this._next.put(Integer.valueOf(s.getSeedId()), sp);
        }
    }

    public void writeImpl() {
        writeC(254);
        writeH(31);
        writeD(this._manorId);
        writeD(this._seeds.size());
        for (Seed s : this._seeds) {
            writeD(s.getSeedId());
            writeD(s.getLevel());
            writeC(1);
            writeD(s.getReward(1));
            writeC(1);
            writeD(s.getReward(2));
            writeD(s.getSeedLimit());
            writeD(s.getSeedReferencePrice());
            writeD(s.getSeedMinPrice());
            writeD(s.getSeedMaxPrice());
            if (this._current.containsKey(Integer.valueOf(s.getSeedId()))) {
                SeedProduction sp = this._current.get(Integer.valueOf(s.getSeedId()));
                writeD(sp.getStartAmount());
                writeD(sp.getPrice());
            } else {
                writeD(0);
                writeD(0);
            }
            if (this._next.containsKey(Integer.valueOf(s.getSeedId()))) {
                SeedProduction sp = this._next.get(Integer.valueOf(s.getSeedId()));
                writeD(sp.getStartAmount());
                writeD(sp.getPrice());
                continue;
            }
            writeD(0);
            writeD(0);
        }
    }
}
