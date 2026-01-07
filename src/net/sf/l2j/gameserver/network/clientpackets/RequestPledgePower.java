package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends L2GameClientPacket {
    private int _rank;

    private int _action;

    private int _privs;

    protected void readImpl() {
        this._rank = readD();
        this._action = readD();
        if (this._action == 2) {
            this._privs = readD();
        } else {
            this._privs = 0;
        }
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Clan clan = player.getClan();
        if (clan == null)
            return;
        if (this._action == 2) {
            if (player.isClanLeader()) {
                if (this._rank == 9)
                    this._privs = (this._privs & 0x8) + (this._privs & 0x400) + (this._privs & 0x8000);
                player.getClan().setPriviledgesForRank(this._rank, this._privs);
            }
        } else {
            player.sendPacket(new ManagePledgePower(clan, this._action, this._rank));
        }
    }
}
