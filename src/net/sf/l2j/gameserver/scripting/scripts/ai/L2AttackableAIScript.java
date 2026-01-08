package net.sf.l2j.gameserver.scripting.scripts.ai;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.ArrayList;
import java.util.List;

public class L2AttackableAIScript extends Quest {
    public L2AttackableAIScript() {
        super(-1, "ai");
        registerNpcs();
    }

    public L2AttackableAIScript(String name) {
        super(-1, name);
        registerNpcs();
    }

    public static Player getRandomPlayer(Npc npc) {
        List<Player> result = new ArrayList<>();
        for (Player player : npc.getKnownType(Player.class)) {
            if (player.isDead())
                continue;
            if (player.isGM() && player.getAppearance().getInvisible())
                continue;
            result.add(player);
        }
        return result.isEmpty() ? null : Rnd.get(result);
    }

    public static int getPlayersCountInRadius(int range, Creature npc, boolean invisible) {
        int count = 0;
        for (Player player : npc.getKnownTypeInRadius(Player.class, range)) {
            if (player.isDead())
                continue;
            if (!invisible && player.getAppearance().getInvisible())
                continue;
            count++;
        }
        return count;
    }

    public static int[] getPlayersCountInPositions(int range, Creature npc, boolean invisible) {
        int frontCount = 0;
        int backCount = 0;
        int sideCount = 0;
        for (Player player : npc.getKnownType(Player.class)) {
            if (player.isDead())
                continue;
            if (!invisible && player.getAppearance().getInvisible())
                continue;
            if (!MathUtil.checkIfInRange(range, npc, player, true))
                continue;
            if (player.isInFrontOf(npc)) {
                frontCount++;
                continue;
            }
            if (player.isBehind(npc)) {
                backCount++;
                continue;
            }
            sideCount++;
        }
        int[] array = {frontCount, backCount, sideCount};
        return array;
    }

    protected static void attack(Attackable npc, Creature victim, int aggro) {
        npc.setIsRunning(true);
        npc.addDamageHate(victim, 0, (aggro <= 0) ? 999 : aggro);
        npc.getAI().setIntention(IntentionType.ATTACK, victim);
    }

    protected static void attack(Attackable npc, Creature victim) {
        attack(npc, victim, 0);
    }

    protected static boolean testCursesOnAttack(Npc npc, Creature attacker, int npcId) {
        if (Config.RAID_DISABLE_CURSE)
            return false;
        if (attacker.getLevel() - npc.getLevel() > 8) {
            L2Skill curse = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
            if (attacker.getFirstEffect(curse) == null) {
                npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
                curse.getEffects(npc, attacker);
                ((Attackable) npc).stopHating(attacker);
                return true;
            }
        }
        if (npc.getNpcId() == npcId && attacker instanceof Player && ((Player) attacker).isMounted()) {
            L2Skill curse = SkillTable.FrequentSkill.RAID_ANTI_STRIDER_SLOW.getSkill();
            if (attacker.getFirstEffect(curse) == null) {
                npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
                curse.getEffects(npc, attacker);
            }
        }
        return false;
    }

    protected static boolean testCursesOnAttack(Npc npc, Creature attacker) {
        return testCursesOnAttack(npc, attacker, npc.getNpcId());
    }

    protected static boolean testCursesOnAggro(Npc npc, Creature attacker) {
        return testCursesOnAttack(npc, attacker, -1);
    }

    protected void registerNpcs() {
        for (NpcTemplate template : NpcData.getInstance().getAllNpcs()) {
            try {
                if (Attackable.class.isAssignableFrom(Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType()))) {
                    template.addQuestEvent(ScriptEventType.ON_ATTACK, this);
                    template.addQuestEvent(ScriptEventType.ON_KILL, this);
                    template.addQuestEvent(ScriptEventType.ON_SPAWN, this);
                    template.addQuestEvent(ScriptEventType.ON_SKILL_SEE, this);
                    template.addQuestEvent(ScriptEventType.ON_FACTION_CALL, this);
                    template.addQuestEvent(ScriptEventType.ON_AGGRO, this);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("An unknown template type {} has been found on {}.", e, template.getType(), toString());
            }
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        return null;
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (caster == null)
            return null;
        if (!(npc instanceof Attackable attackable))
            return null;
        int skillAggroPoints = skill.getAggroPoints();
        if (caster.getSummon() != null)
            if (targets.length == 1 && ArraysUtil.contains((Object[]) targets, caster.getSummon()))
                skillAggroPoints = 0;
        if (skillAggroPoints > 0)
            if (attackable.hasAI() && attackable.getAI().getDesire().getIntention() == IntentionType.ATTACK) {
                WorldObject npcTarget = attackable.getTarget();
                for (WorldObject skillTarget : targets) {
                    if (npcTarget == skillTarget || npc == skillTarget) {
                        Creature originalCaster = isPet ? caster.getSummon() : caster;
                        attackable.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (attackable.getLevel() + 7));
                    }
                }
            }
        return null;
    }

    public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        if (attacker == null)
            return null;
        if (caller instanceof net.sf.l2j.gameserver.model.actor.instance.RiftInvader && attacker.isInParty() && attacker.getParty().isInDimensionalRift() && !attacker.getParty().getDimensionalRift().isInCurrentRoomZone(npc))
            return null;
        Attackable attackable = (Attackable) npc;
        Creature originalAttackTarget = isPet ? attacker.getSummon() : attacker;
        attackable.addDamageHate(originalAttackTarget, 0, 1);
        if (attackable.getAI().getDesire().getIntention() != IntentionType.ATTACK) {
            attackable.setRunning();
            attackable.getAI().setIntention(IntentionType.ATTACK, originalAttackTarget);
        }
        return null;
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        if (player == null)
            return null;
        ((Attackable) npc).addDamageHate(isPet ? (Creature) player.getSummon() : (Creature) player, 0, 1);
        return null;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        npc.getAI().notifyEvent(AiEventType.ATTACKED, attacker);
        ((Attackable) npc).addDamageHate(attacker, damage, damage * 100 / (npc.getLevel() + 7));
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        if (npc instanceof Monster mob) {
            Monster master = mob.getMaster();
            if (master != null)
                master.getMinionList().onMinionDie(mob, master.isRaidBoss() ? Config.RAID_MINION_RESPAWN_TIMER : (master.getSpawn().getRespawnDelay() * 1000 / 2));
            if (mob.hasMinions())
                mob.getMinionList().onMasterDie();
        }
        return null;
    }
}
