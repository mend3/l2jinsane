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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class OfflineShop extends AbstractMods {
    public OfflineShop() {
        if (ConfigData.OFFLINE_TRADE_ENABLE || ConfigData.OFFLINE_SELLBUFF_ENABLE) {
            this.registerMod(true);
        }

    }

    public static OfflineShop getInstance() {
        return OfflineShop.SingletonHolder.INSTANCE;
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
        Iterator var1 = World.getInstance().getPlayers().iterator();

        while (var1.hasNext()) {
            Player player = (Player) var1.next();

            try {
                boolean saveValue = false;
                String title = "";
                String storeItems = "";
                String storeType = "";
                if (player.isInStoreMode() && ConfigData.OFFLINE_TRADE_ENABLE) {
                    storeType = player.getStoreType().name();
                    Iterator var7;
                    label69:
                    switch (player.getStoreType()) {
                        case BUY:
                            title = player.getBuyList().getTitle();
                            var7 = player.getBuyList().getItems().iterator();

                            while (true) {
                                if (!var7.hasNext()) {
                                    break label69;
                                }

                                TradeItem item = (TradeItem) var7.next();
                                storeItems = storeItems + item.getItem().getItemId() + "," + item.getCount() + "," + item.getPrice() + ";";
                            }
                        case MANUFACTURE:
                            title = player.getCreateList().getStoreName();
                            var7 = player.getCreateList().getList().iterator();

                            while (true) {
                                if (!var7.hasNext()) {
                                    break label69;
                                }

                                ManufactureItem item = (ManufactureItem) var7.next();
                                storeItems = storeItems + item.getId() + "," + item.getValue() + ";";
                            }
                        case PACKAGE_SELL:
                        case SELL:
                            title = player.getSellList().getTitle();
                            var7 = player.getSellList().getItems().iterator();

                            while (true) {
                                if (!var7.hasNext()) {
                                    break label69;
                                }

                                TradeItem item = (TradeItem) var7.next();
                                storeItems = storeItems + item.getObjectId() + "," + item.getCount() + "," + item.getPrice() + ";";
                            }
                        default:
                            System.out.println("NPE ->" + player.getStoreType().name());
                            return;
                    }

                    saveValue = true;
                } else if (PlayerData.get(player).isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE) {
                    title = "SellBuff";
                    storeItems = storeItems + PlayerData.get(player).getSellBuffPrice();
                    storeType = "SELL_BUFF";
                    saveValue = false;
                }

                if (saveValue) {
                    this.setValueDB(player.getObjectId(), "offlineShop", storeType + "#" + (title != null && title.length() != 0 ? title.replaceAll("#", " ") : "null") + "#" + storeItems);
                }
            } catch (Exception var9) {
                var9.printStackTrace();
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
        Iterator var1 = PlayerData.getAllPlayers().iterator();

        while (true) {
            PlayerHolder ph;
            String shop;
            do {
                if (!var1.hasNext()) {
                    return;
                }

                ph = (PlayerHolder) var1.next();
                shop = this.getValueDB(ph.getObjectId(), "offlineShop");
            } while (shop == null);

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
                    String[] var10;
                    int var11;
                    int var12;
                    String list;
                    List items;
                    switch (store) {
                        case BUY:
                            String[] var16 = shopItems.split(";");
                            int var17 = var16.length;

                            for (var11 = 0; var11 < var17; ++var11) {
                                list = var16[var11];
                                items = Util.parseInt(list, ",");
                                if (player.getBuyList().addItemByItemId((Integer) items.get(0), (Integer) items.get(1), (Integer) items.get(2)) == null) {
                                    throw new NullPointerException();
                                }
                            }

                            player.getBuyList().setTitle(shopTitle.equals("null") ? "" : shopTitle);
                            break;
                        case MANUFACTURE:
                            ManufactureList createList = new ManufactureList();
                            var10 = shopItems.split(";");
                            var11 = var10.length;

                            for (var12 = 0; var12 < var11; ++var12) {
                                list = var10[var12];
                                items = Util.parseInt(list, ",");
                                createList.add(new ManufactureItem((Integer) items.get(0), (Integer) items.get(1)));
                            }

                            player.setCreateList(createList);
                            player.getCreateList().setStoreName(shopTitle.equals("null") ? "" : shopTitle);
                            break;
                        case PACKAGE_SELL:
                        case SELL:
                            var10 = shopItems.split(";");
                            var11 = var10.length;

                            for (var12 = 0; var12 < var11; ++var12) {
                                list = var10[var12];
                                items = Util.parseInt(list, ",");
                                if (player.getSellList().addItem((Integer) items.get(0), (Integer) items.get(1), (Integer) items.get(2)) == null) {
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
            } catch (Exception var15) {
                LOG.log(Level.WARNING, this.getClass().getSimpleName() + ": Error loading trader: " + player, var15);
                var15.printStackTrace();
                if (player != null) {
                    player.deleteMe();
                }

                ph.setOffline(false);
            }
        }
    }

    private static class SingletonHolder {
        protected static final OfflineShop INSTANCE = new OfflineShop();
    }
}