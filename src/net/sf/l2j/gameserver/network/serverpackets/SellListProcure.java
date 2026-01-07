package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.manor.CropProcure;

import java.util.HashMap;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket {
    private final Map<ItemInstance, Integer> _sellList;

    private final int _money;

    public SellListProcure(Player player, int castleId) {
        this._money = player.getAdena();
        this._sellList = new HashMap<>();
        for (CropProcure c : CastleManorManager.getInstance().getCropProcure(castleId, false)) {
            ItemInstance item = player.getInventory().getItemByItemId(c.getId());
            if (item != null && c.getAmount() > 0)
                this._sellList.put(item, c.getAmount());
        }
    }

    protected final void writeImpl() {
        writeC(233);
        writeD(this._money);
        writeD(0);
        writeH(this._sellList.size());
        for (Map.Entry<ItemInstance, Integer> itemEntry : this._sellList.entrySet()) {
            ItemInstance item = itemEntry.getKey();
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(itemEntry.getValue());
            writeH(item.getItem().getType2());
            writeH(0);
            writeD(0);
        }
    }
}
