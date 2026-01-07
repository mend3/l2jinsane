package net.sf.l2j.gameserver.communitybbs.Custom;

import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

import java.util.Calendar;
import java.util.StringTokenizer;

public class TeleportBBSManager extends BaseBBSManager {
    public static TeleportBBSManager getInstance() {
        return SingletonHolder._instance;
    }

    public void parseCmd(String command, Player player) {
        if (player.getPvpFlag() > 0) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are pvp flagged.</center></body></html>", player);
            player.sendMessage("You can't use teleport when you are pvp flagged.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInCombat()) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are in combat.</center></body></html>", player);
            player.sendMessage("You can't use teleport when you are in combat.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isDead()) {
            separateAndSend("<html><body><br><br><center>You're dead. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're dead. You can't use teleport.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!player.isInsideZone(ZoneId.PEACE)) {
            separateAndSend("<html><body><br><br><center>You're not in Peace Zone. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're not in Peace Zone. You can't use teleport.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (command.equals("_bbsteleport")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
            separateAndSend(html, player);
        } else if (command.startsWith("_bbsteleport;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String actualCommand = st.nextToken();
            if (actualCommand.equalsIgnoreCase("goto")) {
                TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(st.nextToken()));
                if (list == null)
                    return;
                Siege siegeOnTeleportLocation = CastleManager.getInstance().getActiveSiege(list.getX(), list.getY(), list.getZ());
                if (siegeOnTeleportLocation != null && siegeOnTeleportLocation.isInProgress()) {
                    player.sendMessage("You can't use teleport during Siege.");
                    return;
                }
                if (player.getKarma() > 0) {
                    player.sendMessage("Go away, you're not welcome here.");
                    return;
                }
                if (list.isNoble() && !player.isNoble()) {
                    player.sendMessage("You're not Noble, you can't use this teleport.");
                    return;
                }
                Calendar cal = Calendar.getInstance();
                int price = list.getPrice();
                if (!list.isNoble() &&
                        cal.get(11) >= 20 && cal.get(11) <= 23 && (cal.get(7) == 1 || cal.get(7) == 7))
                    price /= 2;
                if (player.destroyItemByItemId("Teleport ", list.isNoble() ? 6651 : 57, price, player, true))
                    player.teleportTo(list, 100);
            } else if (actualCommand.startsWith("tele_tournament")) {
                if (ArenaTask.is_started()) {
                    player.teleportTo(Config.NPC_locx + Rnd.get(-100, 100), Config.NPC_locy + Rnd.get(-100, 100), Config.NPC_locz, 20);
                    player.setTournamentTeleport(true);
                } else {
                    player.sendMessage("Tournamemt Event not started yet");
                }
            } else if (actualCommand.startsWith("tele_pvpevent")) {
                if (PvPEvent.getInstance().isActive()) {
                    player.teleportTo(152360 + Rnd.get(-100, 100), -122088 + Rnd.get(-100, 100), -2376, 100);
                } else {
                    player.sendMessage("PvP Event not started yet");
                }
            } else if (actualCommand.startsWith("pvpzone")) {
                if (RandomZoneManager.getInstance().getCurrentZone() != null)
                    player.teleportTo(RandomZoneManager.getInstance().getCurrentZone().getLoc(), 200);
            } else if (actualCommand.startsWith("chat")) {
                String html;
                int page = Integer.valueOf(st.nextToken());
                if (page == 0) {
                    html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
                } else {
                    html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index-" + page + ".htm");
                }
                separateAndSend(html, player);
            }
        } else {
            super.parseCmd(command, player);
        }
    }

    protected String getFolder() {
        return "top/teleport/";
    }

    private static class SingletonHolder {
        protected static final TeleportBBSManager _instance = new TeleportBBSManager();
    }
}
