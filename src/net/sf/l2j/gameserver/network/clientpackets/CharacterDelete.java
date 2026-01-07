package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteFail;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterDelete extends L2GameClientPacket {
    private int _slot;

    protected void readImpl() {
        this._slot = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.CHARACTER_SELECT)) {
            sendPacket(CharDeleteFail.REASON_DELETION_FAILED);
            return;
        }
        switch (getClient().markToDeleteChar(this._slot)) {
            case 0:
                sendPacket(CharDeleteOk.STATIC_PACKET);
                break;
            case 1:
                sendPacket(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER);
                break;
            case 2:
                sendPacket(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED);
                break;
        }
        CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), (getClient().getSessionId()).playOkID1, 0);
        sendPacket(csi);
        getClient().setCharSelectSlot(csi.getCharacterSlots());
    }
}
