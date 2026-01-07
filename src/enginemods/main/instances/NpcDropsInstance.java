package enginemods.main.instances;

import enginemods.main.enums.ItemDropType;
import enginemods.main.holders.DropBonusHolder;
import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.data.xml.HerbDropData;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.HashMap;
import java.util.Map;

public class NpcDropsInstance {
    private final Map<ItemDropType, DropBonusHolder> _dropsSettings = new HashMap<>();

    public NpcDropsInstance() {
        this._dropsSettings.put(ItemDropType.NORMAL, new DropBonusHolder());
        this._dropsSettings.put(ItemDropType.SPOIL, new DropBonusHolder());
        this._dropsSettings.put(ItemDropType.SEED, new DropBonusHolder());
        this._dropsSettings.put(ItemDropType.HERB, new DropBonusHolder());
    }

    public void increaseDrop(ItemDropType type, double chance, double amount) {
        this._dropsSettings.get(type).increaseAmountBonus(amount);
        this._dropsSettings.get(type).increaseChanceBonus(chance);
    }

    public boolean hasSettings() {
        for (DropBonusHolder holder : this._dropsSettings.values()) {
            if (holder.getAmountBonus() > 1.0D || holder.getChanceBonus() > 1.0D)
                return true;
        }
        return false;
    }

