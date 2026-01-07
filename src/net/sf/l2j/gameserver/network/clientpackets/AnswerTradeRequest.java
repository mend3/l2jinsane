package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends L2GameClientPacket {
    private int _response;

    protected void readImpl() {
        this._response = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        Player partner = player.getActiveRequester();
        if (partner == null || World.getInstance().getPlayer(partner.getObjectId()) == null) {
            player.sendPacket(new SendTradeDone(0));
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.setActiveRequester(null);
            return;
        }
        if (this._response == 1 && !partner.isRequestExpired()) {
            player.startTrade(partner);
        } else {
            partner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addCharName(player));
        }
        player.setActiveRequester(null);
        partner.onTransactionResponse();
    }
}
