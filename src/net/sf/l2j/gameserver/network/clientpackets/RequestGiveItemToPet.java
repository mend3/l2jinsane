package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;

public final class RequestGiveItemToPet extends L2GameClientPacket {
    private int _objectId;

    private int _amount;

    protected void readImpl() {
        this._objectId = readD();
        this._amount = readD();
    }

    protected void runImpl() {
        if (this._amount <= 0)
            return;
        Player player = getClient().getPlayer();
        if (player == null || !player.hasPet())
            return;
        if (!Config.KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0) {
            player.sendMessage("You cannot trade in a chaotic state.");
            return;
        }
        if (player.isInStoreMode()) {
            player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
            return;
        }
        if (player.isProcessingTransaction()) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING);
            return;
        }
        ItemInstance item = player.getInventory().getItemByObjectId(this._objectId);
        if (item == null || item.isAugmented())
            return;
        if (item.isHeroItem() || !item.isDropable() || !item.isDestroyable() || !item.isTradable() || item.getItem().getItemType() == EtcItemType.ARROW || item.getItem().getItemType() == EtcItemType.SHOT) {
            player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
            return;
        }
        Pet pet = (Pet) player.getSummon();
        if (pet.isDead()) {
            player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
            return;
        }
        if (MathUtil.calculateDistance(player, pet, true) > 150.0D) {
            player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
            return;
        }
        if (!pet.getInventory().validateCapacity(item)) {
            player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
            return;
        }
        if (!pet.getInventory().validateWeight(item, this._amount)) {
            player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
            return;
        }
        if (player.getActiveEnchantItem() != null) {
            player.setActiveEnchantItem(null);
            player.sendPacket(EnchantResult.CANCELLED);
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        player.transferItem("Transfer", this._objectId, this._amount, pet.getInventory(), pet);
    }
}
