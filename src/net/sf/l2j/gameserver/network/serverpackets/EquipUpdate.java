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
        int bodypart = switch (this._item.getItem().getBodyPart()) {
            case 4 -> 1;
            case 2 -> 2;
            case 8 -> 3;
            case 16 -> 4;
            case 32 -> 5;
            case 64 -> 6;
            case 128 -> 7;
            case 256 -> 8;
            case 512 -> 9;
            case 1024 -> 10;
            case 2048 -> 11;
            case 4096 -> 12;
            case 8192 -> 13;
            case 16384 -> 14;
            case 262144 -> 15;
            default -> 0;
        };
        writeD(bodypart);
    }
}
