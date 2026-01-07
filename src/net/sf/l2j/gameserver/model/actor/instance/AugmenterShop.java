package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.AugmentationData;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

public class AugmenterShop extends Folk {
    private final int augmenterCoin = 57;

    public AugmenterShop(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("learn")) {
            String[] args = command.substring(6).split(" ");
            int id = Integer.parseInt(args[0]);
            int count = 2000;
            if (player.getInventory().getPaperdollItem(7) == null) {
                showChatWindow(player, 0);
                player.sendMessage("You need to equip a weapon to learn a skill.");
                return;
            }
            if (!player.getInventory().getPaperdollItem(7).isWeapon()) {
                showChatWindow(player, 0);
                player.sendMessage("You need to equip a weapon to learn a skill.");
                return;
            }
            if (player.getInventory().getPaperdollItem(7).getAugmentation() != null) {
                showChatWindow(player, 0);
                player.sendMessage("Your weapon is already augmented.");
                return;
            }
            if (player.getInventory().getItemByItemId(this.augmenterCoin) == null || player.getInventory().getItemByItemId(this.augmenterCoin).getCount() < count) {
                player.sendMessage("Incorrect item count.");
                return;
            }
            player.destroyItemByItemId("donation shop", this.augmenterCoin, count, this, true);
            ItemInstance wep = player.getInventory().getPaperdollItem(7);
            player.disarmWeapons();
            L2Augmentation aug = AugmentationData.getInstance().generateAugmentationWithSkill(id, SkillTable.getInstance().getMaxLevel(id));
            if (wep.isAugmented()) {
                wep.removeAugmentation();
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(wep);
                player.sendPacket(iu);
            }
            wep.setAugmentation(aug);
            InventoryUpdate iuu = new InventoryUpdate();
            iuu.addModifiedItem(wep);
            player.sendPacket(iuu);
            StatusUpdate su = new StatusUpdate(player);
            su.addAttribute(14, player.getCurrentLoad());
            player.sendPacket(su);
            showChatWindow(player, 0);
            player.store();
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/mods/augmentershop/" + filename + ".htm";
    }
}
