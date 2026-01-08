package enginemods.main.engine.community;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.AuctionItemHolder;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.IconsTable;
import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MemoCommunityBoard extends AbstractMods {
    public MemoCommunityBoard() {
        registerMod(ConfigData.ENABLE_BBS_MEMO);
    }

    private static String getMyAuctions(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        int color = 0;
        PlayerHolder ph = PlayerData.get(player);
        hb.append("<br1>");
        hb.append("If you cancel the sale of an item you will not be refunded<br1>");
        hb.append("the initial commission we charged you.<br>");
        hb.append(Html.newImage("L2UI.SquareGray", 600, 1));
        for (Map.Entry<Integer, AuctionItemHolder> entry : ph.getAuctionsSell().entrySet()) {
            AuctionItemHolder holder = entry.getValue();
            int id = entry.getKey();
            if (player.getObjectId() != holder.getOwnerId())
                continue;
            ItemInstance item = ItemTable.getInstance().createDummyItem(holder.getItemId());
            hb.append("<table ", (color % 2 == 0) ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=600 height=56>");
            hb.append("<tr>");
            hb.append("<td width=32>", Html.newImage(IconsTable.getIconByItemId(item.getItemId()), 32, 32), "</td>");
            hb.append("<td width=489>");
            hb.append("<table cellspacing=0 cellpadding=0 width=489 height=56>");
            hb.append("<tr><td>", Html.newFontColor("FF8000", "Name: "), item.getName(), "</td></tr>");
            hb.append("<tr><td>", Html.newFontColor("FF8000", "Grade: "), item.getItem().getCrystalType().toString(), "</td></tr>");
            hb.append("<tr><td>", Html.newFontColor("FF8000", "Enchant: "), "+", holder.getItemEnchantLevel(), "</td></tr>");
            hb.append("<tr><td>", Html.newFontColor("FF8000", "count: "), "+", holder.getItemCount(), "</td></tr>");
            hb.append("</table");
            hb.append("</td>");
            hb.append("<td width=75><button value=CANCEL action=\"bypass _bbsmemo cancelSell ", id, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append(Html.newImage("L2UI.SquareGray", 600, 1));
            color++;
        }
        return hb.toString();
    }

    private static String allAuctions(String bypass, String grade, int page, String itemType) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        hb.append("<table width=600 height=24 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=18 align=right>", grade.equals("ALL") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=ALL action=\"bypass _bbsmemo ", bypass, " ", itemType, " ALL ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("NONE") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=NO action=\"bypass _bbsmemo ", bypass, " ", itemType, " NONE ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("D") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=D action=\"bypass _bbsmemo ", bypass, " ", itemType, " D ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("C") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=C action=\"bypass _bbsmemo ", bypass, " ", itemType, " C ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("B") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=B action=\"bypass _bbsmemo ", bypass, " ", itemType, " B ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("A") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=A action=\"bypass _bbsmemo ", bypass, " ", itemType, " A ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("<td width=18 align=right>", grade.equals("S") ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td><td width=64><button value=S action=\"bypass _bbsmemo ", bypass, " ", itemType, " S ", page, "\" width=64 height=22 back=", "L2UI_CH3.herochat_tab2",
                " fore=", "L2UI_CH3.herochat_tab2_over", "></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td align=center>");
        hb.append("<table width=126 height=22 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=94 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackMid", 94, 22), "</td>");
        hb.append("<td width=16 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table cellspacing=3 cellpadding=0>");
        hb.append(menuItemType(bypass + " all " + bypass + " " + grade, "all", itemType));
        hb.append(menuItemType(bypass + " weapon " + bypass + " " + grade, "weapon", itemType));
        hb.append(menuItemType(bypass + " chest " + bypass + " " + grade, "chest", itemType));
        hb.append(menuItemType(bypass + " legs " + bypass + " " + grade, "legs", itemType));
        hb.append(menuItemType(bypass + " head " + bypass + " " + grade, "head", itemType));
        hb.append(menuItemType(bypass + " glove " + bypass + " " + grade, "glove", itemType));
        hb.append(menuItemType(bypass + " boot " + bypass + " " + grade, "boot", itemType));
        hb.append(menuItemType(bypass + " shield " + bypass + " " + grade, "shield", itemType));
        hb.append(menuItemType(bypass + " jewel " + bypass + " " + grade, "jewel", itemType));
        hb.append(menuItemType(bypass + " scroll " + bypass + " " + grade, "scroll", itemType));
        hb.append(menuItemType(bypass + " misc " + bypass + " " + grade, "misc", itemType));
        hb.append("</table>");
        hb.append("</td>");
        hb.append("<td align=center>");
        hb.append("<table width=474 height=22 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=442 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackMid", 442, 22), "</td>");
        hb.append("<td width=16 height=22 valign=top align=center>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(getAuctionItems(bypass, itemType, grade, page));
        hb.append("</td>");
        hb.append("</tr>");
        hb.append("</table>");
        return hb.toString();
    }

    private static String buyItem(Player player, PlayerHolder phOwner, int key) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        if (!phOwner.getAuctionsSell().containsKey(key)) {
            hb.append("<br><br><br>");
            hb.append("What the hell are you trying to do???");
        } else if (player.getObjectId() == phOwner.getObjectId()) {
            hb.append("<br><br><br>");
            hb.append("What the hell are you trying to do???");
        } else {
            AuctionItemHolder ih = phOwner.getAuctionsSell().get(key);
            hb.append("<br><br><br>");
            hb.append("<table cellspacing=0 cellpadding=0 height=80 width=600>");
            hb.append("<tr><td height=20 align=center>");
            hb.append(Html.newFontColor("LEVEL", "Are you sure you want to buy???"));
            hb.append("</td></tr>");
            hb.append("<tr><td height=60 align=center>");
            ItemInstance item = ItemTable.getInstance().createDummyItem(ih.getItemId());
            hb.append("<table bgcolor=000000 cellspacing=0 cellpadding=0 width=474>");
            hb.append("<tr>");
            hb.append("<td fixwidth=32>", Html.newImage(IconsTable.getIconByItemId(item.getItemId()), 32, 32), "</td>");
            hb.append("<td align=center>");
            hb.append("<table cellspacing=0 cellpadding=0 width=337 height=60>");
            hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Name: "), item.getName(), " - ", item.getItem().getCrystalType().toString(), "</td></tr>");
            hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Enchant: "), "+", ih.getItemEnchantLevel(), "</td></tr>");
            hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Price: "), Html.formatAdena(ih.getItemPriceCount()), " ", ItemTable.getInstance().getTemplate(ih.getItemPriceId()).getName(), "</td></tr>");
            hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Owner: "), phOwner.getName(), "</td></tr>");
            hb.append("</table>");
            hb.append("</td></tr>");
            hb.append("</table>");
            hb.append("</td></tr>");
            hb.append("<tr><td align=center>");
            hb.append("<br>");
            hb.append("<table cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            hb.append("<td><button value=\"Yes\" action=\"bypass _bbsmemo buyItemConfirm ", phOwner.getObjectId(), " ", key, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
            hb.append("<td><button value=\"No\" action=\"bypass _bbsmemo allAuctions\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append("</td></tr>");
            hb.append("</table>");
        }
        return hb.toString();
    }

    private static String getSellList(Player player, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        hb.append("<br1>");
        hb.append("Please note that for putting an item on sale<br1>");
        hb.append("you will be charged ", Html.formatAdena(ConfigData.COMMISION_FOR_START_SELL), "  ", ItemTable.getInstance().getTemplate(ConfigData.COMMISION_FOR_START_SELL_ID).getName(), "<br1>");
        hb.append("and at the time of sale we keep<br1>");
        hb.append(" ", ConfigData.COMMISION_FOR_END_SELL, " % of the total amount.<br>");
        hb.append(Html.newImage("L2UI.SquareGray", 600, 1));
        int MAX_PER_PAGE = 6;
        int searchPage = MAX_PER_PAGE * (page - 1);
        int count = 0;
        int color = 0;
        ItemInstance[] list = player.getInventory().getAvailableItems(false, true);
        for (ItemInstance item : list) {
            if (count < searchPage) {
                count++;
            } else if (count < searchPage + MAX_PER_PAGE) {
                hb.append("<table ", (color % 2 == 0) ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=600 height=42>");
                hb.append("<tr>");
                hb.append("<td width=32>", Html.newImage(IconsTable.getIconByItemId(item.getItemId()), 32, 32), "</td>");
                hb.append("<td width=300>");
                hb.append("<table cellspacing=0 cellpadding=0 width=300 height=42>");
                hb.append("<tr><td>", Html.newFontColor("FF8000", "Name: "), item.getName(), "</td></tr>");
                hb.append("<tr><td>", Html.newFontColor("FF8000", "Grade: "), item.getItem().getCrystalType().toString(), "</td></tr>");
                hb.append("<tr><td>", Html.newFontColor("FF8000", "Enchant: "), "+", item.getEnchantLevel(), "</td></tr>");
                hb.append("</table");
                hb.append("</td>");
                hb.append("<td width=50>Count:</td>");
                hb.append("<td width=50><edit var=\"count", color, "\" width=50></td>");
                hb.append("<td width=50>Price:</td>");
                hb.append("<td width=50><edit var=\"price", color, "\" width=50></td>");
                hb.append("<td width=50><combobox width=50 var=item", color, " list=Adena;coin1;coin2;coin3;></td>");
                hb.append("<td width=50><button value=SELL action=\"bypass _bbsmemo sellItem ", item.getObjectId(), " $count", color, " $price", color, " $item", color, "\" width=50 height=21 back=", "L2UI_CH3.Btn1_normalOn",
                        " fore=", "L2UI_CH3.Btn1_normal", "></td>");
                hb.append("</tr>");
                hb.append("</table>");
                hb.append(Html.newImage("L2UI.SquareGray", 600, 1));
                color++;
                count++;
            }
        }
        hb.append(Html.newImage("L2UI.SquareWhite", 474, 1));
        hb.append("<table>");
        hb.append("<tr>");
        int currentPage = 1;
        int size = list.length;
        for (int i = 0; i < size; i++) {
            if (i % MAX_PER_PAGE == 0) {
                if (currentPage == page) {
                    hb.append("<td width=20>", Html.newFontColor("LEVEL", currentPage), "</td>");
                } else {
                    hb.append("<td width=20><a action=\"bypass _bbsmemo setAuction ", currentPage, "\">", currentPage, "</a></td>");
                }
                currentPage++;
            }
        }
        hb.append("</tr>");
        hb.append("</table>");
        return hb.toString();
    }

    private static String getAuctionItems(String bypass, String itemType, String grade, int page) {
        List<AuctionItemHolder> itemList = new ArrayList<>();
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            for (AuctionItemHolder holder : ph.getAuctionsSell().values()) {
                ItemInstance it = ItemTable.getInstance().createDummyItem(holder.getItemId());
                switch (grade) {
                    case "ALL":
                        if (searchItemByType(itemType, it))
                            itemList.add(holder);
                        continue;
                }
                CrystalType cType = CrystalType.valueOf(grade);
                if (it.getItem().getCrystalType() == cType)
                    if (searchItemByType(itemType, it))
                        itemList.add(holder);
            }
        }
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        if (!itemList.isEmpty()) {
            int MAX_PER_PAGE = 5;
            int searchPage = MAX_PER_PAGE * (page - 1);
            int count = 0;
            int color = 0;
            hb.append(Html.newImage("L2UI.SquareGray", 474, 1));
            hb.append("<table width=474 height=294 cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            hb.append("<td valign=top>");
            for (AuctionItemHolder holder : itemList) {
                ItemInstance item = ItemTable.getInstance().createDummyItem(holder.getItemId());
                if (count < searchPage) {
                    count++;
                    continue;
                }
                if (count >= searchPage + MAX_PER_PAGE)
                    continue;
                hb.append("<table ", (color % 2 == 0) ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=474 height=56>");
                hb.append("<tr>");
                hb.append("<td fixwidth=32>", Html.newImage(IconsTable.getIconByItemId(item.getItemId()), 32, 32), "</td>");
                hb.append("<td height=56>");
                hb.append("<table cellspacing=0 cellpadding=0 width=337 height=56>");
                hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Name: "), item.getName(), " - ", item.getItem().getCrystalType().toString(), " - (", holder.getItemCount(), ")</td></tr>");
                hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Enchant: "), "+", holder.getItemEnchantLevel(), "</td></tr>");
                hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Price: "), Html.formatAdena(holder.getItemPriceCount()), " ", ItemTable.getInstance().getTemplate(holder.getItemPriceId()).getName(), "</td></tr>");
                hb.append("<tr><td fixwidth=337>", Html.newFontColor("FF8000", "Owner: "), PlayerData.get(holder.getOwnerId()).getName(), "</td></tr>");
                hb.append("</table>");
                hb.append("</td>");
                hb.append("<td width=105><button value=BUY action=\"bypass _bbsmemo buyItem ", holder.getOwnerId(), " ", holder.getkey(), "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
                hb.append("</tr>");
                hb.append("</table>");
                hb.append(Html.newImage("L2UI.SquareGray", 474, 1));
                color++;
                count++;
            }
            hb.append("</td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append("<br>");
            hb.append(Html.newImage("L2UI.SquareWhite", 474, 1));
            hb.append("<table bgcolor=000000 cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            int currentPage = 1;
            int size = itemList.size();
            for (int i = 0; i < size; i++) {
                if (i % MAX_PER_PAGE == 0) {
                    if (currentPage == page) {
                        hb.append("<td width=20>", Html.newFontColor("LEVEL", currentPage), "</td>");
                    } else {
                        hb.append("<td width=20><a action=\"bypass _bbsmemo ", bypass, " ", itemType, " ", grade, " ", currentPage, "\">", currentPage,
                                "</a></td>");
                    }
                    currentPage++;
                }
            }
            hb.append("</tr>");
            hb.append("</table>");
            hb.append(Html.newImage("L2UI.SquareWhite", 474, 1));
            hb.append("<br>");
        } else {
            hb.append("<center>No items for sale</center>");
        }
        hb.append("<button value=\"Refresh\" action=\"bypass _bbsmemo ", bypass, " ", itemType, " ", grade, " ", page, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn",
                " fore=", "L2UI_CH3.Btn1_normal", ">");
        return hb.toString();
    }

    private static String menuItemType(String bypass, String itemType, String actualItemType) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        hb.append("<tr>");
        hb.append("<td width=16 height=22>", actualItemType.equals(itemType) ? Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16) : "", "</td>");
        hb.append("<td><button value=\"", itemType, "\" action=\"bypass _bbsmemo ", bypass, "\" width=95 height=22 back=", "L2UI_CH3.bigbutton_down", " fore=", "L2UI_CH3.bigbutton", "></td>");
        hb.append("</tr>");
        return hb.toString();
    }

    private static String marcButton(String bypass) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        if (bypass != null) {
            hb.append("<table cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            hb.append("<td>", Html.newImage(bypass.equals("allAuctions") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("<td>", Html.newImage(bypass.equals("myAuctions") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("<td>", Html.newImage(bypass.equals("setAuction") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("</tr>");
            hb.append("</table>");
        } else {
            hb.append(Html.newImage("L2UI.SquareGray", 506, 1));
        }
        return hb.toString();
    }

    private static boolean searchItemByType(String itemType, ItemInstance it) {
        switch (itemType) {
            case "all":
                return true;
            case "weapon":
                if (it.getItem() instanceof net.sf.l2j.gameserver.model.item.kind.Weapon)
                    return true;
                break;
            case "chest":
                if (it.getItem().getBodyPart() == 1024 || it.getItem().getBodyPart() == 32768)
                    return true;
                break;
            case "legs":
                if (it.getItem().getBodyPart() == 2048)
                    return true;
                break;
            case "head":
                if (it.getItem().getBodyPart() == 64)
                    return true;
                break;
            case "glove":
                if (it.getItem().getBodyPart() == 512)
                    return true;
                break;
            case "boot":
                if (it.getItem().getBodyPart() == 4096)
                    return true;
                break;
            case "shield":
                if (it.getItem().getBodyPart() == 1)
                    return true;
                break;
            case "jewel":
                switch (it.getItem().getBodyPart()) {
                    case 2:
                    case 4:
                    case 6:
                    case 8:
                    case 16:
                    case 32:
                    case 48:
                        return true;
                }
                break;
            case "scroll":
                if (it.getItem().getItemType() == EtcItemType.BLESS_SCRL_ENCHANT_WP || it.getItem().getItemType() == EtcItemType.BLESS_SCRL_ENCHANT_AM || it.getItem().getItemType() == EtcItemType.SCRL_ENCHANT_WP || it.getItem().getItemType() == EtcItemType.SCRL_ENCHANT_AM)
                    return true;
                break;
            case "misc":
                if (it.getItem() instanceof net.sf.l2j.gameserver.model.item.kind.EtcItem) {
                    return it.getItem().getItemType() != EtcItemType.BLESS_SCRL_ENCHANT_WP && it.getItem().getItemType() != EtcItemType.BLESS_SCRL_ENCHANT_AM && it.getItem().getItemType() != EtcItemType.SCRL_ENCHANT_WP && it.getItem().getItemType() != EtcItemType.SCRL_ENCHANT_AM;
                }
                break;
        }
        return false;
    }

    private static void giveAuctionSold(Player owner, AuctionItemHolder ih) {
        Item item = ItemTable.getInstance().getTemplate(ih.getItemId());
        owner.sendPacket(new CreatureSay(0, 2, "Auction", "You have sold the item " + item.getName()));
        owner.sendPacket(new ExShowScreenMessage("You have sold the item " + item.getName(), 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
        owner.addItem("auction", ih.getItemPriceId(), ih.getItemPriceCount(), null, true);
        owner.getInventory().updateDatabase();
        owner.sendPacket(new ItemList(owner, false));
        owner.sendPacket(new InventoryUpdate());
        owner.sendPacket(new EtcStatusUpdate(owner));
        owner.refreshOverloaded();
        owner.refreshExpertisePenalty();
        owner.sendPacket(new UserInfo(owner));
        owner.broadcastUserInfo();
    }

    private static int getNewKey(Map<Integer, AuctionItemHolder> map) {
        int id = -1;
        for (int i = 1; i <= 100; i++) {
            if (!map.containsKey(i)) {
                id = i;
                break;
            }
        }
        return id;
    }

    public static void getInstance() {
    }

    public void onModState() {
        switch (getState()) {
            case START:
                loadAllItems();
                break;
        }
    }

    private void loadAllItems() {
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            try {
                int i;
                for (i = 1; i <= 100; i++) {
                    String auction = getValueDB(ph.getObjectId(), "auctionSold " + i);
                    if (auction != null)
                        ph.addAuctionSold(i, new AuctionItemHolder(auction));
                }
                for (i = 1; i <= 100; i++) {
                    String auction = getValueDB(ph.getObjectId(), "auctionSell " + i);
                    if (auction != null)
                        ph.addAuctionSell(i, new AuctionItemHolder(auction));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeSold(PlayerHolder ph, int key) {
        removeValueDB(ph.getObjectId(), "auctionSold " + key);
        ph.removeAuctionSold(key);
    }

    private void removeSell(PlayerHolder ph, int key) {
        removeValueDB(ph.getObjectId(), "auctionSell " + key);
        ph.removeAuctionSell(key);
    }

    public void onEnterWorld(Player player) {
        PlayerHolder ph = PlayerData.get(player);
        for (AuctionItemHolder ih : ph.getAuctionsSold().values()) {
            removeSold(ph, ih.getkey());
            giveAuctionSold(player, ih);
        }
    }

    public boolean onCommunityBoard(Player player, String command) {
        if (command.startsWith("_bbsmemo")) {
            int itemObjId, page;
            String itemType;
            int itemCount;
            String grade;
            int itemPriceCount, i, itemPriceId;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String bypass = st.hasMoreTokens() ? st.nextToken() : "allAuctions";
            HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
            hb.append("<html><body>");
            hb.append("<br>");
            hb.append("<center>");
            hb.append(marcButton(bypass));
            hb.append("<table>");
            hb.append("<tr>");
            hb.append("<td><button value=\"ALL AUCTIONS\" action=\"bypass _bbsmemo allAuctions\" width=100 height=32 back=", "L2UI_CH3.refinegrade3_21", " fore=", "L2UI_CH3.refinegrade3_22", "></td>");
            hb.append("<td><button value=\"MY AUCTION\" action=\"bypass _bbsmemo myAuctions\" width=100 height=32 back=", "L2UI_CH3.refinegrade3_21", " fore=", "L2UI_CH3.refinegrade3_22", "></td>");
            hb.append("<td><button value=\"AUCTION ITEM\" action=\"bypass _bbsmemo setAuction\" width=100 height=32 back=", "L2UI_CH3.refinegrade3_21", " fore=", "L2UI_CH3.refinegrade3_22", "></td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append(marcButton(bypass));
            hb.append("<br>");
            hb.append("<br>");
            switch (bypass) {
                case "buyItemConfirm":
                    try {
                        PlayerHolder phOwner = PlayerData.get(Integer.parseInt(st.nextToken()));
                        int key = Integer.parseInt(st.nextToken());
                        hb.append(buyItemConfirm(player, phOwner, key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "buyItem":
                    try {
                        PlayerHolder phOwner = PlayerData.get(Integer.parseInt(st.nextToken()));
                        int key = Integer.parseInt(st.nextToken());
                        hb.append(buyItem(player, phOwner, key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "sellItem":
                    itemObjId = 0;
                    itemCount = 1;
                    itemPriceCount = 0;
                    itemPriceId = 57;
                    try {
                        itemObjId = Integer.parseInt(st.nextToken());
                        itemCount = Integer.parseInt(st.nextToken());
                        itemPriceCount = Integer.parseInt(st.nextToken());
                        switch (st.nextToken()) {
                            case "Adena":
                                itemPriceId = 57;
                                break;
                        }
                        hb.append(sellItem(player, itemObjId, itemCount, itemPriceCount, itemPriceId));
                    } catch (Exception e) {
                        break;
                    }
                case "cancelSell":
                    if (st.hasMoreTokens()) {
                        PlayerHolder ph = PlayerData.get(player);
                        int key = Integer.parseInt(st.nextToken());
                        AuctionItemHolder ih = ph.getAuctionsSell().get(key);
                        if (ih != null)
                            if (ih.getOwnerId() == player.getObjectId()) {
                                removeSell(ph, key);
                                ItemInstance itemBuy = ItemInstance.create(ih.getItemId(), 1, player, player);
                                itemBuy.setEnchantLevel(ih.getItemEnchantLevel());
                                player.addItem("auction buy", itemBuy, player, true);
                            } else {
                                hb.append("bypass? xD");
                            }
                    }
                case "myAuctions":
                    hb.append(getMyAuctions(player));
                    break;
                case "setAuction":
                    page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                    hb.append(getSellList(player, page));
                    break;
                case "allAuctions":
                    itemType = st.hasMoreTokens() ? st.nextToken() : "all";
                    grade = st.hasMoreTokens() ? st.nextToken() : "ALL";
                    i = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                    hb.append(allAuctions(bypass, grade, i, itemType));
                    break;
            }
            hb.append("</center>");
            hb.append("</body></html>");
            sendCommunity(player, hb.toString());
            return true;
        }
        return false;
    }

    private String sellItem(Player player, int itemObjId, int itemCount, int itemPriceCount, int itemPriceId) {
        if (itemPriceCount < 0)
            itemPriceCount = 0;
        PlayerHolder ph = PlayerData.get(player);
        if (ph.getAuctionsSell().size() > 100)
            return "<br><br><br>you have exceeded the maximum allowed number of items of 100.";
        if (!player.destroyItemByItemId("start auction", ConfigData.COMMISION_FOR_START_SELL_ID, ConfigData.COMMISION_FOR_START_SELL, player, false))
            return "<br><br><br>Not enough for the initial commission";
        if (player.getInventory().getItemByObjectId(itemObjId).getCount() < itemCount)
            return "<br><br><br>You do not have so many items to sell";
        ItemInstance item = player.getInventory().destroyItem("auction", itemObjId, itemCount, player, player);
        if (item != null) {
            int id = getNewKey(ph.getAuctionsSell());
            if (id == -1)
                return "<br><br><br>ufff an error obtaining the new item id ";
            AuctionItemHolder ash = new AuctionItemHolder(id, player.getObjectId(), itemObjId, item.getItemId(), itemCount, item.getEnchantLevel(), itemPriceCount, itemPriceId);
            ph.addAuctionSell(id, ash);
            setValueDB(player.getObjectId(), "auctionSell " + id, ash.toString());
        }
        return "";
    }

    private String buyItemConfirm(Player player, PlayerHolder phOwner, int key) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
        if (!phOwner.getAuctionsSell().containsKey(key)) {
            hb.append("<br><br><br>");
            hb.append("What the hell are you trying to do???");
        } else {
            AuctionItemHolder ih = phOwner.getAuctionsSell().get(key);
            int countBuy = player.getInventory().getInventoryItemCount(ih.getItemPriceId(), -1, false);
            if (countBuy < ih.getItemPriceCount()) {
                hb.append("<br><br><br>");
                hb.append("Sorry, you lack resources to buy this item???");
                return hb.toString();
            }
            player.getInventory().destroyItemByItemId("auction buy", ih.getItemPriceId(), ih.getItemPriceCount(), player, player);
            ItemInstance itemBuy = ItemInstance.create(ih.getItemId(), 1, player, player);
            itemBuy.setEnchantLevel(ih.getItemEnchantLevel());
            player.addItem("auction buy", itemBuy, player, true);
            player.sendPacket(new ItemList(player, false));
            player.sendPacket(new InventoryUpdate());
            player.sendPacket(new EtcStatusUpdate(player));
            player.refreshOverloaded();
            player.refreshExpertisePenalty();
            player.sendPacket(new UserInfo(player));
            player.broadcastUserInfo();
            Player owner = World.getInstance().getPlayer(phOwner.getObjectId());
            if (owner == null) {
                int id = getNewKey(phOwner.getAuctionsSold());
                if (id == -1)
                    return "<br><br><br>ufff an error getting the new item id ";
                ih.setKey(id);
                phOwner.addAuctionSold(ih.getkey(), ih);
                setValueDB(phOwner.getObjectId(), "auctionSold " + ih.getkey(), ih.toString());
            } else {
                giveAuctionSold(owner, ih);
            }
            removeSell(phOwner, ih.getkey());
            hb.append("<br><br><br>");
            hb.append("Congratulations your purchase was successful.");
        }
        return hb.toString();
    }

    private static class SingletonHolder {
        protected static final MemoCommunityBoard INSTANCE = new MemoCommunityBoard();
    }
}
