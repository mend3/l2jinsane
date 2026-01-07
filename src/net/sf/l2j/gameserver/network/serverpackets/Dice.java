package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class Dice extends L2GameServerPacket {
    private final int _objectId;

    private final int _itemId;

    private final int _number;

    private final int _x;

    private final int _y;

    private final int _z;

    public Dice(Player player, int itemId, int number) {
        this._objectId = player.getObjectId();
        this._itemId = itemId;
        this._number = number;
        this._x = player.getX() - 30;
        this._y = player.getY() - 30;
        this._z = player.getZ();
    }

    protected final void writeImpl() {
        writeC(212);
        writeD(this._objectId);
        writeD(this._itemId);
        writeD(this._number);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
