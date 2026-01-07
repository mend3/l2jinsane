package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public class Harvest implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.HARVEST};

    private static boolean calcSuccess(Creature activeChar, Creature target) {
        int basicSuccess = 100;
        int levelPlayer = activeChar.getLevel();
        int levelTarget = target.getLevel();
        int diff = levelPlayer - levelTarget;
        if (diff < 0)
            diff = -diff;
        if (diff > 5)
            basicSuccess -= (diff - 5) * 5;
        if (basicSuccess < 1)
            basicSuccess = 1;
        return (Rnd.get(99) < basicSuccess);
    }

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        WorldObject object = targets[0];
        if (!(object instanceof Monster target))
            return;
        if (player.getObjectId() != target.getSeederId()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
            return;
        }
        boolean send = false;
        int total = 0;
        int cropId = 0;
        if (target.isSeeded()) {
            if (calcSuccess(player, target)) {
                List<IntIntHolder> items = target.getHarvestItems();
                if (!items.isEmpty()) {
                    InventoryUpdate iu = new InventoryUpdate();
                    for (IntIntHolder ritem : items) {
                        cropId = ritem.getId();
                        if (player.isInParty()) {
                            player.getParty().distributeItem(player, ritem, true, target);
                            continue;
                        }
                        ItemInstance item = player.getInventory().addItem("Manor", ritem.getId(), ritem.getValue(), player, target);
                        iu.addItem(item);
                        send = true;
                        total += ritem.getValue();
                    }
                    if (send) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(cropId).addNumber(total));
                        if (player.isInParty())
                            player.getParty().broadcastToPartyMembers(player, SystemMessage.getSystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S).addCharName(player).addItemName(cropId).addNumber(total));
                        player.sendPacket(iu);
                    }
                }
            } else {
                player.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
            }
        } else {
            player.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
