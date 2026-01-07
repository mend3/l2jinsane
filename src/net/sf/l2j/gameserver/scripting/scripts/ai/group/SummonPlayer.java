package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class SummonPlayer extends L2AttackableAIScript {
    private static final int PORTA = 20213;

    private static final int PERUM = 20221;

    public SummonPlayer() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAttackId(20213, 20221);
        addSpellFinishedId(20213, 20221);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (!(attacker instanceof Player))
            return null;
        if (npc.getScriptValue() == 0) {
            double distance = MathUtil.calculateDistance(npc, attacker, true);
            if (distance > 300.0D) {
                if (Rnd.nextBoolean()) {
                    npc.doCast(NpcTemplate.SkillType.TELEPORT);
                    npc.setScriptValue(1);
                }
            } else if (distance > 100.0D) {
                Creature mostHated = ((Attackable) npc).getMostHated();
                int chance = Rnd.get(100);
                if ((mostHated == attacker && chance < 50) || chance < 10) {
                    npc.doCast(NpcTemplate.SkillType.TELEPORT);
                    npc.setScriptValue(1);
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        if (skill.getId() == 4161) {
            npc.setScriptValue(0);
            Creature mostHated = ((Attackable) npc).getMostHated();
            if (mostHated == player && Rnd.get(100) < 33)
                npc.doCast(NpcTemplate.SkillType.SHORT_RANGE);
        }
        return super.onSpellFinished(npc, player, skill);
    }
}
