package mods.newbies;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class NewbiesNpc {
    public static void giveItems(int Classes, Player player) {
        ItemInstance items;
        int[] DaggerArmors = {5618, 2395, 5787, 5775, 2419, 858, 858, 889, 889, 920};
        int[] ArcherArmors = {4831, 2395, 5787, 5775, 2419, 858, 858, 889, 889, 920};
        int[] MageArmors = {
                5643, 2407, 5767, 5779, 6377, 512, 858, 858, 889, 889,
                920};
        int[] DuelistArmor = {
                8588, 2382, 5768, 5780, 547, 547, 858, 858, 889, 889,
                920};
        int[] TitanArmor = {
                5646, 2382, 5768, 5780, 547, 547, 858, 858, 889, 889,
                920};
        int[] GrandKhaArmors = {5623, 2395, 5787, 5775, 2419, 858, 858, 889, 889, 920};
        int[] TankArmors = {
                5647, 2382, 5768, 5780, 547, 6377, 547, 858, 858, 889,
                889, 920};
        int[] DwarfArmors = {
                5603, 2382, 5768, 5780, 547, 6377, 547, 858, 858, 889,
                889, 920};
        int[] DreadArmors = {5634, 2382, 5768, 5780, 547, 858, 858, 889, 889, 920};
        int[] DancerArmors = {6580, 2395, 5787, 5775, 2419, 858, 858, 889, 889, 920};
        ClassId classes = player.getClassId();
        switch (classes) {
            case ADVENTURER:
            case WIND_RIDER:
            case GHOST_HUNTER:
                if (DaggerArmors.length == 0)
                    return;
                items = null;
                for (int id : DaggerArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case SAGGITARIUS:
            case GHOST_SENTINEL:
            case MOONLIGHT_SENTINEL:
                if (ArcherArmors.length == 0)
                    return;
                for (int id : ArcherArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case SOULTAKER:
            case MYSTIC_MUSE:
            case ARCHMAGE:
            case ARCANA_LORD:
            case ELEMENTAL_MASTER:
            case CARDINAL:
            case STORM_SCREAMER:
            case SPECTRAL_MASTER:
            case SHILLIEN_SAINT:
            case DOMINATOR:
            case DOOMCRYER:
            case HIEROPHANT:
                if (MageArmors.length == 0)
                    return;
                for (int id : MageArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case DUELIST:
                if (DuelistArmor.length == 0)
                    return;
                for (int id : DuelistArmor) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case TITAN:
                if (TitanArmor.length == 0)
                    return;
                for (int id : TitanArmor) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case GRAND_KHAVATARI:
                if (GrandKhaArmors.length == 0)
                    return;
                for (int id : GrandKhaArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case PHOENIX_KNIGHT:
            case HELL_KNIGHT:
            case EVAS_TEMPLAR:
            case SHILLIEN_TEMPLAR:
                if (TankArmors.length == 0)
                    return;
                for (int id : TankArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case FORTUNE_SEEKER:
            case MAESTRO:
                if (DwarfArmors.length == 0)
                    return;
                for (int id : DwarfArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case DREADNOUGHT:
                if (DreadArmors.length == 0)
                    return;
                for (int id : DreadArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
            case SPECTRAL_DANCER:
            case SWORD_MUSE:
                if (DancerArmors.length == 0)
                    return;
                for (int id : DancerArmors) {
                    player.getInventory().addItem("Armors", id, 1, player, null);
                    items = player.getInventory().getItemByItemId(id);
                    player.getInventory().equipItemAndRecord(items);
                }
                break;
        }
        player.sendPacket(new ItemList(player, false));
        player.sendPacket(new InventoryUpdate());
        player.sendPacket(new EtcStatusUpdate(player));
        player.refreshOverloaded();
        player.refreshExpertisePenalty();
        player.sendPacket(new UserInfo(player));
        player.broadcastUserInfo();
    }
}
