package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.data.xml.MultisellData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SellList;
import net.sf.l2j.gameserver.network.serverpackets.ShopPreviewList;

import java.util.List;
import java.util.StringTokenizer;

public class Merchant extends Folk {
    public Merchant(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }

        return "data/html/merchant/" + filename + ".htm";
    }

    public void onBypassFeedback(Player player, String command) {
        if (Config.KARMA_PLAYER_CAN_SHOP || player.getKarma() <= 0 || !this.showPkDenyChatWindow(player, "merchant")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken();
            if (actualCommand.equalsIgnoreCase("Buy")) {
                if (st.countTokens() < 1) {
                    return;
                }

                this.showBuyWindow(player, Integer.parseInt(st.nextToken()));
            } else if (actualCommand.equalsIgnoreCase("Sell")) {
                List<ItemInstance> items = player.getInventory().getSellableItems();
                if (items.isEmpty()) {
                    String content = HtmCache.getInstance().getHtm("data/html/" + (this instanceof Fisherman ? "fisherman" : "merchant") + "/" + this.getNpcId() + "-empty.htm");
                    if (content != null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setHtml(content);
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                        return;
                    }
                }

                player.sendPacket(new SellList(player.getAdena(), items));
            } else if (actualCommand.equalsIgnoreCase("Wear") && Config.ALLOW_WEAR) {
                if (st.countTokens() < 1) {
                    return;
                }

                this.showWearWindow(player, Integer.parseInt(st.nextToken()));
            } else if (actualCommand.equalsIgnoreCase("Multisell")) {
                if (st.countTokens() < 1) {
                    return;
                }

                MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, false);
            } else if (actualCommand.equalsIgnoreCase("Multisell_Shadow")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (player.getLevel() < 40) {
                    html.setFile("data/html/common/shadow_item-lowlevel.htm");
                } else if (player.getLevel() < 46) {
                    html.setFile("data/html/common/shadow_item_mi_c.htm");
                } else if (player.getLevel() < 52) {
                    html.setFile("data/html/common/shadow_item_hi_c.htm");
                } else {
                    html.setFile("data/html/common/shadow_item_b.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("Exc_Multisell")) {
                if (st.countTokens() < 1) {
                    return;
                }

                MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, true);
            } else if (actualCommand.equalsIgnoreCase("Newbie_Exc_Multisell")) {
                if (st.countTokens() < 1) {
                    return;
                }

                if (player.isNewbie()) {
                    MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, true);
                } else {
                    this.showChatWindow(player, "data/html/exchangelvlimit.htm");
                }
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }

    public void showChatWindow(Player player, int val) {
        if (Config.KARMA_PLAYER_CAN_SHOP || player.getKarma() <= 0 || !this.showPkDenyChatWindow(player, "merchant")) {
            this.showChatWindow(player, this.getHtmlPath(this.getNpcId(), val));
        }
    }

    private final void showWearWindow(Player player, int val) {
        NpcBuyList buyList = BuyListManager.getInstance().getBuyList(val);
        if (buyList != null && buyList.isNpcAllowed(this.getNpcId())) {
            player.tempInventoryDisable();
            player.sendPacket(new ShopPreviewList(buyList, player.getAdena(), player.getSkillLevel(239)));
        }
    }

    protected final void showBuyWindow(Player player, int val) {
        NpcBuyList buyList = BuyListManager.getInstance().getBuyList(val);
        if (buyList != null && buyList.isNpcAllowed(this.getNpcId())) {
            player.tempInventoryDisable();
            player.sendPacket(new BuyList(buyList, player.getAdena(), this.getCastle() != null ? this.getCastle().getTaxRate() : (double) 0.0F));
        }
    }
}
