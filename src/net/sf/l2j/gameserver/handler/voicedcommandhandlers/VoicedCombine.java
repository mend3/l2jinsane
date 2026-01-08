package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import mods.combineItem.CombineEntry;
import mods.combineItem.CombineItem;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.security.SecureRandom;
import java.util.Optional;

public class VoicedCombine implements IVoicedCommandHandler {
    private final SecureRandom RANDOM = new SecureRandom();

    private final String COMBINE_COMMAND = "combine";

    private final String PAGE_COMMAND = "page";

    private final String CONFIRM_COMMAND = "confirm";

    private static void handleMainPage(Player player) {
        sendHtmlText("combineitem.htm", player);
    }

    private static void handlePageRequest(Player player, String pageNumber) {
        String htmlFile = "combineitem-%s.htm".formatted(pageNumber);
        sendHtmlText(htmlFile, player);
    }

    private static Optional<CombineEntry> findCombineEntry(int result) {
        return CombineItem.getCombinations().stream()
                .filter(entry -> (entry.getResult() == result))
                .findFirst();
    }

    private static boolean hasRequiredItems(Player player, CombineEntry entry) {
        int countItem1 = player.getInventory().getInventoryItemCount(entry.getItem1(), -1, false);
        int countItem2 = player.getInventory().getInventoryItemCount(entry.getItem2(), -1, false);
        int countItem3 = player.getInventory().getInventoryItemCount(entry.getAdena(), -1, false);
        return (countItem1 >= entry.getCount1() && countItem2 >= entry.getCount2() && countItem3 >= entry.getAdena());
    }

    private static boolean hasRequiredAdena(Player player, CombineEntry entry) {
        int countAdena = player.getInventory().getInventoryItemCount(entry.getAdena(), -1, false);
        return (countAdena >= entry.getCounAdena());
    }

    private static boolean areItemsRemoved(ItemInstance removed1, ItemInstance removed2, ItemInstance removedAdena) {
        return (removed1 != null && removed2 != null && removedAdena != null);
    }

    private static void updateInventory(Player player, ItemInstance removed1, ItemInstance removed2, ItemInstance removedAdena) {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(removed1);
        if (removed2 != null)
            iu.addModifiedItem(removed2);
        iu.addModifiedItem(removedAdena);
        player.sendPacket(iu);
    }

    private static void sendResultMessage(Player player, boolean success) {
        String message = success ? "Combination successful!" : "Combination failed! You received part of the materials.";
        player.sendPacket(new ExShowScreenMessage(message, 3000));
    }

    private static void redirectToPage(Player player, String[] parts) {
        if (parts.length >= 5) {
            String pageNum = parts[4];
            sendHtmlText("combineitem-%s.htm".formatted(pageNum), player);
        } else {
            sendHtmlText("combineitem.htm", player);
        }
    }

    private static void sendHtmlText(String fileName, Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/combineitem/" + fileName);
        player.sendPacket(html);
    }

    public void useVoicedCommand(String command, Player player, String target) {
        try {
            String[] parts = command.split(" ");
            if (parts.length == 0 || !"combine".equalsIgnoreCase(parts[0]))
                return;
            if (parts.length == 1) {
                handleMainPage(player);
                return;
            }
            if (parts.length == 3 && "page".equalsIgnoreCase(parts[1])) {
                handlePageRequest(player, parts[2]);
                return;
            }
            if (parts.length >= 3 && "confirm".equalsIgnoreCase(parts[1])) {
                handleCombineConfirm(player, parts);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number format in command.");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Error processing combination command.");
        }
    }

    private void handleCombineConfirm(Player player, String[] parts) {
        int result = Integer.parseInt(parts[2]);
        Optional<CombineEntry> entryOpt = findCombineEntry(result);
        if (entryOpt.isEmpty()) {
            player.sendMessage("Invalid combination.");
            return;
        }
        CombineEntry entry = entryOpt.get();
        if (!hasRequiredItems(player, entry)) {
            player.sendMessage("You don't have the required items.");
            return;
        }
        if (!hasRequiredAdena(player, entry)) {
            player.sendMessage("You need %d adena to combine.".formatted(entry.getAdena()));
            return;
        }
        processCombination(player, entry, parts);
    }

    private void processCombination(Player player, CombineEntry entry, String[] parts) {
        int qtyToRemove1 = entry.getCount1();
        int qtyToRemove2 = entry.getCount2();
        int qtyToRemove3 = entry.getCounAdena();
        ItemInstance removed1 = player.getInventory().destroyItemByItemId("CombineItem", entry
                .getItem1(), qtyToRemove1, player, player);
        ItemInstance removed2 = player.getInventory().destroyItemByItemId("CombineItem", entry
                .getItem1(), qtyToRemove2, player, player);
        ItemInstance removedAdena = player.getInventory().destroyItemByItemId("CombineItem", entry
                .getAdena(), qtyToRemove3, player, player);
        if (!areItemsRemoved(removed1, removed2, removedAdena)) {
            player.sendMessage("Error removing combination items. Please try again.");
            return;
        }
        updateInventory(player, removed1, removed2, removedAdena);
        boolean success = performCombination(player, entry);
        sendResultMessage(player, success);
        redirectToPage(player, parts);
        player.sendPacket(new ItemList(player, false));
        player.sendPacket(new InventoryUpdate());
        player.sendPacket(new EtcStatusUpdate(player));
        player.refreshOverloaded();
        player.refreshExpertisePenalty();
        player.sendPacket(new UserInfo(player));
        player.broadcastUserInfo();
    }

    private boolean performCombination(Player player, CombineEntry entry) {
        double chance = this.RANDOM.nextDouble() * 100.0D;
        if (chance <= entry.getChance()) {
            player.getInventory().addItem("CombineSuccess", entry.getResult(), entry.getCounResult(), player, player);
            return true;
        }
        player.getInventory().addItem("CombineFail", entry.getFailureItem(), 1, player, player);
        return false;
    }

    public String[] getVoicedCommandList() {
        return new String[]{"combine"};
    }
}
