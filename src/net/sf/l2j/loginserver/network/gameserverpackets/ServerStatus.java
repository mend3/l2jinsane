package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.commons.network.AttributeType;
import net.sf.l2j.commons.network.StatusType;
import net.sf.l2j.loginserver.GameServerManager;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

public class ServerStatus extends ClientBasePacket {
    private static final int ON = 1;

    public ServerStatus(byte[] decrypt, int serverId) {
        super(decrypt);
        GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServers().get(Integer.valueOf(serverId));
        if (gsi != null) {
            int size = readD();
            for (int i = 0; i < size; i++) {
                int type = readD();
                int value = readD();
                switch (AttributeType.VALUES[type]) {
                    case STATUS:
                        gsi.setStatus(StatusType.VALUES[value]);
                        break;
                    case CLOCK:
                        gsi.setShowingClock((value == 1));
                        break;
                    case BRACKETS:
                        gsi.setShowingBrackets((value == 1));
                        break;
                    case AGE_LIMIT:
                        gsi.setAgeLimit(value);
                        break;
                    case TEST_SERVER:
                        gsi.setTestServer((value == 1));
                        break;
                    case PVP_SERVER:
                        gsi.setPvp((value == 1));
                        break;
                    case MAX_PLAYERS:
                        gsi.setMaxPlayers(value);
                        break;
                }
            }
        }
    }
}
