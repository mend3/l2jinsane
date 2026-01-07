package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.craft.ManufactureList;

import java.util.List;

public class RecipeShopSellList extends L2GameServerPacket {
    private final Player _buyer;

    private final Player _manufacturer;

    public RecipeShopSellList(Player buyer, Player manufacturer) {
        this._buyer = buyer;
        this._manufacturer = manufacturer;
    }

    protected final void writeImpl() {
        ManufactureList createList = this._manufacturer.getCreateList();
        if (createList != null) {
            writeC(217);
            writeD(this._manufacturer.getObjectId());
            writeD((int) this._manufacturer.getCurrentMp());
            writeD(this._manufacturer.getMaxMp());
            writeD(this._buyer.getAdena());
            List<ManufactureItem> list = createList.getList();
            writeD(list.size());
            for (ManufactureItem item : list) {
                writeD(item.getId());
                writeD(0);
                writeD(item.getValue());
            }
        }
    }
}
