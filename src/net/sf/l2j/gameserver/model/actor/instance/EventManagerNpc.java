package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class EventManagerNpc extends Folk {
    public EventManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static String getActiveEvent() {
        String s = "---";
        if (TvTEventManager.getInstance().getActiveEvent() != null) {
            s = TvTEventManager.getInstance().getActiveEvent().getName();
        }

        if (CtfEventManager.getInstance().getActiveEvent() != null) {
            s = CtfEventManager.getInstance().getActiveEvent().getName();
        }

        if (DmEventManager.getInstance().getActiveEvent() != null) {
            s = DmEventManager.getInstance().getActiveEvent().getName();
        }

        return s;
    }

    private static int getCurrentReg() {
        int i = 0;
        if (TvTEventManager.getInstance().getActiveEvent() != null) {
            i = TvTEventManager.getInstance().getTotalParticipants();
        }

        if (CtfEventManager.getInstance().getActiveEvent() != null) {
            i = CtfEventManager.getInstance().getTotalParticipants();
        }

        if (DmEventManager.getInstance().getActiveEvent() != null) {
            i = DmEventManager.getInstance().getTotalParticipants();
        }

        return i;
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.equals("register")) {
            if (TvTEventManager.getInstance().getActiveEvent() != null) {
                TvTEventManager.getInstance().registerPlayer(player);
            }

            if (CtfEventManager.getInstance().getActiveEvent() != null) {
                CtfEventManager.getInstance().registerPlayer(player);
            }

            if (DmEventManager.getInstance().getActiveEvent() != null) {
                DmEventManager.getInstance().registerPlayer(player);
            }
        } else if (command.equals("remove")) {
            if (TvTEventManager.getInstance().getActiveEvent() != null) {
                TvTEventManager.getInstance().removePlayer(player);
            }

            if (CtfEventManager.getInstance().getActiveEvent() != null) {
                CtfEventManager.getInstance().removePlayer(player);
            }

            if (DmEventManager.getInstance().getActiveEvent() != null) {
                DmEventManager.getInstance().removePlayer(player);
            }
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void showChatWindow(Player player, int val) {
        NpcHtmlMessage htm = new NpcHtmlMessage(this.getObjectId());
        int var10001 = this.getNpcId();
        htm.setFile("data/html/mods/eventmanager/" + var10001 + (val == 0 ? "" : "-" + val) + ".htm");
        htm.replace("%activeevent%", getActiveEvent());
        htm.replace("%currentreg%", getCurrentReg());
        htm.replace("%objectId%", "" + this.getObjectId());
        player.sendPacket(htm);
    }

    public String getHtmlPath(int npcId, int val) {
        String pom = "";
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }

        return "data/html/mods/eventmanager/" + pom + ".htm";
    }
}
