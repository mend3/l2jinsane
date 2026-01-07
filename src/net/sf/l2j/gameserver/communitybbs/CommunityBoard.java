package net.sf.l2j.gameserver.communitybbs;

import enginemods.main.EngineModsManager;
import mods.balancer.ClassBalanceGui;
import mods.balancer.SkillBalanceGui;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Custom.*;
import net.sf.l2j.gameserver.communitybbs.Manager.*;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class CommunityBoard {
    public static CommunityBoard getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void handleCommands(GameClient client, String command) {
        Player player = client.getPlayer();
        if (player == null)
            return;
        if (EngineModsManager.onCommunityBoard(player, command))
            return;
        if (!Config.ENABLE_COMMUNITY_BOARD) {
            player.sendPacket(SystemMessageId.CB_OFFLINE);
            return;
        }
        if (player.isGM() && command.contains("balance")) {
            if (command.contains("skillbalance")) {
                SkillBalanceGui.getInstance().parseCmd(command, player);
            } else if (command.contains("classbalance") || command.equals("_bbs_balancer")) {
                ClassBalanceGui.getInstance().parseCmd(command, player);
            }
            return;
        }
        if (player.getPvpFlag() > 0) {
            player.sendMessage("You can't use Community Board when you are pvp flagged.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInCombat()) {
            player.sendMessage("You can't use Community Board when you are in combat.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isDead()) {
            player.sendMessage("You're dead. You can't use Community Board.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!player.isInsideZone(ZoneId.PEACE)) {
            player.sendMessage("You're not in Peace Zone. You can't use Community Board.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (command.startsWith("_bbshome")) {
            TopBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsloc")) {
            InfoBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsclan") || command.startsWith("_bbsupgrade")) {
            UpgradeBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsmemo") || command.startsWith("_bbsWH")) {
            WarehouseBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsmail") || command.equals("_maillist_0_1_0_") || command.startsWith("_bbsdonate")) {
            DonateBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_friend") || command.startsWith("_block")) {
            RaidBossInfoBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbspag")) {
            OpenPagBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsshop")) {
            ShopBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsbuffer")) {
            BufferBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsteleport")) {
            TeleportBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsgetfav") || command.startsWith("_bbsranking")) {
            RankingBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsRB")) {
            RaidBossInfoBBSManager.getInstance().parseCmd(command, player);
        } else if (command.startsWith("_bbsInfo")) {
            InfoBBSManager.getInstance().parseCmd(command, player);
        } else {
            BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + command + " isn't implemented.</center></body></html>", player);
        }
    }

    public void handleWriteCommands(GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5) {
        Player player = client.getPlayer();
        if (player == null)
            return;
        if (!Config.ENABLE_COMMUNITY_BOARD) {
            player.sendPacket(SystemMessageId.CB_OFFLINE);
            return;
        }
        if (url.equals("Topic")) {
            TopicBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else if (url.equals("Post")) {
            PostBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else if (url.equals("_bbsloc")) {
            RegionBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else if (url.equals("_bbsclan")) {
            ClanBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else if (url.equals("Mail")) {
            MailBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else if (url.equals("_friend")) {
            FriendsBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
        } else {
            BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + url + " isn't implemented.</center></body></html>", player);
        }
    }

    private static class SingletonHolder {
        protected static final CommunityBoard INSTANCE = new CommunityBoard();
    }
}
