package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.model.CharSelectSlot;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharSelected;
import net.sf.l2j.gameserver.network.serverpackets.SSQInfo;

public class CharacterSelected extends L2GameClientPacket {
    private int _charSlot;

    private int _unk1;

    private int _unk2;

    private int _unk3;

    private int _unk4;

    protected void readImpl() {
        this._charSlot = readD();
        this._unk1 = readH();
        this._unk2 = readD();
        this._unk3 = readD();
        this._unk4 = readD();
    }

    protected void runImpl() {
        GameClient client = getClient();
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.CHARACTER_SELECT))
            return;
        if (client.getActiveCharLock().tryLock())
            try {
                if (client.getPlayer() == null) {
                    CharSelectSlot info = client.getCharSelectSlot(this._charSlot);
                    if (info == null || info.getAccessLevel() < 0)
                        return;
                    Player cha = client.loadCharFromDisk(this._charSlot);
                    if (cha == null)
                        return;
                    cha.setClient(client);
                    client.setPlayer(cha);
                    cha.setOnlineStatus(true, true);
                    sendPacket(SSQInfo.sendSky());
                    if (!Hwid.checkPlayerWithHWID(client, cha.getObjectId(), cha.getName()))
                        return;
                    client.setState(GameClient.GameClientState.ENTERING);
                    sendPacket(new CharSelected(cha, (client.getSessionId()).playOkID1()));
                }
            } finally {
                client.getActiveCharLock().unlock();
            }
    }
}
