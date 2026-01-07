package net.sf.l2j.gameserver.model.itemcontainer.listeners;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;

public class ItemPassiveSkillsListener implements OnEquipListener {
    private static final ItemPassiveSkillsListener instance = new ItemPassiveSkillsListener();

    public static ItemPassiveSkillsListener getInstance() {
        return instance;
    }

    public void onEquip(int slot, ItemInstance item, Playable actor) {
        Player player = (Player) actor;
        Item it = item.getItem();
        boolean update = false;
        boolean updateTimeStamp = false;
        if (it instanceof Weapon) {
            if (item.isAugmented())
                item.getAugmentation().applyBonus(player);
            if (player.getSkillLevel(239) < it.getCrystalType().getId())
                return;
            if (item.getEnchantLevel() >= 4) {
                L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                if (enchant4Skill != null) {
                    player.addSkill(enchant4Skill, false);
                    update = true;
                }
            }
        }
        IntIntHolder[] skills = it.getSkills();
        if (skills != null)
            for (IntIntHolder skillInfo : skills) {
                if (skillInfo != null) {
                    L2Skill itemSkill = skillInfo.getSkill();
                    if (itemSkill != null) {
                        player.addSkill(itemSkill, false);
                        if (itemSkill.isActive()) {
                            if (!player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode())) {
                                int equipDelay = itemSkill.getEquipDelay();
                                if (equipDelay > 0) {
                                    player.addTimeStamp(itemSkill, equipDelay);
                                    player.disableSkill(itemSkill, equipDelay);
                                }
                            }
                            updateTimeStamp = true;
                        }
                        update = true;
                    }
                }
            }
        if (update) {
            player.sendSkillList();
            if (updateTimeStamp)
                player.sendPacket(new SkillCoolTime(player));
        }
    }

    public void onUnequip(int slot, ItemInstance item, Playable actor) {
        Player player = (Player) actor;
        Item it = item.getItem();
        boolean update = false;
        if (it instanceof Weapon) {
            if (item.isAugmented())
                item.getAugmentation().removeBonus(player);
            if (item.getEnchantLevel() >= 4) {
                L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                if (enchant4Skill != null) {
                    player.removeSkill(enchant4Skill.getId(), false, (enchant4Skill.isPassive() || enchant4Skill.isToggle()));
                    update = true;
                }
            }
        }
        IntIntHolder[] skills = it.getSkills();
        if (skills != null)
            for (IntIntHolder skillInfo : skills) {
                if (skillInfo != null) {
                    L2Skill itemSkill = skillInfo.getSkill();
                    if (itemSkill != null) {
                        boolean found = false;
                        for (ItemInstance pItem : player.getInventory().getPaperdollItems()) {
                            if (pItem != null && it.getItemId() == pItem.getItemId()) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            player.removeSkill(itemSkill.getId(), false, (itemSkill.isPassive() || itemSkill.isToggle()));
                            update = true;
                        }
                    }
                }
            }
        if (update)
            player.sendSkillList();
    }
}
