package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public class AdminEnchant implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{
            "admin_seteh", "admin_setec", "admin_seteg", "admin_setel", "admin_seteb", "admin_setew", "admin_setes", "admin_setle", "admin_setre", "admin_setlf",
            "admin_setrf", "admin_seten", "admin_setun", "admin_setba", "admin_enchant"};

    private static void setEnchant(Player activeChar, int ench, int armorType) {
        Player player1 = null;
        WorldObject target = activeChar.getTarget();
        if (!(target instanceof Player))
            player1 = activeChar;
        Player player = player1;
        ItemInstance item = player.getInventory().getPaperdollItem(armorType);
        if (item != null && item.getLocationSlot() == armorType) {
            Item it = item.getItem();
            int oldEnchant = item.getEnchantLevel();
            item.setEnchantLevel(ench);
            item.updateDatabase();
            if (item.isEquipped()) {
                int currentEnchant = item.getEnchantLevel();
                if (it instanceof Weapon) {
                    if (oldEnchant >= 4 && currentEnchant < 4) {
                        L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                        if (enchant4Skill != null) {
                            player.removeSkill(enchant4Skill.getId(), false);
                            player.sendSkillList();
                        }
                    } else if (oldEnchant < 4 && currentEnchant >= 4) {
                        L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
                        if (enchant4Skill != null) {
                            player.addSkill(enchant4Skill, false);
                            player.sendSkillList();
                        }
                    }
                } else if (it instanceof net.sf.l2j.gameserver.model.item.kind.Armor) {
                    if (oldEnchant >= 6 && currentEnchant < 6) {
                        ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
                        if (chestItem != null) {
                            ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
                            if (armorSet != null) {
                                int skillId = armorSet.getEnchant6skillId();
                                if (skillId > 0) {
                                    player.removeSkill(skillId, false);
                                    player.sendSkillList();
                                }
                            }
                        }
                    } else if (oldEnchant < 6 && currentEnchant >= 6) {
                        ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
                        if (chestItem != null) {
                            ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
                            if (armorSet != null && armorSet.isEnchanted6(player)) {
                                int skillId = armorSet.getEnchant6skillId();
                                if (skillId > 0) {
                                    L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
                                    if (skill != null) {
                                        player.addSkill(skill, false);
                                        player.sendSkillList();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            player.sendPacket(new ItemList(player, false));
            player.broadcastUserInfo();
            activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + it.getName() + " from " + oldEnchant + " to " + ench + ".");
            if (player != activeChar)
                player.sendMessage("A GM has changed the enchantment of your " + it.getName() + " from " + oldEnchant + " to " + ench + ".");
        }
    }

    private static void showMainPage(Player activeChar) {
        AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_enchant")) {
            showMainPage(activeChar);
        } else {
            int armorType = -1;
            if (command.startsWith("admin_seteh")) {
                armorType = 6;
            } else if (command.startsWith("admin_setec")) {
                armorType = 10;
            } else if (command.startsWith("admin_seteg")) {
                armorType = 9;
            } else if (command.startsWith("admin_seteb")) {
                armorType = 12;
            } else if (command.startsWith("admin_setel")) {
                armorType = 11;
            } else if (command.startsWith("admin_setew")) {
                armorType = 7;
            } else if (command.startsWith("admin_setes")) {
                armorType = 8;
            } else if (command.startsWith("admin_setle")) {
                armorType = 1;
            } else if (command.startsWith("admin_setre")) {
                armorType = 2;
            } else if (command.startsWith("admin_setlf")) {
                armorType = 4;
            } else if (command.startsWith("admin_setrf")) {
                armorType = 5;
            } else if (command.startsWith("admin_seten")) {
                armorType = 3;
            } else if (command.startsWith("admin_setun")) {
                armorType = 0;
            } else if (command.startsWith("admin_setba")) {
                armorType = 13;
            }
            if (armorType != -1)
                try {
                    int ench = Integer.parseInt(command.substring(12));
                    if (ench < 0 || ench > 65535) {
                        activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
                    } else {
                        setEnchant(activeChar, ench, armorType);
                    }
                } catch (Exception e) {
                    activeChar.sendMessage("Please specify a new enchant value.");
                }
            showMainPage(activeChar);
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
