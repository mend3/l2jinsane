package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.StringTokenizer;

public class ManorManagerNpc extends Merchant {
    public ManorManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("manor_menu_select")) {
            if (CastleManorManager.getInstance().isUnderMaintenance()) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
                return;
            }
            StringTokenizer st = new StringTokenizer(command, "&");
            int ask = Integer.parseInt(st.nextToken().split("=")[1]);
            int state = Integer.parseInt(st.nextToken().split("=")[1]);
            boolean time = st.nextToken().split("=")[1].equals("1");
            int castleId = (state < 0) ? getCastle().getCastleId() : state;
            switch (ask) {
                case 1:
                    if (castleId != getCastle().getCastleId()) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addString(getCastle().getName()));
                        break;
                    }
                    player.sendPacket(new BuyListSeed(player.getAdena(), castleId));
                    break;
                case 2:
                    player.sendPacket(new ExShowSellCropList(player.getInventory(), castleId));
                    break;
                case 3:
                    player.sendPacket(new ExShowSeedInfo(castleId, time, false));
                    break;
                case 4:
                    player.sendPacket(new ExShowCropInfo(castleId, time, false));
                    break;
                case 5:
                    player.sendPacket(new ExShowManorDefaultInfo(false));
                    break;
                case 6:
                    showBuyWindow(player, 300000 + getNpcId());
                    break;
                case 9:
                    player.sendPacket(new ExShowProcureCropDetail(state));
                    break;
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath(int npcId, int val) {
        return "data/html/manormanager/manager.htm";
    }

    public void showChatWindow(Player player) {
        if (!Config.ALLOW_MANOR) {
            showChatWindow(player, "data/html/npcdefault.htm");
            return;
        }
        if (getCastle() != null && player.getClan() != null && getCastle().getOwnerId() == player.getClanId() && player.isClanLeader()) {
            showChatWindow(player, "data/html/manormanager/manager-lord.htm");
        } else {
            showChatWindow(player, "data/html/manormanager/manager.htm");
        }
    }
}
