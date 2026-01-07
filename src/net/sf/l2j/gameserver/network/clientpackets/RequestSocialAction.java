package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public class RequestSocialAction extends L2GameClientPacket {
    private int _actionId;

    protected void readImpl() {
        this._actionId = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.SOCIAL))
            return;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isFishing()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
            return;
        }
        if (this._actionId < 2 || this._actionId > 13)
            return;
        if (activeChar.isInStoreMode() || activeChar.getActiveRequester() != null || activeChar.isAlikeDead() || activeChar.getAI().getDesire().getIntention() != IntentionType.IDLE)
            return;
        activeChar.broadcastPacket(new SocialAction(activeChar, this._actionId));
    }
}
