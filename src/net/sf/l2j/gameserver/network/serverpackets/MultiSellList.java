package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.ListContainer;

public class MultiSellList extends L2GameServerPacket {
    private final ListContainer _list;
    private final boolean _finished;
    private int _index;
    private int _size;

    public MultiSellList(ListContainer list, int index) {
        this._list = list;
        this._index = index;
        this._size = list.getEntries().size() - index;
        if (this._size > 40) {
            this._finished = false;
            this._size = 40;
        } else {
            this._finished = true;
        }
    }

    protected void writeImpl() {
        writeC(208);
        writeD(this._list.getId());
        writeD(1 + this._index / 40);
        writeD(this._finished ? 1 : 0);
        writeD(40);
        writeD(this._size);
        while (this._size-- > 0) {
            Entry ent = this._list.getEntries().get(this._index++);
            writeD(this._index);
            writeD(0);
            writeD(0);
            writeC(ent.isStackable() ? 1 : 0);
            writeH(ent.getProducts().size());
            writeH(ent.getIngredients().size());
            for (Ingredient ing : ent.getProducts()) {
                writeH(ing.getItemId());
                if (ing.getTemplate() != null) {
                    writeD(ing.getTemplate().getBodyPart());
                    writeH(ing.getTemplate().getType2());
                } else {
                    writeD(0);
                    writeH(65535);
                }
                writeD(ing.getItemCount());
                writeH(ing.getEnchantLevel());
                writeD(0);
                writeD(0);
            }
            for (Ingredient ing : ent.getIngredients()) {
                writeH(ing.getItemId());
                writeH((ing.getTemplate() != null) ? ing.getTemplate().getType2() : 65535);
                writeD(ing.getItemCount());
                writeH(ing.getEnchantLevel());
                writeD(0);
                writeD(0);
            }
        }
    }
}
