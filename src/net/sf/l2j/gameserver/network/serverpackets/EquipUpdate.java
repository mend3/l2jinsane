package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class EquipUpdate extends L2GameServerPacket {
    private final ItemInstance _item;

    private final int _change;

    public EquipUpdate(ItemInstance item, int change) {
        this._item = item;
        this._change = change;
    }

    protected final void writeImpl() {
        writeC(75);
        writeD(this._change);
        writeD(this._item.getObjectId());
        int bodypart = 0;
        switch (this._item.getItem().getBodyPart()) {
            case 4:
                bodypart = 1;
                break;
            case 2:
                bodypart = 2;
                break;
            case 8:
                bodypart = 3;
                break;
            case 16:
                bodypart = 4;
                break;
            case 32:
                bodypart = 5;
                break;
            case 64:
                bodypart = 6;
                break;
            case 128:
                bodypart = 7;
                break;
            case 256:
                bodypart = 8;
                break;
            case 512:
                bodypart = 9;
                break;
            case 1024:
                bodypart = 10;
                break;
            case 2048:
                bodypart = 11;
                break;
            case 4096:
                bodypart = 12;
                break;
            case 8192:
                bodypart = 13;
                break;
            case 16384:
                bodypart = 14;
                break;
            case 262144:
                bodypart = 15;
                break;
        }
        writeD(bodypart);
    }
}
