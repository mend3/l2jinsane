package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestHennaEquip extends L2GameClientPacket {
    private int _symbolId;

    protected void readImpl() {
        this._symbolId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Henna henna = HennaData.getInstance().getHenna(this._symbolId);
        if (henna == null)
            return;
        if (!henna.canBeUsedBy(player)) {
            player.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
            return;
        }
        if (player.getHennaList().isFull()) {
            player.sendPacket(SystemMessageId.SYMBOLS_FULL);
            return;
        }
        ItemInstance ownedDyes = player.getInventory().getItemByItemId(henna.getDyeId());
        int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
        if (count < 10) {
            player.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
            return;
        }
        if (!player.reduceAdena("Henna", henna.getDrawPrice(), player.getCurrentFolk(), true))
            return;
        if (!player.destroyItemByItemId("Henna", henna.getDyeId(), 10, player, true))
            return;
        boolean success = player.getHennaList().add(henna);
        if (success) {
            player.sendPacket(new HennaInfo(player));
            player.sendPacket(new UserInfo(player));
            player.sendPacket(SystemMessageId.SYMBOL_ADDED);
        }
    }
}
