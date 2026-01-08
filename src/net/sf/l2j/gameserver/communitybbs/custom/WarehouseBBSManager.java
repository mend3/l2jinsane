package net.sf.l2j.gameserver.communitybbs.custom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.itemcontainer.PcFreight;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.Map;

public class WarehouseBBSManager extends BaseBBSManager {
    public static WarehouseBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
            return;
        if (player.isProcessingTransaction()) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING);
            return;
        }
        if (player.getActiveEnchantItem() != null) {
            player.setActiveEnchantItem(null);
            player.sendPacket(EnchantResult.CANCELLED);
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
            return;
        }
        if (command.equals("_bbsWH") || command.equals("_bbsmemo")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
            separateAndSend(html, player);
            return;
        }
        if (command.startsWith("_bbsWHWithdrawP")) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            player.setCommunityWarehouse(true);
            player.setActiveWarehouse(player.getWarehouse());
            if (player.getActiveWarehouse().getSize() == 0) {
                player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
                player.setCommunityWarehouse(false);
                return;
            }
            player.sendPacket(new WarehouseWithdrawList(player, 1));
            ShowMainPage(player);
            return;
        }
        switch (command) {
            case "_bbsWHDepositP" -> {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.setCommunityWarehouse(true);
                player.setActiveWarehouse(player.getWarehouse());
                player.tempInventoryDisable();
                player.sendPacket(new WarehouseDepositList(player, 1));
                ShowMainPage(player);
                return;
            }
            case "_bbsWHWithdrawC" -> {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.setCommunityWarehouse(true);
                if ((player.getClanPrivileges() & 0x8) != 8) {
                    player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
                    return;
                }
                if (player.getClan().getLevel() == 0) {
                    player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                } else {
                    player.setActiveWarehouse(player.getClan().getWarehouse());
                    player.sendPacket(new WarehouseWithdrawList(player, 2));
                }
                ShowMainPage(player);
                return;
            }
            case "_bbsWHDepositC" -> {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.setCommunityWarehouse(true);
                if (player.getClan() != null)
                    if (player.getClan().getLevel() == 0) {
                        player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                    } else {
                        player.setActiveWarehouse(player.getClan().getWarehouse());
                        player.tempInventoryDisable();
                        player.sendPacket(new WarehouseDepositList(player, 2));
                    }
                ShowMainPage(player);
                return;
            }
        }
        if (command.startsWith("_bbsWHWithdrawF")) {
            player.setCommunityWarehouse(true);
            if (Config.ALLOW_FREIGHT) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                PcFreight freight = player.getFreight();
                if (freight != null)
                    if (freight.getSize() > 0) {
                        freight.setActiveLocation(0);
                        player.setActiveWarehouse(freight);
                        player.sendPacket(new WarehouseWithdrawList(player, 4));
                    } else {
                        player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
                    }
            }
            ShowMainPage(player);
            return;
        }
        if (command.startsWith("_bbsWHDepositF")) {
            player.setCommunityWarehouse(true);
            if (Config.ALLOW_FREIGHT)
                if (player.getAccountChars().isEmpty()) {
                    player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
                } else {
                    Map<Integer, String> chars = player.getAccountChars();
                    if (chars.isEmpty()) {
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                        return;
                    }
                    player.sendPacket(new PackageToList(chars));
                }
            ShowMainPage(player);
            return;
        }
        if (command.startsWith("_bbsWHFreightChar")) {
            player.setCommunityWarehouse(true);
            if (Config.ALLOW_FREIGHT) {
                String id = command.substring(command.lastIndexOf("_") + 1);
                player.sendPacket(ActionFailed.STATIC_PACKET);
                PcFreight freight = player.getDepositedFreight(Integer.parseInt(id));
                freight.setActiveLocation(0);
                player.setActiveWarehouse(freight);
                player.tempInventoryDisable();
                player.sendPacket(new WarehouseDepositList(player, 4));
            }
            ShowMainPage(player);
            return;
        }
        super.parseCmd(command, player);
    }

    private void ShowMainPage(Player player) {
        String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
        separateAndSend(html, player);
    }

    protected String getFolder() {
        return "top/warehouse/";
    }

    private static class SingletonHolder {
        protected static final WarehouseBBSManager INSTANCE = new WarehouseBBSManager();
    }
}
