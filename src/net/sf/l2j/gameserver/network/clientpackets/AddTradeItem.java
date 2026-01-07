package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.TradeItemUpdate;
import net.sf.l2j.gameserver.network.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeOwnAdd;

public final class AddTradeItem extends L2GameClientPacket {
    private int _tradeId;

    private int _objectId;

    private int _count;

    protected void readImpl() {
        this._tradeId = readD();
        this._objectId = readD();
        this._count = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        TradeList trade = player.getActiveTradeList();
        if (trade == null)
            return;
        Player partner = trade.getPartner();
        if (partner == null || World.getInstance().getPlayer(partner.getObjectId()) == null || partner.getActiveTradeList() == null) {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.cancelActiveTrade();
            return;
        }
        if (trade.isConfirmed() || partner.getActiveTradeList().isConfirmed()) {
            player.sendPacket(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED);
            return;
        }
        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            player.cancelActiveTrade();
            return;
        }
        if (player.validateItemManipulation(this._objectId) == null) {
            player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
            return;
        }
        TradeItem item = trade.addItem(this._objectId, this._count);
        if (item != null) {
            player.sendPacket(new TradeOwnAdd(item));
            player.sendPacket(new TradeItemUpdate(trade, player));
            trade.getPartner().sendPacket(new TradeOtherAdd(item));
        }
    }
}
