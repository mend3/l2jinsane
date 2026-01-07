package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.HennaList;
import net.sf.l2j.gameserver.model.item.Henna;

import java.util.List;

public final class HennaInfo extends L2GameServerPacket {
    private final HennaList _hennaList;

    public HennaInfo(Player player) {
        this._hennaList = player.getHennaList();
    }

    protected void writeImpl() {
        writeC(228);
        writeC(this._hennaList.getStat(HennaType.INT));
        writeC(this._hennaList.getStat(HennaType.STR));
        writeC(this._hennaList.getStat(HennaType.CON));
        writeC(this._hennaList.getStat(HennaType.MEN));
        writeC(this._hennaList.getStat(HennaType.DEX));
        writeC(this._hennaList.getStat(HennaType.WIT));
        writeD(this._hennaList.getMaxSize());
        List<Henna> hennas = this._hennaList.getHennas();
        writeD(hennas.size());
        for (Henna h : hennas) {
            writeD(h.getSymbolId());
            writeD(this._hennaList.canBeUsedBy(h) ? h.getSymbolId() : 0);
        }
    }
}
