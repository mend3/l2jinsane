package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;

import java.util.List;

public class HennaRemoveList extends L2GameServerPacket {
    private final int _adena;

    private final int _emptySlots;

    private final List<Henna> _hennas;

    public HennaRemoveList(Player player) {
        this._adena = player.getAdena();
        this._emptySlots = player.getHennaList().getEmptySlotsAmount();
        this._hennas = player.getHennaList().getHennas();
    }

    protected final void writeImpl() {
        writeC(229);
        writeD(this._adena);
        writeD(this._emptySlots);
        writeD(this._hennas.size());
        for (Henna henna : this._hennas) {
            writeD(henna.getSymbolId());
            writeD(henna.getDyeId());
            writeD(5);
            writeD(henna.getRemovePrice());
            writeD(1);
        }
    }
}
