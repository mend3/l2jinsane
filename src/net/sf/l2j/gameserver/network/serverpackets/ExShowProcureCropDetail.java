package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.manor.CropProcure;

import java.util.HashMap;
import java.util.Map;

public class ExShowProcureCropDetail extends L2GameServerPacket {
    private final int _cropId;

    private final Map<Integer, CropProcure> _castleCrops;

    public ExShowProcureCropDetail(int cropId) {
        this._cropId = cropId;
        this._castleCrops = new HashMap<>();
        for (Castle c : CastleManager.getInstance().getCastles()) {
            CropProcure cropItem = CastleManorManager.getInstance().getCropProcure(c.getCastleId(), cropId, false);
            if (cropItem != null && cropItem.getAmount() > 0)
                this._castleCrops.put(c.getCastleId(), cropItem);
        }
    }

    public void writeImpl() {
        writeC(254);
        writeH(34);
        writeD(this._cropId);
        writeD(this._castleCrops.size());
        for (Map.Entry<Integer, CropProcure> entry : this._castleCrops.entrySet()) {
            CropProcure crop = entry.getValue();
            writeD(entry.getKey());
            writeD(crop.getAmount());
            writeD(crop.getPrice());
            writeC(crop.getReward());
        }
    }
}
