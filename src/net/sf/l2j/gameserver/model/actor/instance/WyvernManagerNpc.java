package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class WyvernManagerNpc extends CastleChamberlain {
    public WyvernManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (player.getCurrentFolk() == null || player.getCurrentFolk().getObjectId() != getObjectId())
            return;
        if (command.startsWith("RideWyvern")) {
            String val = "2";
            if (player.isClanLeader())
                if (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE) == CabalType.DUSK) {
                    val = "3";
                } else if (player.isMounted() && (player.getMountNpcId() == 12526 || player.getMountNpcId() == 12527 || player.getMountNpcId() == 12528)) {
                    if (player.getMountLevel() < Config.WYVERN_REQUIRED_LEVEL) {
                        val = "6";
                    } else if (player.destroyItemByItemId("Wyvern", 1460, Config.WYVERN_REQUIRED_CRYSTALS, player, true)) {
                        player.dismount();
                        if (player.mount(12621, 0))
                            val = "4";
                    } else {
                        val = "5";
                    }
                } else {
                    player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
                    val = "1";
                }
            sendHtm(player, val);
        } else if (command.startsWith("Chat")) {
            String val = "1";
            try {
                val = command.substring(5);
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            }
            sendHtm(player, val);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player) {
        String val = "0a";
        int condition = validateCondition(player);
        if (condition > 0)
            if (condition == 2) {
                if (player.isFlying()) {
                    val = "4";
                } else {
                    val = "0";
                }
            } else if (condition == 3) {
                val = "2";
            }
        sendHtm(player, val);
    }

    private void sendHtm(Player player, String val) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/wyvernmanager/wyvernmanager-" + val + ".htm");
        html.replace("%objectId%", getObjectId());
        html.replace("%npcname%", getName());
        html.replace("%wyvern_level%", Config.WYVERN_REQUIRED_LEVEL);
        html.replace("%needed_crystals%", Config.WYVERN_REQUIRED_CRYSTALS);
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
