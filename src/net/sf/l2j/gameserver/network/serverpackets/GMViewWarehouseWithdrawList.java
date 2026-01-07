package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.Set;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket {
    private final Set<ItemInstance> _items;

    private final String _playerName;

    private final int _money;

    public GMViewWarehouseWithdrawList(Player player) {
        this._items = player.getWarehouse().getItems();
        this._playerName = player.getName();
        this._money = player.getWarehouse().getAdena();
    }

    public GMViewWarehouseWithdrawList(Clan clan) {
        this._playerName = clan.getLeaderName();
        this._items = clan.getWarehouse().getItems();
        this._money = clan.getWarehouse().getAdena();
    }

    protected final void writeImpl() {
        writeC(149);
        writeS(this._playerName);
        writeD(this._money);
        writeH(this._items.size());
        for (ItemInstance temp : this._items) {
            Item item = temp.getItem();
            writeH(item.getType1());
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD(temp.getCount());
            writeH(item.getType2());
            writeH(temp.getCustomType1());
            writeD(item.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.isWeapon() ? ((Weapon) item).getSoulShotCount() : 0);
            writeH(temp.isWeapon() ? ((Weapon) item).getSpiritShotCount() : 0);
            writeD(temp.getObjectId());
            writeD((temp.isWeapon() && temp.isAugmented()) ? (0xFFFF & temp.getAugmentation().getAugmentationId()) : 0);
            writeD((temp.isWeapon() && temp.isAugmented()) ? (temp.getAugmentation().getAugmentationId() >> 16) : 0);
        }
    }
}
