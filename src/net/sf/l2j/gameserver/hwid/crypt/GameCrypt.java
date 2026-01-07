package net.sf.l2j.gameserver.hwid.crypt;

import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.hwid.crypt.impl.L2Client;
import net.sf.l2j.gameserver.hwid.crypt.impl.L2Server;
import net.sf.l2j.gameserver.hwid.crypt.impl.VMPC;

public class GameCrypt {
    private ProtectionCrypt _client;

    private ProtectionCrypt _server;

    private boolean _isEnabled = false;

    private boolean _isProtected = false;

    public void setProtected(boolean state) {
        this._isProtected = state;
    }

    public void setKey(byte[] key) {
        if (this._isProtected) {
            this._client = new VMPC();
            this._client.setup(key, HwidConfig.GUARD_CLIENT_CRYPT);
            this._server = new L2Server();
            this._server.setup(key, null);
            this._server = new VMPC();
            this._server.setup(key, HwidConfig.GUARD_SERVER_CRYPT);
        } else {
            this._client = new L2Client();
            this._client.setup(key, null);
            this._server = new L2Server();
            this._server.setup(key, null);
        }
    }

    public void decrypt(byte[] raw, int offset, int size) {
        if (this._isEnabled)
            this._client.crypt(raw, offset, size);
    }

    public void encrypt(byte[] raw, int offset, int size) {
        if (this._isEnabled) {
            this._server.crypt(raw, offset, size);
        } else {
            this._isEnabled = true;
        }
    }
}
