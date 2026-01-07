package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket {
    private final int _eventId;

    public TutorialEnableClientEvent(int event) {
        this._eventId = event;
    }

    protected void writeImpl() {
        writeC(162);
        writeD(this._eventId);
    }
}
