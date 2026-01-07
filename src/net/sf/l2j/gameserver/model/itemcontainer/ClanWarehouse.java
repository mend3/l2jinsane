package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends ItemContainer {
    private final Clan _clan;

    public ClanWarehouse(Clan clan) {
        this._clan = clan;
    }

    public String getName() {
        return "ClanWarehouse";
    }

    public int getOwnerId() {
        return this._clan.getClanId();
    }

    public Player getOwner() {
        return this._clan.getLeader().getPlayerInstance();
    }

    public ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.CLANWH;
    }

    public boolean validateCapacity(int slots) {
        return (this._items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN);
    }
}
