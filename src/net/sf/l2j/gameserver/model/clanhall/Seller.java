package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;

public class Seller {
    private final String _name;

    private final String _clanName;

    private int _bid;

    public Seller(String name, String clanName, int bid) {
        this._name = name;
        this._clanName = clanName;
        this._bid = bid;
    }

    public String getName() {
        return this._name;
    }

    public String getClanName() {
        return this._clanName;
    }

    public int getBid() {
        return this._bid;
    }

    public void setBid(int bid) {
        this._bid = bid;
    }

    public Clan getClan() {
        return ClanTable.getInstance().getClanByName(this._clanName);
    }
}
