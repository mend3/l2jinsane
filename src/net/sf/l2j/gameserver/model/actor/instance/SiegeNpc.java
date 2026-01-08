package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;

public class SiegeNpc extends Folk {
    public SiegeNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void showChatWindow(Player player) {
        if (!this.getCastle().getSiege().isInProgress()) {
            player.sendPacket(new SiegeInfo(this.getCastle()));
        } else {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/siege/" + this.getNpcId() + "-busy.htm");
            html.replace("%castlename%", this.getCastle().getName());
            html.replace("%objectId%", this.getObjectId());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }
}
