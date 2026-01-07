package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class HotSpringDisease extends L2AttackableAIScript {
    private static final int MALARIA = 4554;

    private static final int DISEASE_CHANCE = 1;

    private static final int[][] MONSTERS_DISEASES = new int[][]{{21314, 21316, 21317, 21319, 21321, 21322}, {4551, 4552, 4553, 4552, 4551, 4553}};

    public HotSpringDisease() {
        super("ai/group");
    }

    private static void tryToApplyEffect(Npc npc, Player victim, int skillId) {
        if (Rnd.get(100) < 1) {
            int level = 1;
            L2Effect[] effects = victim.getAllEffects();
            for (L2Effect e : effects) {
                if (e.getSkill().getId() == skillId) {
                    level += e.getSkill().getLevel();
                    e.exit();
                }
            }
            if (level > 10)
                level = 10;
            SkillTable.getInstance().getInfo(skillId, level).getEffects(npc, victim);
        }
    }

    protected void registerNpcs() {
        addAttackActId(MONSTERS_DISEASES[0]);
    }

    public String onAttackAct(Npc npc, Player victim) {
        for (int i = 0; i < 6; i++) {
            if (MONSTERS_DISEASES[0][i] == npc.getNpcId()) {
                tryToApplyEffect(npc, victim, 4554);
                tryToApplyEffect(npc, victim, MONSTERS_DISEASES[1][i]);
            }
        }
        return super.onAttackAct(npc, victim);
    }
}
