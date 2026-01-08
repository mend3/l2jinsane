package net.sf.l2j.gameserver.model.actor.instance;

import mods.dungeon.DungeonManager;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public class DungeonManagerNpc extends Folk {
    public DungeonManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public static String getPlayerStatus(Player player, int dungeonId) {
        String s = "You can enter";
        String ip = player.getHWID();
        if (DungeonManager.getInstance().getPlayerData().containsKey(ip) && ((Long[]) DungeonManager.getInstance().getPlayerData().get(ip))[dungeonId] > 0L) {
            long total = ((Long[]) DungeonManager.getInstance().getPlayerData().get(ip))[dungeonId] + 43200000L - System.currentTimeMillis();
            if (total > 0L) {
                int hours = (int) (total / 1000L / 60L / 60L);
                int minutes = (int) (total / 1000L / 60L - (long) (hours * 60));
                int seconds = (int) (total / 1000L - (long) (hours * 60 * 60 + minutes * 60));
                s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }

        return s;
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("dungeon")) {
            if (DungeonManager.getInstance().isInDungeon(player) || player.isInOlympiadMode() || TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player) || CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player) || DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
                player.sendMessage("You are currently unable to enter a Dungeon. Please try again later.");
                return;
            }

            int dungeonId = Integer.parseInt(command.substring(8));
            int dungeonCount = DungeonManager.getInstance().getDungeonsCount();
            if (dungeonCount <= 0 || dungeonId > dungeonCount) {
                player.sendMessage("Dungeon doesn't exists.");
                LOGGER.warn("Dungeon:" + dungeonId + " doesn't exists.");
                return;
            }

            DungeonManager.getInstance().enterDungeon(dungeonId, player);
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void showChatWindow(Player player, int val) {
        NpcHtmlMessage htm = new NpcHtmlMessage(this.getObjectId());
        int var10001 = this.getNpcId();
        htm.setFile("data/html/mods/dungeon/" + var10001 + (val == 0 ? "" : "-" + val) + ".htm");
        String[] s = htm.getHtml().split("%");

        for (int i = 0; i < s.length; ++i) {
            if (i % 2 > 0 && s[i].contains("dung ")) {
                StringTokenizer st = new StringTokenizer(s[i]);
                st.nextToken();
                htm.replace("%" + s[i] + "%", getPlayerStatus(player, Integer.parseInt(st.nextToken())));
            }
        }

        htm.replace("%objectId%", "" + this.getObjectId());
        player.sendPacket(htm);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }

        return "data/html/mods/dungeon/" + filename + ".htm";
    }
}
