package net.sf.l2j.gameserver.model.actor.instance;

import mods.autofarm.AutofarmPlayerRoutine;
import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Calendar;
import java.util.StringTokenizer;

public final class Gatekeeper extends Folk {
    public Gatekeeper(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }
        return "data/html/teleporter/" + filename + ".htm";
    }

    public void onBypassFeedback(Player player, String command) {
        AutofarmPlayerRoutine bot = player.getBot();
        if (!Config.KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0 && showPkDenyChatWindow(player, "teleporter"))
            return;
        if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot leave the event.");
            return;
        }
        if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot leave the event.");
            return;
        }
        if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            player.sendMessage("You cannot leave the event.");
            return;
        }
        if (command.startsWith("goto")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (!st.hasMoreTokens())
                return;
            if (!canInteract(player))
                return;
            TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(st.nextToken()));
            if (list == null)
                return;
            if (CastleManager.getInstance().getActiveSiege(list.getX(), list.getY(), list.getZ()) != null) {
                player.sendPacket(SystemMessageId.CANNOT_PORT_VILLAGE_IN_SIEGE);
                return;
            }
            if (list.isNoble() && !player.isNoble()) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/teleporter/nobleteleporter-no.htm");
                html.replace("%objectId%", getObjectId());
                html.replace("%npcname%", getName());
                player.sendPacket(html);
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            int price = list.getPrice();
            if (!list.isNoble()) {
                Calendar cal = Calendar.getInstance();
                if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))
                    price /= 2;
            }
            if (player.destroyItemByItemId("Teleport ", list.isNoble() ? 6651 : 57, price, this, true))
                player.teleportTo(list, 20);
            if (player.isAutoFarm()) {
                bot.stop();
                player.setAutoFarm(false);
            }
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (command.startsWith("Chat")) {
            int val = 0;
            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            } catch (NumberFormatException numberFormatException) {
            }
            if (val == 1) {
                Calendar cal = Calendar.getInstance();
                if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    String content = HtmCache.getInstance().getHtm("data/html/teleporter/half/" + getNpcId() + ".htm");
                    if (content == null)
                        content = HtmCache.getInstance().getHtmForce("data/html/teleporter/" + getNpcId() + "-1.htm");
                    html.setHtml(content);
                    html.replace("%objectId%", getObjectId());
                    html.replace("%npcname%", getName());
                    player.sendPacket(html);
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }
            }
            showChatWindow(player, val);
        } else if (command.startsWith("tele_tournament")) {
            if (ArenaTask.is_started()) {
                player.teleportTo(Config.NPC_locx + Rnd.get(-100, 100), Config.NPC_locy + Rnd.get(-100, 100), Config.NPC_locz, 20);
                player.setTournamentTeleport(true);
            } else {
                player.sendMessage("Tournamemt Event not started yet");
            }
        } else if (command.startsWith("tele_pvpevent")) {
            if (PvPEvent.getInstance().isActive()) {
                player.teleportTo(152360 + Rnd.get(-100, 100), -122088 + Rnd.get(-100, 100), -2376, 100);
            } else {
                player.sendMessage("PvP Event not started yet");
            }
        } else if (command.startsWith("pvpzone")) {
            if (RandomZoneManager.getInstance().getCurrentZone() != null)
                player.teleportTo(RandomZoneManager.getInstance().getCurrentZone().getLoc(), 20);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player, int val) {
        if (!Config.KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0 && showPkDenyChatWindow(player, "teleporter"))
            return;
        showChatWindow(player, getHtmlPath(getNpcId(), val));
    }
}
