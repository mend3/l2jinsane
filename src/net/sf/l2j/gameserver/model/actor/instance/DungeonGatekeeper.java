package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public class DungeonGatekeeper extends Folk {
    public DungeonGatekeeper(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static void doTeleport(Player player, int val) {
        TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(val);
        if (list != null && !player.isAlikeDead())
            player.teleportTo(list, 20);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onBypassFeedback(Player player, String command) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        CabalType sealAvariceOwner = SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE);
        CabalType sealGnosisOwner = SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS);
        CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
        CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
        if (actualCommand.startsWith("necro")) {
            boolean canPort = true;
            if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                if (winningCabal == CabalType.DAWN && (playerCabal != CabalType.DAWN || sealAvariceOwner != CabalType.DAWN)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                    canPort = false;
                } else if (winningCabal == CabalType.DUSK && (playerCabal != CabalType.DUSK || sealAvariceOwner != CabalType.DUSK)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                    canPort = false;
                } else if (winningCabal == CabalType.NORMAL && playerCabal != CabalType.NORMAL) {
                    canPort = true;
                } else if (playerCabal == CabalType.NORMAL) {
                    canPort = false;
                }
            } else if (playerCabal == CabalType.NORMAL) {
                canPort = false;
            }
            if (!canPort) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/seven_signs/necro_no.htm");
                player.sendPacket(html);
            } else {
                doTeleport(player, Integer.parseInt(st.nextToken()));
                player.setIsIn7sDungeon(true);
            }
        } else if (actualCommand.startsWith("cata")) {
            boolean canPort = true;
            if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                if (winningCabal == CabalType.DAWN && (playerCabal != CabalType.DAWN || sealGnosisOwner != CabalType.DAWN)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                    canPort = false;
                } else if (winningCabal == CabalType.DUSK && (playerCabal != CabalType.DUSK || sealGnosisOwner != CabalType.DUSK)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                    canPort = false;
                } else if (winningCabal == CabalType.NORMAL && playerCabal != CabalType.NORMAL) {
                    canPort = true;
                } else if (playerCabal == CabalType.NORMAL) {
                    canPort = false;
                }
            } else if (playerCabal == CabalType.NORMAL) {
                canPort = false;
            }
            if (!canPort) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/seven_signs/cata_no.htm");
                player.sendPacket(html);
            } else {
                doTeleport(player, Integer.parseInt(st.nextToken()));
                player.setIsIn7sDungeon(true);
            }
        } else if (actualCommand.startsWith("exit")) {
            doTeleport(player, Integer.parseInt(st.nextToken()));
            player.setIsIn7sDungeon(false);
        } else if (actualCommand.startsWith("goto")) {
            doTeleport(player, Integer.parseInt(st.nextToken()));
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/teleporter/" + filename + ".htm";
    }
}
