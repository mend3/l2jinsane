package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.Seed;

import java.util.List;

public class ExShowManorDefaultInfo extends L2GameServerPacket {
    private final List<Seed> _crops;

    private final boolean _hideButtons;

    public ExShowManorDefaultInfo(boolean hideButtons) {
        this._crops = CastleManorManager.getInstance().getCrops();
        this._hideButtons = hideButtons;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(30);
        writeC(this._hideButtons ? 1 : 0);
        writeD(this._crops.size());
        for (Seed crop : this._crops) {
            writeD(crop.getCropId());
            writeD(crop.getLevel());
            writeD(crop.getSeedReferencePrice());
            writeD(crop.getCropReferencePrice());
            writeC(1);
            writeD(crop.getReward(1));
            writeC(1);
            writeD(crop.getReward(2));
        }
    }
}
