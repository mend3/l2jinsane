package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;

import java.util.Collection;

public final class BuyList extends L2GameServerPacket {
    private final int _listId;

    private final int _money;

    private final Collection<Product> _list;

    private double _taxRate = 0.0D;

    public BuyList(NpcBuyList list, int currentMoney, double taxRate) {
        this._listId = list.getListId();
        this._list = list.getProducts();
        this._money = currentMoney;
        this._taxRate = taxRate;
    }

    protected void writeImpl() {
        writeC(17);
        writeD(this._money);
        writeD(this._listId);
        writeH(this._list.size());
        for (Product product : this._list) {
            if (product.getCount() > 0 || !product.hasLimitedStock()) {
                writeH(product.getItem().getType1());
                writeD(product.getItemId());
                writeD(product.getItemId());
                writeD(Math.max(product.getCount(), 0));
                writeH(product.getItem().getType2());
                writeH(0);
                writeD(product.getItem().getBodyPart());
                writeH(0);
                writeH(0);
                writeH(0);
                if (product.getItemId() >= 3960 && product.getItemId() <= 4026) {
                    writeD((int) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1.0D + this._taxRate)));
                    continue;
                }
                writeD((int) (product.getPrice() * (1.0D + this._taxRate)));
            }
        }
    }
}
