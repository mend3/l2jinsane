package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestHennaRemove extends L2GameClientPacket {
    private int _symbolId;

    protected void readImpl() {
        this._symbolId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Henna henna = player.getHennaList().getBySymbolId(this._symbolId);
        if (henna == null)
            return;
        if (player.getAdena() < henna.getRemovePrice()) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return;
        }
        boolean success = player.getHennaList().remove(henna);
        if (!success)
            return;
        sendPacket(new HennaInfo(player));
        sendPacket(new UserInfo(player));
        player.reduceAdena("Henna", henna.getRemovePrice(), player, false);
        player.addItem("Henna", henna.getDyeId(), 5, player, true);
        player.sendPacket(SystemMessageId.SYMBOL_DELETED);
    }
}
