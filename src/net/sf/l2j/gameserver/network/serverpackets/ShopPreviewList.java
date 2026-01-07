package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;

import java.util.Collection;

public class ShopPreviewList extends L2GameServerPacket {
    private final int _listId;

    private final int _money;

    private final int _expertise;

    private final Collection<Product> _list;

    public ShopPreviewList(NpcBuyList list, int currentMoney, int expertiseIndex) {
        this._listId = list.getListId();
        this._list = list.getProducts();
        this._money = currentMoney;
        this._expertise = expertiseIndex;
    }

    protected final void writeImpl() {
        writeC(239);
        writeC(192);
        writeC(19);
        writeC(0);
        writeC(0);
        writeD(this._money);
        writeD(this._listId);
        int newlength = 0;
        for (Product product : this._list) {
            if (product.getItem().getCrystalType().getId() <= this._expertise && product.getItem().isEquipable())
                newlength++;
        }
        writeH(newlength);
        for (Product product : this._list) {
            if (product.getItem().getCrystalType().getId() <= this._expertise && product.getItem().isEquipable()) {
                writeD(product.getItemId());
                writeH(product.getItem().getType2());
                if (product.getItem().getType1() != 4) {
                    writeH(product.getItem().getBodyPart());
                } else {
                    writeH(0);
                }
                writeD(Config.WEAR_PRICE);
            }
        }
    }
}
