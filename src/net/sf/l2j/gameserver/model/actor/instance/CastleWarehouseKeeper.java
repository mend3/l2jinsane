package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleWarehouseKeeper extends WarehouseKeeper {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;

    public CastleWarehouseKeeper(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void showChatWindow(Player player, int val) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/castlewarehouse/castlewarehouse-no.htm";
        int condition = this.validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "data/html/castlewarehouse/castlewarehouse-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "data/html/castlewarehouse/castlewarehouse.htm";
                } else {
                    filename = "data/html/castlewarehouse/castlewarehouse-" + val + ".htm";
                }
            }
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        html.replace("%npcname%", this.getName());
        player.sendPacket(html);
    }

    protected int validateCondition(Player player) {
        if (this.getCastle() != null && player.getClan() != null) {
            if (this.getCastle().getSiege().isInProgress()) {
                return 1;
            }

            if (this.getCastle().getOwnerId() == player.getClanId()) {
                return 2;
            }
        }

        return 0;
    }
}
