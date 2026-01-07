package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;

import java.util.List;
import java.util.stream.Collectors;

public class HennaEquipList extends L2GameServerPacket {
    private final int _adena;

    private final int _maxHennas;

    private final List<Henna> _availableHennas;

    public HennaEquipList(Player player) {
        this._adena = player.getAdena();
        this._maxHennas = player.getHennaList().getMaxSize();
        this._availableHennas = HennaData.getInstance().getHennas().stream().filter(h -> (h.canBeUsedBy(player) && player.getInventory().getItemByItemId(h.getDyeId()) != null)).collect(Collectors.toList());
    }

    protected final void writeImpl() {
        writeC(226);
        writeD(this._adena);
        writeD(this._maxHennas);
        writeD(this._availableHennas.size());
        for (Henna temp : this._availableHennas) {
            writeD(temp.getSymbolId());
            writeD(temp.getDyeId());
            writeD(10);
            writeD(temp.getDrawPrice());
            writeD(1);
        }
    }
}
