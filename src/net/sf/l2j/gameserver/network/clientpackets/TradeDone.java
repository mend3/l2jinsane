package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;

public final class TradeDone extends L2GameClientPacket {
    private int _response;

    protected void readImpl() {
        this._response = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        TradeList trade = player.getActiveTradeList();
        if (trade == null)
            return;
        if (trade.isLocked())
            return;
        if (this._response != 1) {
            player.cancelActiveTrade();
            return;
        }
        Player owner = trade.getOwner();
        if (owner == null || !owner.equals(player))
            return;
        Player partner = trade.getPartner();
        if (partner == null || World.getInstance().getPlayer(partner.getObjectId()) == null) {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.cancelActiveTrade();
            return;
        }
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            player.cancelActiveTrade();
            return;
        }
        if (owner.getActiveEnchantItem() != null) {
            owner.setActiveEnchantItem(null);
            owner.sendPacket(EnchantResult.CANCELLED);
            owner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        if (partner.getActiveEnchantItem() != null) {
            partner.setActiveEnchantItem(null);
            partner.sendPacket(EnchantResult.CANCELLED);
            partner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        trade.confirm();
    }
}
