package net.sf.l2j.gameserver.handler.itemhandlers;

import enginemods.main.holders.RewardHolder;
import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class RewardBox implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        int itemId = item.getItemId();
        if (itemId == Config.REWARD_BOX_ID) {
            MagicSkillUse MSU;
            switch (Rnd.get(2)) {
                case 0:
                    activeChar.sendMessage("Ohh Noo! Your box is empty.");
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
                case 1:
                    for (RewardHolder reward : Config.REWARD_BOX_REWARDS) {
                        if (Rnd.get(100) <= reward.getRewardChance())
                            activeChar.addItem("Reward", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), activeChar, true);
                    }
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
            }
        }
        if (itemId == Config.REWARD_BOX_ID_1) {
            MagicSkillUse MSU;
            switch (Rnd.get(2)) {
                case 0:
                    activeChar.sendMessage("Ohh Noo! Your box is empty.");
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
                case 1:
                    for (RewardHolder reward : Config.REWARD_BOX_REWARDS_1) {
                        if (Rnd.get(100) <= reward.getRewardChance())
                            activeChar.addItem("Reward", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), activeChar, true);
                    }
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
            }
        }
        if (itemId == Config.REWARD_BOX_ID_2) {
            MagicSkillUse MSU;
            switch (Rnd.get(2)) {
                case 0:
                    activeChar.sendMessage("Ohh Noo! Your box is empty.");
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
                case 1:
                    for (RewardHolder reward : Config.REWARD_BOX_REWARDS_2) {
                        if (Rnd.get(100) <= reward.getRewardChance())
                            activeChar.addItem("Reward", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), activeChar, true);
                    }
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
            }
        }
        if (itemId == Config.REWARD_BOX_ID_3) {
            MagicSkillUse MSU;
            switch (Rnd.get(2)) {
                case 0:
                    activeChar.sendMessage("Ohh Noo! Your box is empty.");
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
                case 1:
                    for (RewardHolder reward : Config.REWARD_BOX_REWARDS_3) {
                        if (Rnd.get(100) <= reward.getRewardChance())
                            activeChar.addItem("Reward", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), activeChar, true);
                    }
                    playable.destroyItem("Consume", item.getObjectId(), 1, null, true);
                    MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                    activeChar.broadcastPacket(MSU);
                    break;
            }
        }
    }
}
