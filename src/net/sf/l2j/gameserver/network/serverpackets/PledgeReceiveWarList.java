package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.Set;

public class PledgeReceiveWarList extends L2GameServerPacket {
    private final Set<Integer> _clanList;

    private final int _tab;

    private final int _page;

    public PledgeReceiveWarList(Set<Integer> clanList, int tab, int page) {
        this._clanList = clanList;
        this._tab = tab;
        this._page = page;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(62);
        writeD(this._tab);
        writeD(this._page);
        writeD((this._tab == 0) ? this._clanList.size() : ((this._page == 0) ? (Math.min(this._clanList.size(), 13)) : (this._clanList.size() % 13 * this._page)));
        int index = 0;
        for (int clanId : this._clanList) {
            Clan clan = ClanTable.getInstance().getClan(clanId);
            if (clan == null)
                continue;
            if (this._tab != 0) {
                if (index < this._page * 13) {
                    index++;
                    continue;
                }
                if (index == (this._page + 1) * 13)
                    break;
                index++;
            }
            writeS(clan.getName());
            writeD(this._tab);
            writeD(this._page);
        }
    }
}
