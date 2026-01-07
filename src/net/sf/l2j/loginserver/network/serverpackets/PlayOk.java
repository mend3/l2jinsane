package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.loginserver.network.SessionKey;

public final class PlayOk extends L2LoginServerPacket {
    private final int _playOk1;

    private final int _playOk2;

    public PlayOk(SessionKey sessionKey) {
        this._playOk1 = sessionKey.playOkID1;
        this._playOk2 = sessionKey.playOkID2;
    }

    protected void write() {
        writeC(7);
        writeD(this._playOk1);
        writeD(this._playOk2);
    }
}
