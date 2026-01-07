package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class Sow implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.SOW};

    private static boolean calcSuccess(Creature activeChar, Creature target, Seed seed) {
        int minlevelSeed = seed.getLevel() - 5;
        int maxlevelSeed = seed.getLevel() + 5;
        int levelPlayer = activeChar.getLevel();
        int levelTarget = target.getLevel();
        int basicSuccess = seed.isAlternative() ? 20 : 90;
        if (levelTarget < minlevelSeed)
            basicSuccess -= 5 * (minlevelSeed - levelTarget);
        if (levelTarget > maxlevelSeed)
            basicSuccess -= 5 * (levelTarget - maxlevelSeed);
        int diff = levelPlayer - levelTarget;
        if (diff < 0)
            diff = -diff;
        if (diff > 5)
            basicSuccess -= 5 * (diff - 5);
        if (basicSuccess < 1)
            basicSuccess = 1;
        return (Rnd.get(99) < basicSuccess);
    }

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        SystemMessageId smId;
        if (!(activeChar instanceof Player player))
            return;
        WorldObject object = targets[0];
        if (!(object instanceof Monster target))
            return;
        if (target.isDead() || !target.isSeeded() || target.getSeederId() != activeChar.getObjectId())
            return;
        Seed seed = target.getSeed();
        if (seed == null)
            return;
        if (!activeChar.destroyItemByItemId("Consume", seed.getSeedId(), 1, target, false))
            return;
        if (calcSuccess(activeChar, target, seed)) {
            player.sendPacket(new PlaySound("ItemSound.quest_itemget"));
            target.setSeeded(activeChar.getObjectId());
            smId = SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN;
        } else {
            smId = SystemMessageId.THE_SEED_WAS_NOT_SOWN;
        }
        Party party = player.getParty();
        if (party == null) {
            player.sendPacket(smId);
        } else {
            party.broadcastMessage(smId);
        }
        target.getAI().setIntention(IntentionType.IDLE);
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
