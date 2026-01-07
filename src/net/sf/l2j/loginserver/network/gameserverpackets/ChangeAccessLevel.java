package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

public class ChangeAccessLevel extends ClientBasePacket {
    private final int _level;

    private final String _account;

    public ChangeAccessLevel(byte[] decrypt) {
        super(decrypt);
        this._level = readD();
        this._account = readS();
    }

    public String getAccount() {
        return this._account;
    }

    public int getLevel() {
        return this._level;
    }
}
