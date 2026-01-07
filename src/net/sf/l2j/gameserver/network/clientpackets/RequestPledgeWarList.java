package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveWarList;

import java.util.Set;

public final class RequestPledgeWarList extends L2GameClientPacket {
    private int _page;

    private int _tab;

    protected void readImpl() {
        this._page = readD();
        this._tab = readD();
    }

    protected void runImpl() {
        Set<Integer> list;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Clan clan = player.getClan();
        if (clan == null)
            return;
        if (this._tab == 0) {
            list = clan.getWarList();
        } else {
            list = clan.getAttackerList();
            this._page = Math.max(0, (this._page > list.size() / 13) ? 0 : this._page);
        }
        player.sendPacket(new PledgeReceiveWarList(list, this._tab, this._page));
    }
}
