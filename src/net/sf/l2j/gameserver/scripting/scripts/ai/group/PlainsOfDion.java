package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public final class PlainsOfDion extends L2AttackableAIScript {
    private static final int[] MONSTERS = new int[]{21104, 21105, 21107};

    private static final String[] MONSTERS_MSG = new String[]{"$s1! How dare you interrupt our fight! Hey guys, help!", "$s1! Hey! We're having a duel here!", "The duel is over! Attack!", "Foul! Kill the coward!", "How dare you interrupt a sacred duel! You must be taught a lesson!"};

    private static final String[] MONSTERS_ASSIST_MSG = new String[]{"Die, you coward!", "Kill the coward!", "What are you looking at?"};

    public PlainsOfDion() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAttackId(MONSTERS);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isScriptValue(0)) {
            npc.broadcastNpcSay(((String) Rnd.get((Object[]) MONSTERS_MSG)).replace("$s1", attacker.getName()));
            for (Monster obj : npc.getKnownTypeInRadius(Monster.class, 300)) {
                if (!obj.isAttackingNow() && !obj.isDead() && ArraysUtil.contains(MONSTERS, obj.getNpcId())) {
                    attack(obj, attacker);
                    obj.broadcastNpcSay((String) Rnd.get((Object[]) MONSTERS_ASSIST_MSG));
                }
            }
            npc.setScriptValue(1);
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
