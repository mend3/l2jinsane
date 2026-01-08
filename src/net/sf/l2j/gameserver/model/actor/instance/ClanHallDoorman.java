package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanHallDoorman extends Doorman {
    private ClanHall _clanHall;

    public ClanHallDoorman(int objectID, NpcTemplate template) {
        super(objectID, template);
    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        if (this._clanHall != null) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            Clan owner = ClanTable.getInstance().getClan(this._clanHall.getOwnerId());
            if (this.isOwnerClan(player)) {
                html.setFile("data/html/clanHallDoormen/doormen.htm");
                html.replace("%clanname%", owner.getName());
            } else if (owner != null && owner.getLeader() != null) {
                html.setFile("data/html/clanHallDoormen/doormen-no.htm");
                html.replace("%leadername%", owner.getLeaderName());
                html.replace("%clanname%", owner.getName());
            } else {
                html.setFile("data/html/clanHallDoormen/emptyowner.htm");
                html.replace("%hallname%", this._clanHall.getName());
            }

            html.replace("%objectId%", this.getObjectId());
            player.sendPacket(html);
        }
    }

    public void showChatWindow(Player player, int val) {
        this.showChatWindow(player);
    }

    protected final void openDoors(Player player, String command) {
        this._clanHall.openCloseDoors(true);
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/clanHallDoormen/doormen-opened.htm");
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    protected final void closeDoors(Player player, String command) {
        this._clanHall.openCloseDoors(false);
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/clanHallDoormen/doormen-closed.htm");
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    protected final boolean isOwnerClan(Player player) {
        return this._clanHall != null && player.getClan() != null && player.getClanId() == this._clanHall.getOwnerId();
    }

    public void onSpawn() {
        this._clanHall = ClanHallManager.getInstance().getNearestClanHall(this.getX(), this.getY(), 500);
        if (this._clanHall == null) {
            super.onSpawn();
        }

    }
}
