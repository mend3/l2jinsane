package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;

import java.util.List;

public class ExShowCropInfo extends L2GameServerPacket {
    private final List<CropProcure> _crops;

    private final int _manorId;

    private final boolean _hideButtons;

    public ExShowCropInfo(int manorId, boolean nextPeriod, boolean hideButtons) {
        this._manorId = manorId;
        this._hideButtons = hideButtons;
        CastleManorManager manor = CastleManorManager.getInstance();
        this._crops = (nextPeriod && !manor.isManorApproved()) ? null : manor.getCropProcure(manorId, nextPeriod);
    }

    protected void writeImpl() {
        writeC(254);
        writeH(29);
        writeC(this._hideButtons ? 1 : 0);
        writeD(this._manorId);
        writeD(0);
        if (this._crops == null) {
            writeD(0);
            return;
        }
        writeD(this._crops.size());
        for (CropProcure crop : this._crops) {
            writeD(crop.getId());
            writeD(crop.getAmount());
            writeD(crop.getStartAmount());
            writeD(crop.getPrice());
            writeC(crop.getReward());
            Seed seed = CastleManorManager.getInstance().getSeedByCrop(crop.getId());
            if (seed == null) {
                writeD(0);
                writeC(1);
                writeD(0);
                writeC(1);
                writeD(0);
                continue;
            }
            writeD(seed.getLevel());
            writeC(1);
            writeD(seed.getReward(1));
            writeC(1);
            writeD(seed.getReward(2));
        }
    }
}