    public void init(Monster npc, Creature mainDamageDealer) {
        if (mainDamageDealer == null)
            return;
        Player player = mainDamageDealer.getActingPlayer();
        if (player == null)
            return;
        CursedWeaponManager.getInstance().checkDrop(npc, player);
        int levelModifier = calculateLevelModifierForDrop(npc, player);
        for (DropCategory cat : npc.getTemplate().getDropData()) {
            IntIntHolder item = null;
            if (cat.isSweep()) {
                if (npc.getSpoilerId() != 0)
                    for (DropData drop : cat.getAllDrops()) {
                        item = calculateRewardItem(npc, player, drop, levelModifier, true);
                        if (item == null)
                            continue;
                        npc.getSweepItems().add(item);
                    }
                continue;
            }
            if (npc.isSeeded()) {
                DropData drop = cat.dropSeedAllowedDropsOnly();
                if (drop == null)
                    continue;
                item = calculateRewardItem(npc, player, drop, levelModifier, false);
            } else {
                item = calculateCategorizedRewardItem(npc, player, cat, levelModifier);
            }
            if (item != null) {
                if ((npc.isRaidBoss() && Config.AUTO_LOOT_RAID) || (!npc.isRaidBoss() && Config.AUTO_LOOT)) {
                    player.doAutoLoot(npc, item);
                } else {
                    npc.dropItem(player, item);
                }
                if (npc.isRaidBoss() && !npc.isMinion())
                    npc.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addCharName(npc).addItemName(item.getId()).addNumber(item.getValue()));
            }
        }
        if (npc.getTemplate().getDropHerbGroup() > 0)
            for (DropCategory cat : HerbDropData.getInstance().getHerbDroplist(npc.getTemplate().getDropHerbGroup())) {
                IntIntHolder item = calculateCategorizedHerbItem(cat, levelModifier);
                if (item != null) {
                    if (Config.AUTO_LOOT_HERBS) {
                        player.addItem("Loot", item.getId(), 1, npc, true);
                        continue;
                    }
                    int count = item.getValue();
                    if (count > 1) {
                        item.setValue(1);
                        for (int i = 0; i < count; i++)
                            npc.dropItem(player, item);
                        continue;
                    }
                    npc.dropItem(player, item);
                }
            }
    }

    private IntIntHolder calculateRewardItem(Npc npc, Player lastAttacker, DropData drop, int levelModifier, boolean isSweep) {
        double dropChance = drop.getChance();
        if (Config.DEEPBLUE_DROP_RULES) {
            int deepBlueDrop = 1;
            if (levelModifier > 0) {
                deepBlueDrop = 3;
                if (drop.getItemId() == 57) {
                    deepBlueDrop *= (npc.isRaidBoss() && !npc.isMinion()) ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
                    if (deepBlueDrop == 0)
                        deepBlueDrop = 1;
                }
            }
            dropChance = ((drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop);
        }
        if (drop.getItemId() == 57) {
            dropChance *= Config.RATE_DROP_ADENA;
        } else if (isSweep) {
            dropChance *= Config.RATE_DROP_SPOIL;
        } else {
            dropChance *= (npc.isRaidBoss() && !npc.isMinion()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
        }
        dropChance *= this._dropsSettings.get(isSweep ? ItemDropType.SPOIL : ItemDropType.SEED).getChanceBonus();
        if (dropChance < 1.0D)
            dropChance = 1.0D;
        int minCount = drop.getMinDrop();
        int maxCount = drop.getMaxDrop();
        int itemCount = 0;
        int random = Rnd.get(1000000);
        while (random < dropChance) {
            if (minCount < maxCount) {
                itemCount += Rnd.get(minCount, maxCount);
            } else if (minCount == maxCount) {
                itemCount += minCount;
            } else {
                itemCount++;
            }
            dropChance -= 1000000.0D;
        }
        itemCount = (int) (itemCount * this._dropsSettings.get(ItemDropType.SPOIL).getAmountBonus());
        if (itemCount > 0)
            return new IntIntHolder(drop.getItemId(), itemCount);
        return null;
    }

    private IntIntHolder calculateCategorizedRewardItem(Npc npc, Player lastAttacker, DropCategory categoryDrops, int levelModifier) {
        if (categoryDrops == null)
            return null;
        int basecategoryDropChance = categoryDrops.getCategoryChance();
        int categoryDropChance = basecategoryDropChance;
        if (Config.DEEPBLUE_DROP_RULES) {
            int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
            categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
        }
        categoryDropChance = (int) (categoryDropChance * ((npc.isRaidBoss() && !npc.isMinion()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS));
        categoryDropChance = (int) (categoryDropChance * this._dropsSettings.get(ItemDropType.NORMAL).getChanceBonus());
        if (categoryDropChance < 1)
            categoryDropChance = 1;
        if (Rnd.get(1000000) < categoryDropChance) {
            DropData drop = categoryDrops.dropOne((npc.isRaidBoss() && !npc.isMinion()));
            if (drop == null)
                return null;
            double dropChance = drop.getChance();
            if (drop.getItemId() == 57) {
                dropChance *= Config.RATE_DROP_ADENA;
            } else {
                dropChance *= (npc.isRaidBoss() && !npc.isMinion()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
            }
            dropChance *= this._dropsSettings.get(ItemDropType.NORMAL).getChanceBonus();
            if (dropChance < 1000000.0D)
                dropChance = 1000000.0D;
            int min = drop.getMinDrop();
            int max = drop.getMaxDrop();
            int itemCount = 0;
            int random = Rnd.get(1000000);
            while (random < dropChance) {
                if (min < max) {
                    itemCount += Rnd.get(min, max);
                } else if (min == max) {
                    itemCount += min;
                } else {
                    itemCount++;
                }
                dropChance -= 1000000.0D;
            }
            itemCount = (int) (itemCount * this._dropsSettings.get(ItemDropType.NORMAL).getAmountBonus());
            if (itemCount > 0)
                return new IntIntHolder(drop.getItemId(), itemCount);
        }
        return null;
    }

    private int calculateLevelModifierForDrop(Npc npc, Player lastAttacker) {
        if (Config.DEEPBLUE_DROP_RULES) {
            int highestLevel = lastAttacker.getLevel();
            for (Creature atkChar : ((Attackable) npc).getAttackByList()) {
                if (atkChar.getLevel() > highestLevel)
                    highestLevel = atkChar.getLevel();
            }
            if (highestLevel - 9 >= npc.getLevel())
                return (highestLevel - npc.getLevel() + 8) * 9;
        }
        return 0;
    }

    private IntIntHolder calculateCategorizedHerbItem(DropCategory categoryDrops, int levelModifier) {
        if (categoryDrops == null)
            return null;
        int categoryDropChance = categoryDrops.getCategoryChance();
        switch (categoryDrops.getCategoryType()) {
            case 1:
                categoryDropChance = (int) (categoryDropChance * Config.RATE_DROP_HP_HERBS);
                break;
            case 2:
                categoryDropChance = (int) (categoryDropChance * Config.RATE_DROP_MP_HERBS);
                break;
            case 3:
                categoryDropChance = (int) (categoryDropChance * Config.RATE_DROP_SPECIAL_HERBS);
                break;
            default:
                categoryDropChance = (int) (categoryDropChance * Config.RATE_DROP_COMMON_HERBS);
                break;
        }
        categoryDropChance = (int) (categoryDropChance * this._dropsSettings.get(ItemDropType.HERB).getChanceBonus());
        if (Config.DEEPBLUE_DROP_RULES) {
            int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
            categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
        }
        if (Rnd.get(1000000) < Math.max(1, categoryDropChance)) {
            DropData drop = categoryDrops.dropOne(false);
            if (drop == null)
                return null;
            double dropChance = drop.getChance();
            switch (categoryDrops.getCategoryType()) {
                case 1:
                    dropChance *= Config.RATE_DROP_HP_HERBS;
                    break;
                case 2:
                    dropChance *= Config.RATE_DROP_MP_HERBS;
                    break;
                case 3:
                    dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
                    break;
                default:
                    dropChance *= Config.RATE_DROP_COMMON_HERBS;
                    break;
            }
            dropChance *= this._dropsSettings.get(ItemDropType.HERB).getChanceBonus();
            if (dropChance < 1000000.0D)
                dropChance = 1000000.0D;
            int min = drop.getMinDrop();
            int max = drop.getMaxDrop();
            int itemCount = 0;
            int random = Rnd.get(1000000);
            while (random < dropChance) {
                if (min < max) {
                    itemCount += Rnd.get(min, max);
                } else if (min == max) {
                    itemCount += min;
                } else {
                    itemCount++;
                }
                dropChance -= 1000000.0D;
            }
            itemCount = (int) (itemCount * this._dropsSettings.get(ItemDropType.HERB).getAmountBonus());
            if (itemCount > 0)
                return new IntIntHolder(drop.getItemId(), itemCount);
        }
        return null;
    }
}
