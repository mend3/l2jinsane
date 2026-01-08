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
        int condition = this.validateCondition(player);
        if (condition >= 2) {
            if (command.startsWith("back")) {
                this.showChatWindow(player);
            } else if (command.startsWith("how_to")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/mercmanager/mseller005.htm");
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (command.startsWith("hire")) {
                if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setFile("data/html/mercmanager/msellerdenial.htm");
                    html.replace("%objectId%", this.getObjectId());
                    player.sendPacket(html);
                    return;
                }

                StringTokenizer st = new StringTokenizer(command, " ");
                st.nextToken();
                BuyListManager var10000 = BuyListManager.getInstance();
                int var10001 = this.getNpcId();
                NpcBuyList buyList = var10000.getBuyList(Integer.parseInt(var10001 + st.nextToken()));
                if (buyList == null || !buyList.isNpcAllowed(this.getNpcId())) {
                    return;
                }

                player.tempInventoryDisable();
                player.sendPacket(new BuyList(buyList, player.getAdena(), 0.0F));
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/mercmanager/mseller004.htm");
                player.sendPacket(html);
            } else if (command.startsWith("merc_limit")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                int var10 = this.getCastle().getCastleId();
                html.setFile("data/html/mercmanager/" + (var10 == 5 ? "aden_msellerLimit.htm" : "msellerLimit.htm"));
                html.replace("%castleName%", this.getCastle().getName());
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }

    public void showChatWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        int condition = this.validateCondition(player);
        if (condition == 0) {
            html.setFile("data/html/mercmanager/mseller002.htm");
        } else if (condition == 1) {
            html.setFile("data/html/mercmanager/mseller003.htm");
        } else if (condition == 2) {
            switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
                case DAWN -> html.setFile("data/html/mercmanager/mseller001_dawn.htm");
                case DUSK -> html.setFile("data/html/mercmanager/mseller001_dusk.htm");
                default -> html.setFile("data/html/mercmanager/mseller001.htm");
            }
        }

        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    private int validateCondition(Player player) {
        if (this.getCastle() != null && player.getClan() != null) {
            if (this.getCastle().getSiege().isInProgress()) {
                return 1;
            }

            if (this.getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 2097152) == 2097152) {
                return 2;
            }
        }

        return 0;
    }
}
