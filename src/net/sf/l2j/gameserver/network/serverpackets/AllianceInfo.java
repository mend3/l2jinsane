package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanInfo;

import java.util.Collection;

public class AllianceInfo extends L2GameServerPacket {
    private final String _name;

    private final int _total;

    private final int _online;

    private final String _leaderC;

    private final String _leaderP;

    private final ClanInfo[] _allies;

    public AllianceInfo(int allianceId) {
        Clan leader = ClanTable.getInstance().getClan(allianceId);
        this._name = leader.getAllyName();
        this._leaderC = leader.getName();
        this._leaderP = leader.getLeaderName();
        Collection<Clan> allies = ClanTable.getInstance().getClanAllies(allianceId);
        this._allies = new ClanInfo[allies.size()];
        int idx = 0, total = 0, online = 0;
        for (Clan clan : allies) {
            ClanInfo ci = new ClanInfo(clan);
            this._allies[idx++] = ci;
            total += ci.getTotal();
            online += ci.getOnline();
        }
        this._total = total;
        this._online = online;
    }

    protected void writeImpl() {
        writeC(180);
        writeS(this._name);
        writeD(this._total);
        writeD(this._online);
        writeS(this._leaderC);
        writeS(this._leaderP);
        writeD(this._allies.length);
        for (ClanInfo aci : this._allies) {
            writeS(aci.getClan().getName());
            writeD(0);
            writeD(aci.getClan().getLevel());
            writeS(aci.getClan().getLeaderName());
            writeD(aci.getTotal());
            writeD(aci.getOnline());
        }
    }

    public String getName() {
        return this._name;
    }

    public int getTotal() {
        return this._total;
    }

    public int getOnline() {
        return this._online;
    }

    public String getLeaderC() {
        return this._leaderC;
    }

    public String getLeaderP() {
        return this._leaderP;
    }

    public ClanInfo[] getAllies() {
        return this._allies;
    }
}
