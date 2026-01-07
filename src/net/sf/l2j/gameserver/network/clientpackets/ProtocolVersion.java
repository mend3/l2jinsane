package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;

public final class ProtocolVersion extends L2GameClientPacket {
    private int _version;

    private byte[] _data;

    private String _hwidHdd = "NoHWID-HD";

    private String _hwidMac = "NoHWID-MAC";

    private String _hwidCPU = "NoHWID-CPU";

    protected void readImpl() {
        this._version = readD();
        if (Hwid.isProtectionOn()) {
            if (this._buf.remaining() > 260) {
                this._data = new byte[260];
                readB(this._data);
                if (Hwid.isProtectionOn()) {
                    this._hwidHdd = readS();
                    this._hwidMac = readS();
                    this._hwidCPU = readS();
                }
            }
        } else if (Hwid.isProtectionOn()) {
            getClient().close(new KeyPacket(getClient().enableCrypt()));
        }
    }

    protected void runImpl() {
        switch (this._version) {
            case 737:
            case 740:
            case 744:
            case 746:
                getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
                getClient().setRevision(this._version);
                if (Hwid.isProtectionOn()) {
                    if (this._hwidHdd.equals("NoGuard") && this._hwidMac.equals("NoGuard") && this._hwidCPU.equals("NoGuard")) {
                        LOGGER.info("HWID Status: No Client side dlls");
                        getClient().close(new KeyPacket(getClient().enableCrypt()));
                    }
                    switch (HwidConfig.GET_CLIENT_HWID) {
                        case 1:
                            getClient().setHWID(this._hwidHdd);
                            break;
                        case 2:
                            getClient().setHWID(this._hwidMac);
                            break;
                        case 3:
                            getClient().setHWID(this._hwidCPU);
                            break;
                    }
                }
                return;
        }
        getClient().close(null);
    }
}
