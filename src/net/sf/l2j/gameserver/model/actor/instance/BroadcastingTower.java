package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

import java.util.StringTokenizer;

public final class BroadcastingTower extends Folk {
    public BroadcastingTower(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("observe")) {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            int cost = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            if (command.startsWith("observeSiege") && CastleManager.getInstance().getActiveSiege(x, y, z) == null) {
                player.sendPacket(SystemMessageId.ONLY_VIEW_SIEGE);
                return;
            }

            if (player.reduceAdena("Broadcast", cost, this, true)) {
                player.enterObserverMode(x, y, z);
                player.sendPacket(new ItemList(player, false));
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }
        return "data/html/observation/" + filename + ".htm";
    }
}
