package net.sf.l2j.gameserver.model.itemcontainer.listeners;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ArmorSetListener implements OnEquipListener {
    private static final ArmorSetListener instance = new ArmorSetListener();

    public static ArmorSetListener getInstance() {
        return instance;
    }

    public void onEquip(int slot, ItemInstance item, Playable actor) {
        if (!item.isEquipable())
            return;
        Player player = (Player) actor;
        if (item.getItem().getBodyPart() == 131072) {
            player.sendSkillList();
            return;
        }
        ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
        if (chestItem == null)
            return;
        ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
        if (armorSet == null)
            return;
        if (armorSet.containItem(slot, item.getItemId())) {
            if (armorSet.containAll(player)) {
                L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
                if (skill != null) {
                    player.addSkill(SkillTable.getInstance().getInfo(3006, 1), false);
                    player.addSkill(skill, false);
                    player.sendSkillList();
                }
                if (armorSet.containShield(player)) {
                    L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
                    if (skills != null) {
                        player.addSkill(skills, false);
                        player.sendSkillList();
                    }
                }
                if (armorSet.isEnchanted6(player)) {
                    int skillId = armorSet.getEnchant6skillId();
                    if (skillId > 0) {
                        L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
                        if (skille != null) {
                            player.addSkill(skille, false);
                            player.sendSkillList();
                        }
                    }
                }
            }
        } else if (armorSet.containShield(item.getItemId())) {
            if (armorSet.containAll(player)) {
                L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
                if (skills != null) {
                    player.addSkill(skills, false);
                    player.sendSkillList();
                }
            }
        }
    }

    public void onUnequip(int slot, ItemInstance item, Playable actor) {
        Player player = (Player) actor;
        if (item.getItem().getBodyPart() == 131072) {
            player.sendSkillList();
            return;
        }
        boolean remove = false;
        int removeSkillId1 = 0;
        int removeSkillId2 = 0;
        int removeSkillId3 = 0;
        if (slot == 10) {
            ArmorSet armorSet = ArmorSetData.getInstance().getSet(item.getItemId());
            if (armorSet == null)
                return;
            remove = true;
            removeSkillId1 = armorSet.getSkillId();
            removeSkillId2 = armorSet.getShieldSkillId();
            removeSkillId3 = armorSet.getEnchant6skillId();
        } else {
            ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
            if (chestItem == null)
                return;
            ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
            if (armorSet == null)
                return;
            if (armorSet.containItem(slot, item.getItemId())) {
                remove = true;
                removeSkillId1 = armorSet.getSkillId();
                removeSkillId2 = armorSet.getShieldSkillId();
                removeSkillId3 = armorSet.getEnchant6skillId();
            } else if (armorSet.containShield(item.getItemId())) {
                remove = true;
                removeSkillId2 = armorSet.getShieldSkillId();
            }
        }
        if (remove) {
            if (removeSkillId1 != 0) {
                player.removeSkill(3006, false);
                player.removeSkill(removeSkillId1, false);
            }
            if (removeSkillId2 != 0)
                player.removeSkill(removeSkillId2, false);
            if (removeSkillId3 != 0)
                player.removeSkill(removeSkillId3, false);
            player.sendSkillList();
        }
    }
}
