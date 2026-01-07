package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ActionType;
import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.type.L2MultiFunctionZone;
import net.sf.l2j.gameserver.model.zone.type.L2RandomZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public final class UseItem extends L2GameClientPacket {
    private int _objectId;

    private boolean _ctrlPressed;

    protected void readImpl() {
        this._objectId = readD();
        this._ctrlPressed = (readD() != 0);
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
            return;
        }
        if (activeChar.getActiveTradeList() != null) {
            activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
            return;
        }
        ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
        if (item == null)
            return;
        if (getClient().getActiveChar().isInsideZone(ZoneId.MULTI_FUNCTION) && !L2MultiFunctionZone.checkItem(item)) {
            getClient().getActiveChar().sendMessage("You cannot use " + item.getName() + " inside this zone.");
            return;
        }
        if (item.getItem().getType2() == 3) {
            activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
            return;
        }
        if (activeChar.isAlikeDead() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid())
            return;
        if (!EventListener.canUseItem(activeChar, item.getItemId()))
            return;
        if (Config.ENABLE_KILLS_LIMIT_A && activeChar.getPvpKills() < Config.PVP_A_GRADE && item
                .isEquipable())
            if (item.getItem().getCrystalType() == CrystalType.A) {
                activeChar.sendPacket(new PlaySound("ItemSound3.sys_fishing_failed"));
                activeChar.sendMessage("You need " + Config.PVP_A_GRADE + " pvp kills to equip A Grade item.");
                return;
            }
        if (Config.ENABLE_KILLS_LIMIT_S && activeChar.getPvpKills() < Config.PVP_S_GRADE && item
                .isEquipable())
            if (item.getItem().getCrystalType() == CrystalType.S) {
                activeChar.sendPacket(new PlaySound("ItemSound3.sys_fishing_failed"));
                activeChar.sendMessage("You need " + Config.PVP_S_GRADE + " pvp kills to equip S Grade item.");
                return;
            }
        if (activeChar.isInsideZone(ZoneId.RANDOMZONE) && !L2RandomZone.checkItem(item)) {
            activeChar.sendMessage("You cannot use " + item.getName() + " inside this zone.");
            return;
        }
        if (!Config.KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0) {
            IntIntHolder[] sHolders = item.getItem().getSkills();
            if (sHolders != null)
                for (IntIntHolder sHolder : sHolders) {
                    L2Skill skill = sHolder.getSkill();
                    if (skill != null && (skill.getSkillType() == L2SkillType.TELEPORT || skill.getSkillType() == L2SkillType.RECALL))
                        return;
                }
        }
        if (activeChar.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
            return;
        }
        if (item.isPetItem()) {
            if (!activeChar.hasPet()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
                return;
            }
            Pet pet = (Pet) activeChar.getSummon();
            if (!pet.canWear(item.getItem())) {
                activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
                return;
            }
            if (pet.isDead()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
                return;
            }
            if (!pet.getInventory().validateCapacity(item)) {
                activeChar.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
                return;
            }
            if (!pet.getInventory().validateWeight(item, 1)) {
                activeChar.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
                return;
            }
            activeChar.transferItem("Transfer", this._objectId, 1, pet.getInventory(), pet);
            if (item.isEquipped()) {
                pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
            } else {
                pet.getInventory().equipPetItem(item);
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
            }
            activeChar.sendPacket(new PetItemList(pet));
            pet.updateAndBroadcastStatus(1);
            return;
        }
        if (!item.isEquipped())
            if (!item.getItem().checkCondition(activeChar, activeChar, true))
                return;
        if (item.isEquipable()) {
            if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
                return;
            }
            switch (item.getItem().getBodyPart()) {
                case 128:
                case 256:
                case 16384:
                    if (activeChar.isMounted()) {
                        activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                        return;
                    }
                    if (activeChar.isCursedWeaponEquipped())
                        return;
                    break;
            }
            if (activeChar.isCursedWeaponEquipped() && item.getItemId() == 6408)
                return;
            if (activeChar.isAttackingNow()) {
                ThreadPool.schedule(() -> {
                    ItemInstance itemToTest = activeChar.getInventory().getItemByObjectId(this._objectId);
                    if (itemToTest == null)
                        return;
                    activeChar.useEquippableItem(itemToTest, false);
                }, activeChar

                        .getAttackEndTime() - System.currentTimeMillis());
            } else {
                activeChar.useEquippableItem(item, true);
            }
        } else {
            if (activeChar.isCastingNow() && !item.isPotion() && !item.isElixir())
                return;
            if (activeChar.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE) {
                activeChar.getInventory().setPaperdollItem(8, item);
                activeChar.broadcastUserInfo();
                sendPacket(new ItemList(activeChar, false));
                return;
            }
            IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
            if (handler != null)
                handler.useItem(activeChar, item, this._ctrlPressed);
            for (Quest quest : item.getQuestEvents()) {
                QuestState state = activeChar.getQuestState(quest.getName());
                if (state == null || !state.isStarted())
                    continue;
                quest.notifyItemUse(item, activeChar, activeChar.getTarget());
            }
        }
    }
}
