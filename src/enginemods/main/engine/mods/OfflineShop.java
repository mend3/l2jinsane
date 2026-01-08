package enginemods.main.engine.mods;

import enginemods.main.EngineModsManager;
import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.packets.PrivateCustomTitle;
import enginemods.main.packets.PrivateCustomTitle.TitleType;
import enginemods.main.util.Util;
import enginemods.main.util.UtilPlayer;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

import java.util.List;
import java.util.logging.Level;

public class OfflineShop extends AbstractMods {
    public OfflineShop() {
        if (ConfigData.OFFLINE_TRADE_ENABLE || ConfigData.OFFLINE_SELLBUFF_ENABLE) {
            this.registerMod(true);
        }

    }

    public static void getInstance() {
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.restoreAllOfflineShops();
                this.clearValueDB();
            case END:
            default:
        }
    }

    public void onShutDown() {
        for (Player player : World.getInstance().getPlayers()) {
            try {
                boolean saveValue = false;
                String title = "";
                StringBuilder storeItems = new StringBuilder();
                String storeType = "";
                if (player.isInStoreMode() && ConfigData.OFFLINE_TRADE_ENABLE) {
                    storeType = player.getStoreType().name();
                    switch (player.getStoreType()) {
                        case BUY:
                            title = player.getBuyList().getTitle();

                            for (TradeItem item : player.getBuyList().getItems()) {
                                storeItems.append(item.getItem().getItemId()).append(",").append(item.getCount()).append(",").append(item.getPrice()).append(";");
                            }
                            break;
                        case MANUFACTURE:
                            title = player.getCreateList().getStoreName();

                            for (ManufactureItem item : player.getCreateList().getList()) {
                                storeItems.append(item.getId()).append(",").append(item.getValue()).append(";");
                            }
                            break;
                        case PACKAGE_SELL:
                        case SELL:
                            title = player.getSellList().getTitle();

                            for (TradeItem item : player.getSellList().getItems()) {
                                storeItems.append(item.getObjectId()).append(",").append(item.getCount()).append(",").append(item.getPrice()).append(";");
                            }
                            break;
                        default:
                            System.out.println("NPE ->" + player.getStoreType().name());
                            return;
                    }

                    saveValue = true;
                } else if (PlayerData.get(player).isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE) {
                    title = "SellBuff";
                    storeItems.append(PlayerData.get(player).getSellBuffPrice());
                    storeType = "SELL_BUFF";
                    saveValue = false;
                }

                if (saveValue) {
                    this.setValueDB(player.getObjectId(), "offlineShop", storeType + "#" + (title != null && !title.isEmpty() ? title.replaceAll("#", " ") : "null") + "#" + storeItems);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void onEnterWorld(Player player) {
        if (PlayerData.get(player).isOffline()) {
            PlayerData.get(player).setOffline(false);
        }

    }

    public boolean onExitWorld(Player player) {
        if (player.isInStoreMode() && ConfigData.OFFLINE_TRADE_ENABLE || PlayerData.get(player).isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE) {
            if (!player.isInsideZone(ZoneId.PEACE)) {
                player.sendMessage("Estas fuera de la zona de paz!");
                return true;
            }

            if (player.isInOlympiadMode() || player.isFestivalParticipant() || player.isInJail()) {
                return true;
            }

            if (player.isInParty()) {
                player.getParty().removePartyMember(player, MessageType.DISCONNECTED);
            }

            if (player.getSummon() != null) {
                player.getSummon().unSummon(player);
            }

            if (OlympiadManager.getInstance().isRegistered(player) || player.getOlympiadGameId() != -1) {
                OlympiadManager.getInstance().removeDisconnectedCompetitor(player);
            }

            ThreadPool.schedule(() -> {
                if (ConfigData.OFFLINE_SET_NAME_COLOR) {
                    player.getAppearance().setNameColor(ConfigData.OFFLINE_NAME_COLOR);
                }

            }, 5000L);
            PlayerData.get(player).setOffline(true);
        }

        return false;
    }

    private void restoreAllOfflineShops() {
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            String shop = this.getValueDB(ph.getObjectId(), "offlineShop");
            if (shop != null) {
                Player player = null;

                try {
                    String shopType = shop.split("#")[0];
                    String shopTitle = shop.split("#")[1];
                    String shopItems = shop.split("#")[2];
                    player = UtilPlayer.spawnPlayer(ph.getObjectId());
                    player.sitDown();
                    player.setIsInvul(true);
                    ph.setOffline(true);
                    if (shopType.equals("SELL_BUFF")) {
                        EngineModsManager.onEvent(player, "SellBuffs sell" + shopItems);
                        PlayerData.get(player).setSellBuff(true);
                        PlayerData.get(player).setSellBuffPrice(Integer.parseInt(shopItems));
                        player.sitDown();
                        player.setIsImmobilized(true);
                        player.setTeam(TeamType.BLUE);
                        player.broadcastUserInfo();
                        player.broadcastPacket(new PrivateCustomTitle(player, TitleType.SELL, "SellBuffs"));
                        if (PlayerData.get(player).isAio()) {
                            SystemAio.getInstance().onEnterWorld(player);
                        }
                    } else {
                        StoreType store = Enum.valueOf(StoreType.class, shopType);
                        switch (store) {
                            case BUY:
                                for (String list : shopItems.split(";")) {
                                    List<Integer> items = Util.parseInt(list, ",");
                                    if (player.getBuyList().addItemByItemId(items.get(0), items.get(1), items.get(2)) == null) {
                                        throw new NullPointerException();
                                    }
                                }

                                player.getBuyList().setTitle(shopTitle.equals("null") ? "" : shopTitle);
                                break;
                            case MANUFACTURE:
                                ManufactureList createList = new ManufactureList();

                                for (String list : shopItems.split(";")) {
                                    List<Integer> items = Util.parseInt(list, ",");
                                    createList.add(new ManufactureItem(items.get(0), items.get(1)));
                                }

                                player.setCreateList(createList);
                                player.getCreateList().setStoreName(shopTitle.equals("null") ? "" : shopTitle);
                                break;
                            case PACKAGE_SELL:
                            case SELL:
                                for (String list : shopItems.split(";")) {
                                    List<Integer> items = Util.parseInt(list, ",");
                                    if (player.getSellList().addItem(items.get(0), items.get(1), items.get(2)) == null) {
                                        throw new NullPointerException();
                                    }
                                }

                                player.getSellList().setTitle(shopTitle.equals("null") ? "" : shopTitle);
                                player.getSellList().setPackaged(store == StoreType.PACKAGE_SELL);
                                break;
                            default:
                                System.out.println("Wrong store type " + store);
                                player.deleteMe();
                        }

                        player.setStoreType(store);
                    }

                    if (ConfigData.OFFLINE_SET_NAME_COLOR) {
                        player.getAppearance().setNameColor(ConfigData.OFFLINE_NAME_COLOR);
                    }

                    player.broadcastUserInfo();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, this.getClass().getSimpleName() + ": Error loading trader: " + player, e);
                    e.printStackTrace();
                    if (player != null) {
                        player.deleteMe();
                    }

                    ph.setOffline(false);
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final OfflineShop INSTANCE = new OfflineShop();
    }
}
