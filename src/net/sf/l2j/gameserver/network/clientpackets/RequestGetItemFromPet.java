package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;

public final class RequestGetItemFromPet extends L2GameClientPacket {
    private int _objectId;

    private int _amount;

    private int _unknown;

    protected void readImpl() {
        this._objectId = readD();
        this._amount = readD();
        this._unknown = readD();
    }

    protected void runImpl() {
        if (this._amount <= 0)
            return;
        Player player = getClient().getPlayer();
        if (player == null || !player.hasPet())
            return;
        if (player.isProcessingTransaction()) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING);
            return;
        }
        if (player.getActiveEnchantItem() != null) {
            player.setActiveEnchantItem(null);
            player.sendPacket(EnchantResult.CANCELLED);
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        Pet pet = (Pet) player.getSummon();
        pet.transferItem("Transfer", this._objectId, this._amount, player.getInventory(), player, pet);
    }
}
