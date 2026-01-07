package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeShowMemberListDelete extends L2GameServerPacket {
    private final String _player;

    public PledgeShowMemberListDelete(String playerName) {
        this._player = playerName;
    }

    protected final void writeImpl() {
        writeC(86);
        writeS(this._player);
    }
}
