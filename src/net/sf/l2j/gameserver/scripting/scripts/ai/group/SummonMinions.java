package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.HashMap;
import java.util.Map;

public class SummonMinions extends L2AttackableAIScript {
    private static final String[] ORCS_WORDS = new String[]{"Come out, you children of darkness!", "Destroy the enemy, my brothers!", "Show yourselves!", "Forces of darkness! Follow me!"};

    private static final Map<Integer, int[]> MINIONS = new HashMap<>();

    static {
        MINIONS.put(Integer.valueOf(20767), new int[]{20768, 20769, 20770});
        MINIONS.put(Integer.valueOf(21524), new int[]{21525});
        MINIONS.put(Integer.valueOf(21531), new int[]{21658});
        MINIONS.put(Integer.valueOf(21539), new int[]{21540});
    }

    public SummonMinions() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(MINIONS.keySet(), ScriptEventType.ON_ATTACK);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isScriptValue(0)) {
            int npcId = npc.getNpcId();
            if (npcId != 20767) {
                for (int val : MINIONS.get(Integer.valueOf(npcId))) {
                    Attackable newNpc = (Attackable) addSpawn(val, npc, true, 0L, false);
                    attack(newNpc, attacker);
                }
            } else {
                for (int val : MINIONS.get(Integer.valueOf(npcId)))
                    addSpawn(val, npc, true, 0L, false);
                npc.broadcastNpcSay((String) Rnd.get((Object[]) ORCS_WORDS));
            }
            npc.setScriptValue(1);
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
