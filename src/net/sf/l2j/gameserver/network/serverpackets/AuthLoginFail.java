package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.FailReason;

public class AuthLoginFail extends L2GameServerPacket {
    private final FailReason _reason;

    public AuthLoginFail(FailReason reason) {
        this._reason = reason;
    }

    protected final void writeImpl() {
        writeC(20);
        writeD(this._reason.ordinal());
    }
}
