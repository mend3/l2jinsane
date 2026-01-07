package net.sf.l2j.gameserver.network.serverpackets;

public class EventTrigger extends L2GameServerPacket {
    private final int _trapId;

    private final boolean _active;

    public EventTrigger(int trapId, boolean active) {
        this._trapId = trapId;
        this._active = active;
    }

    protected final void writeImpl() {
        writeC(207);
        writeD(this._trapId);
        writeC(this._active ? 1 : 0);
    }
}
