package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

public class GameServerAuth extends ClientBasePacket {
    private final byte[] _hexId;

    private final int _desiredId;

    private final boolean _hostReserved;

    private final boolean _acceptAlternativeId;

    private final int _maxPlayers;

    private final int _port;

    private final String _hostName;

    public GameServerAuth(byte[] decrypt) {
        super(decrypt);
        this._desiredId = readC();
        this._acceptAlternativeId = (readC() != 0);
        this._hostReserved = (readC() != 0);
        this._hostName = readS();
        this._port = readH();
        this._maxPlayers = readD();
        int size = readD();
        this._hexId = readB(size);
    }

    public byte[] getHexID() {
        return this._hexId;
    }

    public boolean getHostReserved() {
        return this._hostReserved;
    }

    public int getDesiredID() {
        return this._desiredId;
    }

    public boolean acceptAlternateID() {
        return this._acceptAlternativeId;
    }

    public int getMaxPlayers() {
        return this._maxPlayers;
    }

    public String getHostName() {
        return this._hostName;
    }

    public int getPort() {
        return this._port;
    }
}
