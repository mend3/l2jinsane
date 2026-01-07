package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;

public class Sweep implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.SWEEP};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        for (WorldObject target : targets) {
            if (target instanceof Monster monster) {
                if (monster.isSpoiled()) {
                    List<IntIntHolder> items = monster.getSweepItems();
                    if (!items.isEmpty())
                        for (IntIntHolder item : items) {
                            if (player.isInParty()) {
                                player.getParty().distributeItem(player, item, true, monster);
                                continue;
                            }
                            player.addItem("Sweep", item.getId(), item.getValue(), player, true);
                        }
                }
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
