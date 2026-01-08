package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.itemcontainer.PcFreight;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.Map;

public class WarehouseKeeper extends Folk {
    public WarehouseKeeper(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isWarehouse() {
        return true;
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }

        return "data/html/warehouse/" + filename + ".htm";
    }

    public void onBypassFeedback(Player player, String command) {
        player.setCommunityWarehouse(false);
        if (Config.KARMA_PLAYER_CAN_USE_WH || player.getKarma() <= 0 || !this.showPkDenyChatWindow(player, "warehouse")) {
            if (player.isProcessingTransaction()) {
                player.sendPacket(SystemMessageId.ALREADY_TRADING);
            } else {
                if (player.getActiveEnchantItem() != null) {
                    player.setActiveEnchantItem(null);
                    player.sendPacket(EnchantResult.CANCELLED);
                    player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
                }

                if (command.startsWith("WithdrawP")) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    player.setActiveWarehouse(player.getWarehouse());
                    if (player.getActiveWarehouse().getSize() == 0) {
                        player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
                        return;
                    }

                    player.sendPacket(new WarehouseWithdrawList(player, 1));
                } else if (command.equals("DepositP")) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    player.setActiveWarehouse(player.getWarehouse());
                    player.tempInventoryDisable();
                    player.sendPacket(new WarehouseDepositList(player, 1));
                } else if (command.equals("WithdrawC")) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    if ((player.getClanPrivileges() & 8) != 8) {
                        player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
                        return;
                    }

                    if (player.getClan().getLevel() == 0) {
                        player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                    } else {
                        player.setActiveWarehouse(player.getClan().getWarehouse());
                        player.sendPacket(new WarehouseWithdrawList(player, 2));
                    }
                } else if (command.equals("DepositC")) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    if (player.getClan() != null) {
                        if (player.getClan().getLevel() == 0) {
                            player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                        } else {
                            player.setActiveWarehouse(player.getClan().getWarehouse());
                            player.tempInventoryDisable();
                            player.sendPacket(new WarehouseDepositList(player, 2));
                        }
                    }
                } else if (command.startsWith("WithdrawF")) {
                    if (Config.ALLOW_FREIGHT) {
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                        PcFreight freight = player.getFreight();
                        if (freight != null) {
                            if (freight.getSize() > 0) {
                                if (Config.ALT_GAME_FREIGHTS) {
                                    freight.setActiveLocation(0);
                                } else {
                                    freight.setActiveLocation(this.getRegion().hashCode());
                                }

                                player.setActiveWarehouse(freight);
                                player.sendPacket(new WarehouseWithdrawList(player, 4));
                            } else {
                                player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
                            }
                        }
                    }
                } else if (command.startsWith("DepositF")) {
                    if (Config.ALLOW_FREIGHT) {
                        if (player.getAccountChars().isEmpty()) {
                            player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
                        } else {
                            Map<Integer, String> chars = player.getAccountChars();
                            if (chars.size() < 1) {
                                player.sendPacket(ActionFailed.STATIC_PACKET);
                                return;
                            }

                            player.sendPacket(new PackageToList(chars));
                        }
                    }
                } else if (command.startsWith("FreightChar")) {
                    if (Config.ALLOW_FREIGHT) {
                        String id = command.substring(command.lastIndexOf("_") + 1);
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                        PcFreight freight = player.getDepositedFreight(Integer.parseInt(id));
                        if (Config.ALT_GAME_FREIGHTS) {
                            freight.setActiveLocation(0);
                        } else {
                            freight.setActiveLocation(this.getRegion().hashCode());
                        }

                        player.setActiveWarehouse(freight);
                        player.tempInventoryDisable();
                        player.sendPacket(new WarehouseDepositList(player, 4));
                    }
                } else {
                    super.onBypassFeedback(player, command);
                }

            }
        }
    }

    public void showChatWindow(Player player, int val) {
        if (Config.KARMA_PLAYER_CAN_USE_WH || player.getKarma() <= 0 || !this.showPkDenyChatWindow(player, "warehouse")) {
            this.showChatWindow(player, this.getHtmlPath(this.getNpcId(), val));
        }
    }
}
