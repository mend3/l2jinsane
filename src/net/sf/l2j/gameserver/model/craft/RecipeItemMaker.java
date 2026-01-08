package net.sf.l2j.gameserver.model.craft;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

public class RecipeItemMaker implements Runnable {
    protected final Recipe _recipe;
    protected final Player _player;
    protected final Player _target;
    protected final int _skillId;
    protected final int _skillLevel;
    public boolean _isValid;
    protected final double _manaRequired;

    protected int _price;

    public RecipeItemMaker(Player player, Recipe recipe, Player target) {
        this._player = player;
        this._target = target;
        this._recipe = recipe;
        this._isValid = false;
        this._skillId = this._recipe.isDwarven() ? 172 : 1320;
        this._skillLevel = this._player.getSkillLevel(this._skillId);
        this._manaRequired = this._recipe.getMpCost();
        this._player.setCrafting(true);
        if (this._player.isAlikeDead() || this._target.isAlikeDead()) {
            this._player.sendPacket(ActionFailed.STATIC_PACKET);
            abort();
            return;
        }
        if (this._player.isProcessingTransaction() || this._target.isProcessingTransaction()) {
            this._target.sendPacket(ActionFailed.STATIC_PACKET);
            abort();
            return;
        }
        if (this._recipe.getLevel() > this._skillLevel) {
            this._player.sendPacket(ActionFailed.STATIC_PACKET);
            abort();
            return;
        }
        if (this._player != this._target)
            for (ManufactureItem temp : this._player.getCreateList().getList()) {
                if (temp.getId() == this._recipe.getId()) {
                    this._price = temp.getValue();
                    if (this._target.getAdena() < this._price) {
                        this._target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                        abort();
                        return;
                    }
                    break;
                }
            }
        if (!listItems(false)) {
            abort();
            return;
        }
        if (this._player.getCurrentMp() < this._manaRequired) {
            this._target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
            abort();
            return;
        }
        updateMakeInfo(true);
        updateStatus();
        this._player.setCrafting(false);
        this._isValid = true;
    }

    public void run() {
        if (!Config.IS_CRAFTING_ENABLED) {
            this._target.sendMessage("Item creation is currently disabled.");
            abort();
            return;
        }
        if (this._player == null || this._target == null) {
            abort();
            return;
        }
        if (!this._player.isOnline() || !this._target.isOnline()) {
            abort();
            return;
        }
        this._player.reduceCurrentMp(this._manaRequired);
        if (this._target != this._player && this._price > 0) {
            ItemInstance adenaTransfer = this._target.transferItem("PayManufacture", this._target.getInventory().getAdenaInstance().getObjectId(), this._price, this._player.getInventory(), this._player);
            if (adenaTransfer == null) {
                this._target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                abort();
                return;
            }
        }
        if (!listItems(true)) {
            abort();
            return;
        }
        if (Rnd.get(100) < this._recipe.getSuccessRate()) {
            rewardPlayer();
            updateMakeInfo(true);
        } else {
            if (this._target != this._player) {
                this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED).addCharName(this._target).addItemName(this._recipe.getProduct().getId()).addItemNumber(this._price));
                this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA).addCharName(this._player).addItemName(this._recipe.getProduct().getId()).addItemNumber(this._price));
            } else {
                this._target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
            }
            updateMakeInfo(false);
        }
        updateStatus();
        this._player.setCrafting(false);
        this._target.sendPacket(new ItemList(this._target, false));
    }

    private void updateMakeInfo(boolean success) {
        if (this._target == this._player) {
            this._target.sendPacket(new RecipeItemMakeInfo(this._recipe.getId(), this._target, success ? 1 : 0));
        } else {
            this._target.sendPacket(new RecipeShopItemInfo(this._player, this._recipe.getId()));
        }
    }

    private void updateStatus() {
        StatusUpdate su = new StatusUpdate(this._target);
        su.addAttribute(11, (int) this._target.getCurrentMp());
        su.addAttribute(14, this._target.getCurrentLoad());
        this._target.sendPacket(su);
    }

    private boolean listItems(boolean remove) {
        PcInventory pcInventory = this._target.getInventory();
        boolean gotAllMats = true;
        for (IntIntHolder material : this._recipe.getMaterials()) {
            int quantity = material.getValue();
            if (quantity > 0) {
                ItemInstance item = pcInventory.getItemByItemId(material.getId());
                if (item == null || item.getCount() < quantity) {
                    this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE).addItemName(material.getId()).addItemNumber((item == null) ? quantity : (quantity - item.getCount())));
                    gotAllMats = false;
                }
            }
        }
        if (!gotAllMats)
            return false;
        if (remove)
            for (IntIntHolder material : this._recipe.getMaterials()) {
                pcInventory.destroyItemByItemId("Manufacture", material.getId(), material.getValue(), this._target, this._player);
                if (material.getValue() > 1) {
                    this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(material.getId()).addItemNumber(material.getValue()));
                    continue;
                }
                this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(material.getId()));
            }
        return true;
    }

    private void abort() {
        updateMakeInfo(false);
        this._player.setCrafting(false);
    }

    private void rewardPlayer() {
        int itemId = this._recipe.getProduct().getId();
        int itemCount = this._recipe.getProduct().getValue();
        this._target.getInventory().addItem("Manufacture", itemId, itemCount, this._target, this._player);
        if (this._target != this._player)
            if (itemCount == 1) {
                this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_S1_FOR_S3_ADENA).addString(this._target.getName()).addItemName(itemId).addItemNumber(this._price));
                this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_FOR_S3_ADENA).addString(this._player.getName()).addItemName(itemId).addItemNumber(this._price));
            } else {
                this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA).addString(this._target.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(this._price));
                this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_S3_S_FOR_S4_ADENA).addString(this._player.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(this._price));
            }
        if (itemCount > 1) {
            this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount));
        } else {
            this._target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
        }
        updateMakeInfo(true);
    }
}
