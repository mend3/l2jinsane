package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Door;

public class DoorStatusUpdate extends L2GameServerPacket {
    private final Door _door;

    private final boolean _showHp;

    public DoorStatusUpdate(Door door) {
        this._door = door;
        this._showHp = (door.getCastle() != null && door.getCastle().getSiege().isInProgress());
    }

    protected final void writeImpl() {
        writeC(77);
        writeD(this._door.getObjectId());
        writeD(this._door.isOpened() ? 0 : 1);
        writeD(this._door.getDamage());
        writeD(this._showHp ? 1 : 0);
        writeD(this._door.getDoorId());
        writeD(this._door.getMaxHp());
        writeD((int) this._door.getCurrentHp());
    }
}
