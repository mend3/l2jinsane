package net.sf.l2j.gameserver.network.serverpackets;

public final class JoinParty extends L2GameServerPacket {
    private final int _response;

    public JoinParty(int response) {
        this._response = response;
    }

    protected void writeImpl() {
        writeC(58);
        writeD(this._response);
    }
}
