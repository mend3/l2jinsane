package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class RequestDestroyItem extends L2GameClientPacket {
    private static final String DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?";

    private int _objectId;

    private int _count;

    protected void readImpl() {
        this._objectId = readD();
        this._count = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isProcessingTransaction() || player.isInStoreMode()) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
            return;
        }
        ItemInstance itemToRemove = player.getInventory().getItemByObjectId(this._objectId);
        if (itemToRemove == null)
            return;
        if (this._count < 1 || this._count > itemToRemove.getCount()) {
            player.sendPacket(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT);
            return;
        }
        if (!itemToRemove.isStackable() && this._count > 1)
            return;
        int itemId = itemToRemove.getItemId();
        if (player.isCastingNow() && player.getCurrentSkill().getSkill() != null && player.getCurrentSkill().getSkill().getItemConsumeId() == itemId) {
            player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            return;
        }
        if (player.isCastingSimultaneouslyNow() && player.getLastSimultaneousSkillCast() != null && player.getLastSimultaneousSkillCast().getItemConsumeId() == itemId) {
            player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            return;
        }
        if (!itemToRemove.isDestroyable() || CursedWeaponManager.getInstance().isCursed(itemId)) {
            player.sendPacket(itemToRemove.isHeroItem() ? SystemMessageId.HERO_WEAPONS_CANT_DESTROYED : SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            return;
        }
        if (itemToRemove.isEquipped() && (!itemToRemove.isStackable() || (itemToRemove.isStackable() && this._count >= itemToRemove.getCount()))) {
            ItemInstance[] unequipped = player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance item : unequipped) {
                item.unChargeAllShots();
                iu.addModifiedItem(item);
            }
            player.sendPacket(iu);
            player.broadcastUserInfo();
        }
        if (itemToRemove.getItemType() == EtcItemType.PET_COLLAR) {
            if ((player.getSummon() != null && player.getSummon().getControlItemId() == this._objectId) || (player.isMounted() && player.getMountObjectId() == this._objectId)) {
                player.sendPacket(SystemMessageId.PET_SUMMONED_MAY_NOT_DESTROYED);
                return;
            }
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
                    try {
                        ps.setInt(1, this._objectId);
                        ps.execute();
                        if (ps != null)
                            ps.close();
                    } catch (Throwable throwable) {
                        if (ps != null)
                            try {
                                ps.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (con != null)
                        con.close();
                } catch (Throwable throwable) {
                    if (con != null)
                        try {
                            con.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't delete pet item with objectid {}.", e, Integer.valueOf(this._objectId));
            }
        }
        player.destroyItem("Destroy", this._objectId, this._count, player, true);
    }
}
