package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExShowCropSetting extends L2GameServerPacket {
    private final int _manorId;

    private final Set<Seed> _seeds;

    private final Map<Integer, CropProcure> _current = new HashMap<>();

    private final Map<Integer, CropProcure> _next = new HashMap<>();

    public ExShowCropSetting(int manorId) {
        CastleManorManager manor = CastleManorManager.getInstance();
        this._manorId = manorId;
        this._seeds = manor.getSeedsForCastle(this._manorId);
        for (Seed s : this._seeds) {
            CropProcure cp = manor.getCropProcure(manorId, s.getCropId(), false);
            if (cp != null)
                this._current.put(Integer.valueOf(s.getCropId()), cp);
            cp = manor.getCropProcure(manorId, s.getCropId(), true);
            if (cp != null)
                this._next.put(Integer.valueOf(s.getCropId()), cp);
        }
    }

    public void writeImpl() {
        writeC(254);
        writeH(32);
        writeD(this._manorId);
        writeD(this._seeds.size());
        for (Seed s : this._seeds) {
            writeD(s.getCropId());
            writeD(s.getLevel());
            writeC(1);
            writeD(s.getReward(1));
            writeC(1);
            writeD(s.getReward(2));
            writeD(s.getCropLimit());
            writeD(0);
            writeD(s.getCropMinPrice());
            writeD(s.getCropMaxPrice());
            if (this._current.containsKey(Integer.valueOf(s.getCropId()))) {
                CropProcure cp = this._current.get(Integer.valueOf(s.getCropId()));
                writeD(cp.getStartAmount());
                writeD(cp.getPrice());
                writeC(cp.getReward());
            } else {
                writeD(0);
                writeD(0);
                writeC(0);
            }
            if (this._next.containsKey(Integer.valueOf(s.getCropId()))) {
                CropProcure cp = this._next.get(Integer.valueOf(s.getCropId()));
                writeD(cp.getStartAmount());
                writeD(cp.getPrice());
                writeC(cp.getReward());
                continue;
            }
            writeD(0);
            writeD(0);
            writeC(0);
        }
    }
}
