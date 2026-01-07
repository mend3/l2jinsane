package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterRestore extends L2GameClientPacket {
    private int _slot;

    protected void readImpl() {
        this._slot = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.CHARACTER_SELECT))
            return;
        getClient().markRestoredChar(this._slot);
        CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), (getClient().getSessionId()).playOkID1, 0);
        sendPacket(csi);
        getClient().setCharSelectSlot(csi.getCharacterSlots());
    }
}
