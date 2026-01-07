package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.HashMap;
import java.util.Map;

public class PolymorphingOnAttack extends L2AttackableAIScript {
    private static final Map<Integer, Integer[]> MOBSPAWNS = new HashMap<>();
    private static final String[][] MOBTEXTS = new String[][]{{"Enough fooling around. Get ready to die!", "You idiot! I've just been toying with you!", "Now the fun starts!"}, {"I must admit, no one makes my blood boil quite like you do!", "Now the battle begins!", "Witness my true power!"}, {"Prepare to die!", "I'll double my strength!", "You have more skill than I thought"}};

    static {
        MOBSPAWNS.put(21258, new Integer[]{21259,
                100,
                100,
                -1});
        MOBSPAWNS.put(21261, new Integer[]{21262,
                100,
                20,
                0});
        MOBSPAWNS.put(21262, new Integer[]{21263,
                100,
                10,
                1});
        MOBSPAWNS.put(21263, new Integer[]{21264,
                100,
                5,
                2});
        MOBSPAWNS.put(21265, new Integer[]{21271,
                100,
                33,
                0});
        MOBSPAWNS.put(21266, new Integer[]{21269,
                100,
                100,
                -1});
        MOBSPAWNS.put(21267, new Integer[]{21270,
                100,
                100,
                -1});
        MOBSPAWNS.put(21271, new Integer[]{21272,
                66,
                10,
                1});
        MOBSPAWNS.put(21272, new Integer[]{21273,
                33,
                5,
                2});
        MOBSPAWNS.put(21521, new Integer[]{21522,
                100,
                30,
                -1});
        MOBSPAWNS.put(21527, new Integer[]{21528,
                100,
                30,
                -1});
        MOBSPAWNS.put(21533, new Integer[]{21534,
                100,
                30,
                -1});
        MOBSPAWNS.put(21537, new Integer[]{21538,
                100,
                30,
                -1});
    }

    public PolymorphingOnAttack() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(MOBSPAWNS.keySet(), ScriptEventType.ON_ATTACK);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isVisible() && !npc.isDead()) {
            Integer[] tmp = MOBSPAWNS.get(npc.getNpcId());
            if (tmp != null)
                if (npc.getCurrentHp() <= (npc.getMaxHp() * tmp[1]) / 100.0D && Rnd.get(100) < tmp[2]) {
                    if (tmp[3] >= 0) {
                        String text = (String) Rnd.get((Object[]) MOBTEXTS[tmp[3]]);
                        npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), text));
                    }
                    npc.deleteMe();
                    Attackable newNpc = (Attackable) addSpawn(tmp[0], npc, false, 0L, true);
                    attack(newNpc, attacker);
                }
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
