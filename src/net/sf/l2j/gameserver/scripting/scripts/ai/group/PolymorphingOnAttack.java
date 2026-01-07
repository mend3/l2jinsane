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
        MOBSPAWNS.put(Integer.valueOf(21258), new Integer[]{Integer.valueOf(21259),
                Integer.valueOf(100),
                Integer.valueOf(100),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21261), new Integer[]{Integer.valueOf(21262),
                Integer.valueOf(100),
                Integer.valueOf(20),
                Integer.valueOf(0)});
        MOBSPAWNS.put(Integer.valueOf(21262), new Integer[]{Integer.valueOf(21263),
                Integer.valueOf(100),
                Integer.valueOf(10),
                Integer.valueOf(1)});
        MOBSPAWNS.put(Integer.valueOf(21263), new Integer[]{Integer.valueOf(21264),
                Integer.valueOf(100),
                Integer.valueOf(5),
                Integer.valueOf(2)});
        MOBSPAWNS.put(Integer.valueOf(21265), new Integer[]{Integer.valueOf(21271),
                Integer.valueOf(100),
                Integer.valueOf(33),
                Integer.valueOf(0)});
        MOBSPAWNS.put(Integer.valueOf(21266), new Integer[]{Integer.valueOf(21269),
                Integer.valueOf(100),
                Integer.valueOf(100),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21267), new Integer[]{Integer.valueOf(21270),
                Integer.valueOf(100),
                Integer.valueOf(100),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21271), new Integer[]{Integer.valueOf(21272),
                Integer.valueOf(66),
                Integer.valueOf(10),
                Integer.valueOf(1)});
        MOBSPAWNS.put(Integer.valueOf(21272), new Integer[]{Integer.valueOf(21273),
                Integer.valueOf(33),
                Integer.valueOf(5),
                Integer.valueOf(2)});
        MOBSPAWNS.put(Integer.valueOf(21521), new Integer[]{Integer.valueOf(21522),
                Integer.valueOf(100),
                Integer.valueOf(30),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21527), new Integer[]{Integer.valueOf(21528),
                Integer.valueOf(100),
                Integer.valueOf(30),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21533), new Integer[]{Integer.valueOf(21534),
                Integer.valueOf(100),
                Integer.valueOf(30),
                Integer.valueOf(-1)});
        MOBSPAWNS.put(Integer.valueOf(21537), new Integer[]{Integer.valueOf(21538),
                Integer.valueOf(100),
                Integer.valueOf(30),
                Integer.valueOf(-1)});
    }

    public PolymorphingOnAttack() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(MOBSPAWNS.keySet(), ScriptEventType.ON_ATTACK);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isVisible() && !npc.isDead()) {
            Integer[] tmp = MOBSPAWNS.get(Integer.valueOf(npc.getNpcId()));
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
