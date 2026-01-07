package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;

public final class RequestRecipeShopListSet extends L2GameClientPacket {
    private int _count;

    private int[] _items;

    protected void readImpl() {
        this._count = readD();
        if (this._count < 0 || this._count * 8 > this._buf.remaining() || this._count > Config.MAX_ITEM_IN_PACKET)
            this._count = 0;
        this._items = new int[this._count * 2];
        for (int x = 0; x < this._count; x++) {
            int recipeID = readD();
            this._items[x * 2] = recipeID;
            int cost = readD();
            this._items[x * 2 + 1] = cost;
        }
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isInDuel()) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            return;
        }
        if (player.isInsideZone(ZoneId.MULTI_FUNCTION) && !Config.STORE_ZONE) {
            player.sendMessage("You cannot craft while inside Multifunction zone.");
            return;
        }
        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (this._count == 0) {
            player.forceStandUp();
        } else {
            ManufactureList createList = new ManufactureList();
            for (int x = 0; x < this._count; x++) {
                int recipeID = this._items[x * 2];
                int cost = this._items[x * 2 + 1];
                createList.add(new ManufactureItem(recipeID, cost));
            }
            createList.setStoreName((player.getCreateList() != null) ? player.getCreateList().getStoreName() : "");
            player.setCreateList(createList);
            player.setStoreType(StoreType.MANUFACTURE);
            player.sitDown();
            player.broadcastUserInfo();
            player.broadcastPacket(new RecipeShopMsg(player));
        }
    }
}
