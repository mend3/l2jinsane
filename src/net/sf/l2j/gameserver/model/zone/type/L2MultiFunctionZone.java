/**/
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

import java.util.Iterator;

public class L2MultiFunctionZone extends SpawnZoneType {
    static String[] gradeNames = new String[]{"", "D", "C", "B", "A", "S"};
    L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);

    public L2MultiFunctionZone(int id) {
        super(id);
    }

    static void heal(Player activeChar) {
        activeChar.setCurrentHp(activeChar.getMaxHp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        activeChar.setCurrentMp(activeChar.getMaxMp());
    }

    private static void clear(Player player) {
        Summon pet;
        if (Config.REMOVE_BUFFS) {
            player.stopAllEffects();
            if (Config.REMOVE_PETS) {
                pet = player.getSummon();
                if (pet != null) {
                    pet.stopAllEffects();
                    pet.unSummon(player);
                }
            }
        } else if (Config.REMOVE_PETS) {
            pet = player.getSummon();
            if (pet != null) {
                pet.unSummon(player);
            }
        }

    }

    public static void givereward(Player player) {
        if (player.isInsideZone(ZoneId.MULTI_FUNCTION)) {
            Iterator var1 = Config.REWARDS.keySet().iterator();

            while (var1.hasNext()) {
                int reward = (Integer) var1.next();
                player.addItem("PvP Zone", reward, Config.REWARDS.get(reward), null, true);
            }
        }

        player.mikadoPlayerUpdate();
    }

    public static boolean checkItem(ItemInstance item) {
        int o = item.getItem().getCrystalType().getId();
        int e = item.getEnchantLevel();
        if (Config.ENCHANT != 0 && e >= Config.ENCHANT) {
            return false;
        } else if (Config.GRADES.contains(gradeNames[o])) {
            return false;
        } else {
            return Config.ITEMS == null || !Config.ITEMS.contains(item.getItemId());
        }
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player activeChar) {
            ((Player) character).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
            if (Config.CLASSES != null && Config.CLASSES.contains(activeChar.getClassId().getId())) {
                activeChar.teleToLocation(new Location(83597, 147888, -3405));
                activeChar.sendMessage("Your class is not allowed in the MultiFunction zone.");
                return;
            }

            Iterator var3 = activeChar.getInventory().getItems().iterator();

            while (var3.hasNext()) {
                ItemInstance o = (ItemInstance) var3.next();
                if (o.isEquipable() && o.isEquipped() && !checkItem(o)) {
                    int slot = activeChar.getInventory().getSlotFromItem(o);
                    activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
                    activeChar.sendMessage(o.getItemName() + " unequiped because is not allowed inside this zone.");
                }
            }

            if (Config.GIVE_NOBLES) {
                this.noblesse.getEffects(activeChar, activeChar);
            }

            if (Config.PVP_ENABLED) {
                if (activeChar.getPvpFlag() > 0) {
                    PvpFlagTaskManager.getInstance().remove(activeChar);
                }

                activeChar.updatePvPFlag(1);
            }

            activeChar.mikadoPlayerUpdate();
            activeChar.sendMessage("You entered in a MultiFunction zone.");
            clear(activeChar);
        }

        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.MULTI_FUNCTION, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.MULTI_FUNCTION, false);
        if (character instanceof Player activeChar) {
            ((Player) character).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
            activeChar.sendMessage("You left from a MultiFunction zone.");
            if (Config.PVP_ENABLED) {
                activeChar.updatePvPFlag(0);
            }
        }

    }

    public void onDieInside(Creature character) {
        if (character instanceof Player activeChar) {
            if (Config.REVIVE) {
                ThreadPool.schedule(() -> {
                    activeChar.doRevive();
                    L2MultiFunctionZone.heal(activeChar);
                    int[] loc = Config.SPAWN_LOC[Rnd.get(Config.SPAWN_LOC.length)];
                    activeChar.teleToLocation(new Location(loc[0] + Rnd.get(-Config.RADIUS, Config.RADIUS), loc[1] + Rnd.get(-Config.RADIUS, Config.RADIUS), loc[2]));
                }, Config.REVIVE_DELAY * 1000L);
            }
        }

    }

    public void onReviveInside(Creature character) {
        if (character instanceof Player activeChar) {
            if (Config.REVIVE_NOBLES) {
                this.noblesse.getEffects(activeChar, activeChar);
            }

            if (Config.REVIVE_HEAL) {
                heal(activeChar);
            }
        }

    }
}