package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.commons.network.StatusType;
import net.sf.l2j.loginserver.GameServerManager;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.model.ServerData;
import net.sf.l2j.loginserver.network.LoginClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class ServerList extends L2LoginServerPacket {
    private final List<ServerData> _servers = new ArrayList<>();

    private final int _lastServer;

    public ServerList(LoginClient client) {
        this._lastServer = client.getLastServer();
        for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values()) {
            StatusType status = (client.getAccessLevel() < 0 || (gsi.getStatus() == StatusType.GM_ONLY && client.getAccessLevel() <= 0)) ? StatusType.DOWN : gsi.getStatus();
            String hostName = gsi.getHostName();
            this._servers.add(new ServerData(status, hostName, gsi));
        }
    }

    public void write() {
        writeC(4);
        writeC(this._servers.size());
        writeC(this._lastServer);
        for (ServerData server : this._servers) {
            writeC(server.getServerId());
            try {
                byte[] raw = InetAddress.getByName(server.getHostName()).getAddress();
                writeC(raw[0] & 0xFF);
                writeC(raw[1] & 0xFF);
                writeC(raw[2] & 0xFF);
                writeC(raw[3] & 0xFF);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                writeC(127);
                writeC(0);
                writeC(0);
                writeC(1);
            }
            writeD(server.getPort());
            writeC(server.getAgeLimit());
            writeC(server.isPvp() ? 1 : 0);
            writeH(server.getCurrentPlayers());
            writeH(server.getMaxPlayers());
            writeC((server.getStatus() == StatusType.DOWN) ? 0 : 1);
            int bits = 0;
            if (server.isTestServer())
                bits |= 0x4;
            if (server.isShowingClock())
                bits |= 0x2;
            writeD(bits);
            writeC(server.isShowingBrackets() ? 1 : 0);
        }
    }
}
