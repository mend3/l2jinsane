package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;

import java.util.HashMap;
import java.util.Map;

public class ExShowSellCropList extends L2GameServerPacket {
    private final Map<Integer, ItemInstance> _cropsItems;
    private final Map<Integer, CropProcure> _castleCrops;
    private int _manorId = 1;

    public ExShowSellCropList(PcInventory inventory, int manorId) {
        this._manorId = manorId;
        this._castleCrops = new HashMap<>();
        this._cropsItems = new HashMap<>();
        for (int cropId : CastleManorManager.getInstance().getCropIds()) {
            ItemInstance item = inventory.getItemByItemId(cropId);
            if (item != null)
                this._cropsItems.put(cropId, item);
        }
        for (CropProcure crop : CastleManorManager.getInstance().getCropProcure(this._manorId, false)) {
            if (this._cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
                this._castleCrops.put(crop.getId(), crop);
        }
    }

    public void writeImpl() {
        writeC(254);
        writeH(33);
        writeD(this._manorId);
        writeD(this._cropsItems.size());
        for (ItemInstance item : this._cropsItems.values()) {
            Seed seed = CastleManorManager.getInstance().getSeedByCrop(item.getItemId());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(seed.getLevel());
            writeC(1);
            writeD(seed.getReward(1));
            writeC(1);
            writeD(seed.getReward(2));
            if (this._castleCrops.containsKey(item.getItemId())) {
                CropProcure crop = this._castleCrops.get(item.getItemId());
                writeD(this._manorId);
                writeD(crop.getAmount());
                writeD(crop.getPrice());
                writeC(crop.getReward());
            } else {
                writeD(-1);
                writeD(0);
                writeD(0);
                writeC(0);
            }
            writeD(item.getCount());
        }
    }
}
