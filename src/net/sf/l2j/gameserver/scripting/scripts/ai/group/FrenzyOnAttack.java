package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class FrenzyOnAttack extends L2AttackableAIScript {
    private static final L2Skill ULTIMATE_BUFF = SkillTable.getInstance().getInfo(4318, 1);

    private static final String[] ORCS_WORDS = new String[]{"Dear ultimate power!!!", "The battle has just begun!", "I never thought I'd use this against a novice!", "You won't take me down easily."};

    public FrenzyOnAttack() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAttackId(20270, 20495, 20588, 20778, 21116);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.getCurrentHp() / npc.getMaxHp() < 0.25D && npc.getFirstEffect(ULTIMATE_BUFF) == null && Rnd.get(10) == 0) {
            npc.broadcastNpcSay((String) Rnd.get((Object[]) ORCS_WORDS));
            npc.setTarget(npc);
            npc.doCast(ULTIMATE_BUFF);
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
