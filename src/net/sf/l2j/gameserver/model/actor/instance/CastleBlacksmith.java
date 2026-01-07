package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleBlacksmith extends Folk {
    protected static final int COND_ALL_FALSE = 0;

    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;

    protected static final int COND_OWNER = 2;

    public CastleBlacksmith(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (!Config.ALLOW_MANOR) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/npcdefault.htm");
            html.replace("%objectId%", getObjectId());
            html.replace("%npcname%", getName());
            player.sendPacket(html);
            return;
        }
        if (validateCondition(player) != 2)
            return;
        if (command.startsWith("Chat")) {
            int val = 0;
            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            } catch (NumberFormatException numberFormatException) {
            }
            showChatWindow(player, val);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player, int val) {
        if (!Config.ALLOW_MANOR) {
            NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
            npcHtmlMessage.setFile("data/html/npcdefault.htm");
            npcHtmlMessage.replace("%objectId%", getObjectId());
            npcHtmlMessage.replace("%npcname%", getName());
            player.sendPacket(npcHtmlMessage);
            return;
        }
        String filename = "data/html/castleblacksmith/castleblacksmith-no.htm";
        int condition = validateCondition(player);
        if (condition > 0)
            if (condition == 1) {
                filename = "data/html/castleblacksmith/castleblacksmith-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "data/html/castleblacksmith/castleblacksmith.htm";
                } else {
                    filename = "data/html/castleblacksmith/castleblacksmith-" + val + ".htm";
                }
            }
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        html.replace("%npcname%", getName());
        html.replace("%castleid%", getCastle().getCastleId());
        player.sendPacket(html);
    }

    protected int validateCondition(Player player) {
        if (getCastle() != null && player.getClan() != null) {
            if (getCastle().getSiege().isInProgress())
                return 1;
            if (getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x10000) == 65536)
                return 2;
        }
        return 0;
    }
}
