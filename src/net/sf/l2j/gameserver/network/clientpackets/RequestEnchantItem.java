package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.EnchantTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.model.L2EnchantScroll;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

public final class RequestEnchantItem extends L2GameClientPacket {
    private int _objectId = 0;

    private static boolean isEnchantable(ItemInstance item) {
        if (item.isHeroItem() || item.isShadowItem() || item.isEtcItem() || item.getItem().getItemType() == WeaponType.FISHINGROD)
            return false;
        return item.getLocation() == ItemInstance.ItemLocation.INVENTORY || item.getLocation() == ItemInstance.ItemLocation.PAPERDOLL;
    }

    protected void readImpl() {
        this._objectId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null || this._objectId == 0)
            return;
        if (!activeChar.isOnline() || getClient().isDetached()) {
            activeChar.setActiveEnchantItem(null);
            return;
        }
        if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            return;
        }
        if (activeChar.getActiveTradeList() != null) {
            activeChar.cancelActiveTrade();
            activeChar.sendPacket(SystemMessageId.TRADE_ATTEMPT_FAILED);
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            return;
        }
        ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
        ItemInstance scroll = activeChar.getActiveEnchantItem();
        if (item == null || scroll == null) {
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            return;
        }
        L2EnchantScroll enchant = EnchantTable.getInstance().getEnchantScroll(scroll);
        if (enchant == null)
            return;
        if (!isEnchantable(item) || !enchant.isValid(item) || item.getOwnerId() != activeChar.getObjectId()) {
            activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            return;
        }
        scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
        if (scroll == null) {
            activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            return;
        }
        synchronized (item) {
            if (Rnd.get(100) < enchant.getChance(item)) {
                SystemMessage sm;
                if (item.getEnchantLevel() == 0) {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
                    activeChar.sendPacket(sm);
                } else {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
                    activeChar.sendPacket(sm);
                }
                sm.addItemName(item.getItemId());
                item.setEnchantLevel(item.getEnchantLevel() + 1);
                item.updateDatabase();
                if (item.isEquipped()) {
                    Item it = item.getItem();
                    if (it instanceof Weapon && item.getEnchantLevel() == 4) {
                        L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                        if (enchant4Skill != null) {
                            activeChar.addSkill(enchant4Skill, false);
                            activeChar.sendSkillList();
                        }
                    } else if (it instanceof net.sf.l2j.gameserver.model.item.kind.Armor && item.getEnchantLevel() == 6) {
                        ItemInstance chestItem = activeChar.getInventory().getPaperdollItem(10);
                        if (chestItem != null) {
                            ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
                            if (armorSet != null && armorSet.isEnchanted6(activeChar)) {
                                int skillId = armorSet.getEnchant6skillId();
                                if (skillId > 0) {
                                    L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
                                    if (skill != null) {
                                        activeChar.addSkill(skill, false);
                                        activeChar.sendSkillList();
                                    }
                                }
                            }
                        }
                    }
                }
                activeChar.sendPacket(EnchantResult.SUCCESS);
            } else {
                if (item.isEquipped()) {
                    Item it = item.getItem();
                    if (it instanceof Weapon && item.getEnchantLevel() >= 4) {
                        L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                        if (enchant4Skill != null) {
                            activeChar.removeSkill(enchant4Skill.getId(), false);
                            activeChar.sendSkillList();
                        }
                    } else if (it instanceof net.sf.l2j.gameserver.model.item.kind.Armor && item.getEnchantLevel() >= 6) {
                        ItemInstance chestItem = activeChar.getInventory().getPaperdollItem(10);
                        if (chestItem != null) {
                            ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
                            if (armorSet != null && armorSet.isEnchanted6(activeChar)) {
                                int skillId = armorSet.getEnchant6skillId();
                                if (skillId > 0) {
                                    activeChar.removeSkill(skillId, false);
                                    activeChar.sendSkillList();
                                }
                            }
                        }
                    }
                }
                if (!enchant.canBreak()) {
                    activeChar.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED);
                    if (!enchant.canMaintain()) {
                        item.setEnchantLevel(0);
                        item.updateDatabase();
                    }
                    activeChar.sendPacket(EnchantResult.UNSUCCESS);
                } else {
                    ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
                    if (destroyItem == null) {
                        activeChar.setActiveEnchantItem(null);
                        activeChar.sendPacket(EnchantResult.CANCELLED);
                        return;
                    }
                    int crystalType = item.getItem().getCrystalItemId();
                    ItemInstance crystals = null;
                    if (crystalType != 0) {
                        int crystalCount = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
                        if (crystalCount < 1)
                            crystalCount = 1;
                        crystals = activeChar.getInventory().addItem("Enchant", crystalType, crystalCount, activeChar, destroyItem);
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals.getItemId()).addItemNumber(crystalCount));
                    }
                    InventoryUpdate iu = new InventoryUpdate();
                    if (destroyItem.getCount() == 0) {
                        iu.addRemovedItem(destroyItem);
                    } else {
                        iu.addModifiedItem(destroyItem);
                    }
                    activeChar.sendPacket(iu);
                    World.getInstance().removeObject(destroyItem);
                    if (item.getEnchantLevel() > 0) {
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
                    } else {
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(item.getItemId()));
                    }
                    if (crystalType == 0) {
                        activeChar.sendPacket(EnchantResult.UNK_RESULT_4);
                    } else {
                        activeChar.sendPacket(EnchantResult.UNK_RESULT_1);
                    }
                    StatusUpdate su = new StatusUpdate(activeChar);
                    su.addAttribute(14, activeChar.getCurrentLoad());
                    activeChar.sendPacket(su);
                }
            }
            EngineModsManager.onEnchant(activeChar);
            activeChar.sendPacket(new ItemList(activeChar, false));
            activeChar.broadcastUserInfo();
            activeChar.setActiveEnchantItem(null);
        }
    }
}
