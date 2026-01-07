package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public final class MercenaryManagerNpc extends Folk {
    private static final int COND_ALL_FALSE = 0;

    private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;

    private static final int COND_OWNER = 2;

    public MercenaryManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        int condition = validateCondition(player);
        if (condition < 2)
            return;
        if (command.startsWith("back")) {
            showChatWindow(player);
        } else if (command.startsWith("how_to")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/mercmanager/mseller005.htm");
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (command.startsWith("hire")) {
            if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
                npcHtmlMessage.setFile("data/html/mercmanager/msellerdenial.htm");
                npcHtmlMessage.replace("%objectId%", getObjectId());
                player.sendPacket(npcHtmlMessage);
                return;
            }
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            NpcBuyList buyList = BuyListManager.getInstance().getBuyList(Integer.parseInt("" + getNpcId() + getNpcId()));
            if (buyList == null || !buyList.isNpcAllowed(getNpcId()))
                return;
            player.tempInventoryDisable();
            player.sendPacket(new BuyList(buyList, player.getAdena(), 0.0D));
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/mercmanager/mseller004.htm");
            player.sendPacket(html);
        } else if (command.startsWith("merc_limit")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/mercmanager/" + ((getCastle().getCastleId() == 5) ? "aden_msellerLimit.htm" : "msellerLimit.htm"));
            html.replace("%castleName%", getCastle().getName());
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        int condition = validateCondition(player);
        if (condition == 0) {
            html.setFile("data/html/mercmanager/mseller002.htm");
        } else if (condition == 1) {
            html.setFile("data/html/mercmanager/mseller003.htm");
        } else if (condition == 2) {
            switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
                case DAWN:
                    html.setFile("data/html/mercmanager/mseller001_dawn.htm");
                    break;
                case DUSK:
                    html.setFile("data/html/mercmanager/mseller001_dusk.htm");
                    break;
                default:
                    html.setFile("data/html/mercmanager/mseller001.htm");
                    break;
            }
        }
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }

    private int validateCondition(Player player) {
        if (getCastle() != null && player.getClan() != null) {
            if (getCastle().getSiege().isInProgress())
                return 1;
            if (getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x200000) == 2097152)
                return 2;
        }
        return 0;
    }
}
