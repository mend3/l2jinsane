package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public class Doorman extends Folk {
    public Doorman(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("open_doors")) {
            if (isOwnerClan(player))
                if (isUnderSiege()) {
                    cannotManageDoors(player);
                    player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
                } else {
                    openDoors(player, command);
                }
        } else if (command.startsWith("close_doors")) {
            if (isOwnerClan(player))
                if (isUnderSiege()) {
                    cannotManageDoors(player);
                    player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
                } else {
                    closeDoors(player, command);
                }
        } else if (command.startsWith("tele")) {
            if (isOwnerClan(player))
                doTeleport(player, command);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/doormen/" + getTemplate().getNpcId() + (!isOwnerClan(player) ? "-no.htm" : ".htm"));
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    protected void openDoors(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
        st.nextToken();
        while (st.hasMoreTokens())
            DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
    }

    protected void closeDoors(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
        st.nextToken();
        while (st.hasMoreTokens())
            DoorData.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
    }

    protected void cannotManageDoors(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/doormen/busy.htm");
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    protected void doTeleport(Player player, String command) {
        TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(command.substring(5).trim()));
        if (list != null && !player.isAlikeDead())
            player.teleportTo(list, 0);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    protected boolean isOwnerClan(Player player) {
        return true;
    }

    protected boolean isUnderSiege() {
        return false;
    }
}
