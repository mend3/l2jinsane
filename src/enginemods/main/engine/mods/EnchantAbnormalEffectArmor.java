package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class EnchantAbnormalEffectArmor extends AbstractMods {
    public EnchantAbnormalEffectArmor() {
        registerMod(ConfigData.ENABLE_EnchantAbnormalEffectArmor);
    }

    public static EnchantAbnormalEffectArmor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onEnchant(Creature player) {
        checkSetEffect(player);
    }

    public void onEquip(Creature player) {
        checkSetEffect(player);
    }

    public void onUnequip(Creature player) {
        checkSetEffect(player);
    }

    public boolean onExitWorld(Player player) {
        cancelTimer("customEffectSkill", null, player);
        return super.onExitWorld(player);
    }

    public void onTimer(String timerName, Npc npc, Player player) {
        switch (timerName) {
            case "customEffectSkill":
                if (player != null)
                    player.broadcastPacket(new MagicSkillUse(player, player, 4326, 1, 1000, 1000));
                break;
        }
    }

    private void checkSetEffect(Creature character) {
        if (!Util.areObjectType(Player.class, character))
            return;
        Player player = (Player) character;
        if (checkItems(player)) {
            startTimer("customEffectSkill", 2000L, null, player, true);
        } else {
            cancelTimer("customEffectSkill", null, player);
        }
    }

    private boolean checkItems(Player player) {
        PcInventory pcInventory = player.getInventory();
        ItemInstance chestItem = pcInventory.getPaperdollItem(10);
        if (chestItem == null)
            return false;
        ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
        if (armorSet == null)
            return false;
        if (chestItem.getEnchantLevel() < ConfigData.ENCHANT_EFFECT_LVL)
            return false;
        int legs = 0;
        int head = 0;
        int gloves = 0;
        int feet = 0;
        ItemInstance legsItem = pcInventory.getPaperdollItem(11);
        if (legsItem != null && legsItem.getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
            legs = legsItem.getItemId();
        if (armorSet.getSetItemsId()[1] != 0 && armorSet.getSetItemsId()[1] != legs)
            return false;
        ItemInstance headItem = pcInventory.getPaperdollItem(6);
        if (headItem != null && headItem.getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
            head = headItem.getItemId();
        if (armorSet.getSetItemsId()[2] != 0 && armorSet.getSetItemsId()[2] != head)
            return false;
        ItemInstance glovesItem = pcInventory.getPaperdollItem(9);
        if (glovesItem != null && glovesItem.getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
            gloves = glovesItem.getItemId();
        if (armorSet.getSetItemsId()[3] != 0 && armorSet.getSetItemsId()[3] != gloves)
            return false;
        ItemInstance feetItem = pcInventory.getPaperdollItem(12);
        if (feetItem != null && feetItem.getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
            feet = feetItem.getItemId();
        return armorSet.getSetItemsId()[4] == 0 || armorSet.getSetItemsId()[4] == feet;
    }

    private static class SingletonHolder {
        protected static final EnchantAbnormalEffectArmor INSTANCE = new EnchantAbnormalEffectArmor();
    }
}
