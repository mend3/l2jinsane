package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class StakatoNest extends L2AttackableAIScript {
    private static final int SPIKED_STAKATO_GUARD = 22107;

    private static final int FEMALE_SPIKED_STAKATO = 22108;

    private static final int MALE_SPIKED_STAKATO_1 = 22109;

    private static final int MALE_SPIKED_STAKATO_2 = 22110;

    private static final int STAKATO_FOLLOWER = 22112;

    private static final int CANNIBALISTIC_STAKATO_LEADER_1 = 22113;

    private static final int CANNIBALISTIC_STAKATO_LEADER_2 = 22114;

    private static final int SPIKED_STAKATO_CAPTAIN = 22117;

    private static final int SPIKED_STAKATO_NURSE_1 = 22118;

    private static final int SPIKED_STAKATO_NURSE_2 = 22119;

    private static final int SPIKED_STAKATO_BABY = 22120;

    public StakatoNest() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAttackId(22113, 22114);
        addKillId(22109, 22108, 22118, 22120);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.getCurrentHp() / npc.getMaxHp() < 0.3D && Rnd.get(100) < 5)
            for (Monster follower : npc.getKnownTypeInRadius(Monster.class, 400)) {
                if (follower.getNpcId() == 22112 && !follower.isDead()) {
                    npc.setIsCastingNow(true);
                    npc.broadcastPacket(new MagicSkillUse(npc, follower, (npc.getNpcId() == 22114) ? 4072 : 4073, 1, 3000, 0));
                    ThreadPool.schedule(() -> {
                        if (npc.isDead())
                            return;
                        if (follower.isDead()) {
                            npc.setIsCastingNow(false);
                            return;
                        }
                        npc.setCurrentHp(npc.getCurrentHp() + follower.getCurrentHp() / 2.0D);
                        follower.doDie(follower);
                        npc.setIsCastingNow(false);
                    }, 3000L);
                    break;
                }
            }
        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onKill(Npc npc, Creature killer) {
        switch (npc.getNpcId()) {
            case 22109:
                for (Monster angryFemale : npc.getKnownTypeInRadius(Monster.class, 400)) {
                    if (angryFemale.getNpcId() == 22108 && !angryFemale.isDead())
                        for (int i = 0; i < 3; i++) {
                            Npc guard = addSpawn(22107, angryFemale, true, 0L, false);
                            attack((Attackable) guard, killer);
                        }
                }
                break;
            case 22108:
                for (Monster morphingMale : npc.getKnownTypeInRadius(Monster.class, 400)) {
                    if (morphingMale.getNpcId() == 22109 && !morphingMale.isDead()) {
                        Npc newForm = addSpawn(22110, morphingMale, true, 0L, false);
                        attack((Attackable) newForm, killer);
                        morphingMale.deleteMe();
                    }
                }
                break;
            case 22118:
                for (Monster baby : npc.getKnownTypeInRadius(Monster.class, 400)) {
                    if (baby.getNpcId() == 22120 && !baby.isDead())
                        for (int i = 0; i < 3; i++) {
                            Npc captain = addSpawn(22117, baby, true, 0L, false);
                            attack((Attackable) captain, killer);
                        }
                }
                break;
            case 22120:
                for (Monster morphingNurse : npc.getKnownTypeInRadius(Monster.class, 400)) {
                    if (morphingNurse.getNpcId() == 22118 && !morphingNurse.isDead()) {
                        Npc newForm = addSpawn(22119, morphingNurse, true, 0L, false);
                        attack((Attackable) newForm, killer);
                        morphingNurse.deleteMe();
                    }
                }
                break;
        }
        return super.onKill(npc, killer);
    }
}
