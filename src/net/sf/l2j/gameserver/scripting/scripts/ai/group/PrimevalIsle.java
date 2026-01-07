package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class PrimevalIsle extends L2AttackableAIScript {
    private static final int[] SPRIGANTS = new int[]{18345, 18346};

    private static final int[] MOBIDS = new int[]{22199, 22215, 22216, 22217};

    private static final int ANCIENT_EGG = 18344;

    private static final L2Skill ANESTHESIA = SkillTable.getInstance().getInfo(5085, 1);

    private static final L2Skill POISON = SkillTable.getInstance().getInfo(5086, 1);

    public PrimevalIsle() {
        super("ai/group");
        for (L2Spawn npc : SpawnTable.getInstance().getSpawns()) {
            if (ArraysUtil.contains(MOBIDS, npc.getNpcId()) && npc.getNpc() != null && npc.getNpc() instanceof Attackable)
                ((Attackable) npc.getNpc()).seeThroughSilentMove(true);
        }
    }

    protected void registerNpcs() {
        addEventIds(SPRIGANTS, ScriptEventType.ON_AGGRO, ScriptEventType.ON_KILL);
        addAttackId(18344);
        addSpawnId(MOBIDS);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (!(npc instanceof Attackable))
            return null;
        if (event.equalsIgnoreCase("skill")) {
            int playableCounter = 0;
            for (Playable playable : npc.getKnownTypeInRadius(Playable.class, npc.getTemplate().getAggroRange())) {
                if (!playable.isDead())
                    playableCounter++;
            }
            if (playableCounter == 0) {
                cancelQuestTimer("skill", npc, null);
                return null;
            }
            npc.setTarget(npc);
            npc.doCast((npc.getNpcId() == 18345) ? ANESTHESIA : POISON);
        }
        return null;
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        if (player == null)
            return null;
        npc.setTarget(npc);
        npc.doCast((npc.getNpcId() == 18345) ? ANESTHESIA : POISON);
        if (getQuestTimer("skill", npc, null) == null)
            startQuestTimer("skill", 15000L, npc, null, true);
        return super.onAggro(npc, player, isPet);
    }

    public String onKill(Npc npc, Creature killer) {
        if (getQuestTimer("skill", npc, null) != null)
            cancelQuestTimer("skill", npc, null);
        return super.onKill(npc, killer);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        for (Attackable called : attacker.getKnownTypeInRadius(Attackable.class, 2000)) {
            if (!called.hasAI() || called.isDead())
                continue;
            IntentionType calledIntention = called.getAI().getDesire().getIntention();
            if (calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || (calledIntention == IntentionType.MOVE_TO && !called.isRunning()))
                attack(called, attacker, 1);
        }
        return null;
    }

    public String onSpawn(Npc npc) {
        if (npc instanceof Attackable)
            ((Attackable) npc).seeThroughSilentMove(true);
        return super.onSpawn(npc);
    }
}
