package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class ExStorageMaxCount extends L2GameServerPacket {
    private final int _inventoryLimit;

    private final int _warehouseLimit;

    private final int _freightLimit;

    private final int _privateSellLimit;

    private final int _privateBuyLimit;

    private final int _dwarfRecipeLimit;

    private final int _commonRecipeLimit;

    public ExStorageMaxCount(Player player) {
        this._inventoryLimit = player.getInventoryLimit();
        this._warehouseLimit = player.getWareHouseLimit();
        this._freightLimit = player.getFreightLimit();
        this._privateSellLimit = player.getPrivateSellStoreLimit();
        this._privateBuyLimit = player.getPrivateBuyStoreLimit();
        this._dwarfRecipeLimit = player.getDwarfRecipeLimit();
        this._commonRecipeLimit = player.getCommonRecipeLimit();
    }

    protected void writeImpl() {
        writeC(254);
        writeH(46);
        writeD(this._inventoryLimit);
        writeD(this._warehouseLimit);
        writeD(this._freightLimit);
        writeD(this._privateSellLimit);
        writeD(this._privateBuyLimit);
        writeD(this._dwarfRecipeLimit);
        writeD(this._commonRecipeLimit);
    }
}
