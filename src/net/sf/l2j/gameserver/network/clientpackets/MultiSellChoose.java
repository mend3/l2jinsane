package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class MultiSellChoose extends L2GameClientPacket {
    private static final int CLAN_REPUTATION = 65336;

    private static final int PC_BANG_POINTS = Config.PCB_COIN_ID;

    private int _listId;

    private int _entryId;

    private int _amount;

    protected void readImpl() {
        this._listId = readD();
        this._entryId = readD();
        this._amount = readD();
    }

    public void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.MULTISELL)) {
            player.setMultiSell(null);
            return;
        }
        if (this._amount < 1 || this._amount > 9999) {
            player.setMultiSell(null);
            return;
        }
        PreparedListContainer list = player.getMultiSell();
        if (list == null || list.getId() != this._listId) {
            player.setMultiSell(null);
            return;
        }
        if (this._entryId < 1 || this._entryId > list.getEntries().size()) {
            player.setMultiSell(null);
            return;
        }
        Folk folk = player.getCurrentFolk();
        if ((folk != null && !list.isNpcAllowed(folk.getNpcId())) || (folk == null && list.isNpcOnly())) {
            player.setMultiSell(null);
            return;
        }
        if (folk != null && !folk.canInteract(player)) {
            player.setMultiSell(null);
            return;
        }
        PcInventory inv = player.getInventory();
        Entry entry = list.getEntries().get(this._entryId - 1);
        if (entry == null) {
            player.setMultiSell(null);
            return;
        }
        if (!entry.isStackable() && this._amount > 1) {
            player.setMultiSell(null);
            return;
        }
        int slots = 0;
        int weight = 0;
        for (Ingredient e : entry.getProducts()) {
            if (e.getItemId() < 0)
                continue;
            if (!e.isStackable()) {
                slots += e.getItemCount() * this._amount;
            } else if (player.getInventory().getItemByItemId(e.getItemId()) == null) {
                slots++;
            }
            weight += e.getItemCount() * this._amount * e.getWeight();
        }
        if (!inv.validateWeight(weight)) {
            player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
            return;
        }
        if (!inv.validateCapacity(slots)) {
            player.sendPacket(SystemMessageId.SLOTS_FULL);
            return;
        }
        List<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
        for (Ingredient e : entry.getIngredients()) {
            boolean newIng = true;
            for (int i = ingredientsList.size(); --i >= 0; ) {
                Ingredient ex = ingredientsList.get(i);
                if (ex.getItemId() == e.getItemId() && ex.getEnchantLevel() == e.getEnchantLevel()) {
                    if (ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE) {
                        player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                        return;
                    }
                    Ingredient ing = ex.getCopy();
                    ing.setItemCount(ex.getItemCount() + e.getItemCount());
                    ingredientsList.set(i, ing);
                    newIng = false;
                    break;
                }
            }
            if (newIng)
                ingredientsList.add(e);
        }
        for (Ingredient e : ingredientsList) {
            if (e.getItemCount() * this._amount > Integer.MAX_VALUE) {
                player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                return;
            }
            if (e.getItemId() == 65336) {
                if (player.getClan() == null) {
                    player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
                    return;
                }
                if (!player.isClanLeader()) {
                    player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
                    return;
                }
                if (player.getClan().getReputationScore() < e.getItemCount() * this._amount) {
                    player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    return;
                }
                continue;
            }
            if (e.getItemId() == PC_BANG_POINTS) {
                if (player.getPcBang() < e.getItemCount() * this._amount) {
                    player.sendMessage("You don't have enough Territory War Points.");
                    return;
                }
                continue;
            }
            if (inv.getInventoryItemCount(e.getItemId(), (list.getMaintainEnchantment() || e.getEnchantLevel() > 0) ? e.getEnchantLevel() : -1, false) < ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) ? (e.getItemCount() * this._amount) : e.getItemCount())) {
                player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                return;
            }
        }
        List<L2Augmentation> augmentation = new ArrayList<>();
        for (Ingredient e : entry.getIngredients()) {
            if (e.getItemId() == 65336) {
                int amount = e.getItemCount() * this._amount;
                player.getClan().takeReputationScore(amount);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(amount));
                continue;
            }
            if (e.getItemId() == PC_BANG_POINTS) {
                int totalTWPoints = e.getItemCount() * this._amount;
                player.setPcBang(player.getPcBang() - totalTWPoints);
                continue;
            }
            ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());
            if (itemToTake == null) {
                player.setMultiSell(null);
                return;
            }
            if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) {
                if (itemToTake.isStackable()) {
                    if (!player.destroyItem("Multisell", itemToTake.getObjectId(), e.getItemCount() * this._amount, player.getTarget(), true)) {
                        player.setMultiSell(null);
                        return;
                    }
                    continue;
                }
                if (list.getMaintainEnchantment() || e.getEnchantLevel() > 0) {
                    ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantLevel(), false);
                    for (int j = 0; j < e.getItemCount() * this._amount; j++) {
                        if (inventoryContents[j].isAugmented())
                            augmentation.add(inventoryContents[j].getAugmentation());
                        if (!player.destroyItem("Multisell", inventoryContents[j].getObjectId(), 1, player.getTarget(), true)) {
                            player.setMultiSell(null);
                            return;
                        }
                    }
                    continue;
                }
                for (int i = 1; i <= e.getItemCount() * this._amount; i++) {
                    ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), false);
                    itemToTake = inventoryContents[0];
                    if (itemToTake.getEnchantLevel() > 0)
                        for (ItemInstance item : inventoryContents) {
                            if (item.getEnchantLevel() < itemToTake.getEnchantLevel()) {
                                itemToTake = item;
                                if (itemToTake.getEnchantLevel() == 0)
                                    break;
                            }
                        }
                    if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true)) {
                        player.setMultiSell(null);
                        return;
                    }
                }
            }
        }
        for (Ingredient e : entry.getProducts()) {
            SystemMessage sm;
            if (e.getItemId() == 65336) {
                player.getClan().addReputationScore(e.getItemCount() * this._amount);
                continue;
            }
            if (e.getItemId() == PC_BANG_POINTS) {
                player.setPcBang(player.getPcBang() + e.getItemCount() * this._amount);
                continue;
            }
            if (e.isStackable()) {
                inv.addItem("Multisell", e.getItemId(), e.getItemCount() * this._amount, player, player.getTarget());
            } else {
                for (int i = 0; i < e.getItemCount() * this._amount; i++) {
                    ItemInstance product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
                    if ((product != null && list.getMaintainEnchantment()) || (product != null && e.getEnchantLevel() > 0)) {
                        if (i < augmentation.size())
                            product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill()));
                        product.setEnchantLevel(e.getEnchantLevel());
                        product.updateDatabase();
                    }
                }
            }
            if (e.getItemCount() * this._amount > 1) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addNumber(e.getItemCount() * this._amount);
            } else if (list.getMaintainEnchantment() && e.getEnchantLevel() > 0) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(e.getEnchantLevel()).addItemName(e.getItemId());
            } else {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(e.getItemId());
            }
            player.sendPacket(sm);
        }
        player.sendPacket(new ItemList(player, false));
        player.sendPacket(SystemMessageId.SUCCESSFULLY_TRADED_WITH_NPC);
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
        if (folk != null && entry.getTaxAmount() > 0)
            folk.getCastle().addToTreasury(entry.getTaxAmount() * this._amount);
    }
}
