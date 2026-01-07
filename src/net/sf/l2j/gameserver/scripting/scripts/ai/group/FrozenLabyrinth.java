package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public final class FrozenLabyrinth extends L2AttackableAIScript {
    private static final int PRONGHORN_SPIRIT = 22087;

    private static final int PRONGHORN = 22088;

    private static final int LOST_BUFFALO = 22093;

    private static final int FROST_BUFFALO = 22094;

    public FrozenLabyrinth() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAttackId(22088, 22094);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (skill != null && !skill.isMagic()) {
            int spawnId = 22093;
            if (npc.getNpcId() == 22088)
                spawnId = 22087;
            Attackable monster = (Attackable) addSpawn(spawnId, npc, false, 120000L, false);
            attack(monster, attacker);
            monster = (Attackable) addSpawn(spawnId, npc.getX() + 20, npc.getY(), npc.getZ(), npc.getHeading(), false, 120000L, false);
            attack(monster, attacker);
            monster = (Attackable) addSpawn(spawnId, npc.getX() + 40, npc.getY(), npc.getZ(), npc.getHeading(), false, 120000L, false);
            attack(monster, attacker);
            monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 120000L, false);
            attack(monster, attacker);
            monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 40, npc.getZ(), npc.getHeading(), false, 120000L, false);
            attack(monster, attacker);
            monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 60, npc.getZ(), npc.getHeading(), false, 120000L, false);
            attack(monster, attacker);
            npc.deleteMe();
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
