/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.time.SchedulingPattern;
import net.sf.l2j.commons.time.SchedulingPattern.InvalidPatternException;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class Orfen extends L2AttackableAIScript {
    private static final SpawnLocation[] ORFEN_LOCATION = new SpawnLocation[]{new SpawnLocation(43728, 17220, -4342, 0), new SpawnLocation(55024, 17368, -5412, 0), new SpawnLocation(53504, 21248, -5486, 0), new SpawnLocation(53248, 24576, -5262, 0)};
    private static final String[] ORFEN_CHAT = new String[]{"$s1. Stop kidding yourself about your own powerlessness!", "$s1. I'll make you feel what true fear is!", "You're really stupid to have challenged me. $s1! Get ready!", "$s1. Do you think that's going to work?!"};
    private static final int ORFEN = 29014;
    private static final int RAIKEL_LEOS = 29016;
    private static final int RIBA_IREN = 29018;
    private static final byte ALIVE = 0;
    private static final byte DEAD = 1;
    private static long _lastAttackTime = 0L;
    private static boolean _isTeleported;
    private static int _currentIndex;

    public Orfen() {
        super("ai/individual");
        _isTeleported = false;
        StatSet info = GrandBossManager.getInstance().getStatsSet(29014);
        int status = GrandBossManager.getInstance().getBossStatus(29014);
        if (status == 1) {
            long temp = info.getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("orfen_unlock", temp, null, null, false);
            } else {
                _currentIndex = Rnd.get(1, 3);
                GrandBoss orfen = (GrandBoss) this.addSpawn(29014, ORFEN_LOCATION[_currentIndex], false, 0L, false);
                GrandBossManager.getInstance().setBossStatus(29014, 0);
                this.spawnBoss(orfen);
            }
        } else {
            int loc_x = info.getInteger("loc_x");
            int loc_y = info.getInteger("loc_y");
            int loc_z = info.getInteger("loc_z");
            int heading = info.getInteger("heading");
            int hp = info.getInteger("currentHP");
            int mp = info.getInteger("currentMP");
            GrandBoss orfen = (GrandBoss) this.addSpawn(29014, loc_x, loc_y, loc_z, heading, false, 0L, false);
            orfen.setCurrentHpMp(hp, mp);
            this.spawnBoss(orfen);
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFORFEN);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFORFEN + "\" in " + Orfen.class.getSimpleName(), var5);
        }
    }

    private static void goTo(Npc npc, SpawnLocation index) {
        ((Attackable) npc).getAggroList().clear();
        npc.getAI().setIntention(IntentionType.IDLE, null, null);
        L2Spawn spawn = npc.getSpawn();
        spawn.setLoc(index);
        if (index.getX() == 43728) {
            npc.teleportTo(index.getX(), index.getY(), index.getZ(), 0);
        } else {
            npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(index.getX(), index.getY(), index.getZ()));
        }

    }

    protected void registerNpcs() {
        this.addAttackId(new int[]{29014, 29018});
        this.addFactionCallId(new int[]{29016, 29018});
        this.addKillId(new int[]{29014});
        this.addSkillSeeId(new int[]{29014});
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("orfen_unlock")) {
            _currentIndex = Rnd.get(1, 3);
            GrandBoss orfen = (GrandBoss) this.addSpawn(29014, ORFEN_LOCATION[_currentIndex], false, 0L, false);
            GrandBossManager.getInstance().setBossStatus(29014, 0);
            this.spawnBoss(orfen);
        } else if (event.equalsIgnoreCase("check_orfen_pos")) {
            if (_lastAttackTime + 1800000L < System.currentTimeMillis()) {
                int index;
                for (index = _currentIndex; index == _currentIndex; index = Rnd.get(1, 3)) {
                }

                _currentIndex = index;
                _isTeleported = false;
                _lastAttackTime = System.currentTimeMillis();
                goTo(npc, ORFEN_LOCATION[_currentIndex]);
            } else if (_isTeleported && !npc.isInsideZone(ZoneId.SWAMP)) {
                goTo(npc, ORFEN_LOCATION[0]);
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        Creature originalCaster = isPet ? caster.getSummon() : caster;
        if (skill.getAggroPoints() > 0 && Rnd.get(5) == 0 && npc.isInsideRadius(originalCaster, 1000, false, false)) {
            npc.broadcastNpcSay(Rnd.get(ORFEN_CHAT).replace("$s1", caster.getName()));
            originalCaster.teleportTo(npc.getX(), npc.getY(), npc.getZ(), 0);
            npc.setTarget(originalCaster);
            npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
        }

        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        if (caller != null && npc != null && !npc.isCastingNow()) {
            int npcId = npc.getNpcId();
            int callerId = caller.getNpcId();
            if (npcId == 29016 && Rnd.get(20) == 0) {
                npc.setTarget(attacker);
                npc.doCast(SkillTable.getInstance().getInfo(4067, 4));
            } else if (npcId == 29018) {
                int chance = 1;
                if (callerId == 29014) {
                    chance = 9;
                }

                if (callerId != 29018 && caller.getCurrentHp() / (double) caller.getMaxHp() < 0.5D && Rnd.get(10) < chance) {
                    npc.getAI().setIntention(IntentionType.IDLE, null, null);
                    npc.setTarget(caller);
                    npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
                }
            }

            return super.onFactionCall(npc, caller, attacker, isPet);
        } else {
            return super.onFactionCall(npc, caller, attacker, isPet);
        }
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        if (player != null) {
            if (npc.getNpcId() == 29014) {
                _lastAttackTime = System.currentTimeMillis();
                if (!_isTeleported && npc.getCurrentHp() - (double) damage < (double) (npc.getMaxHp() / 2)) {
                    _isTeleported = true;
                    goTo(npc, ORFEN_LOCATION[0]);
                } else if (npc.isInsideRadius(player, 1000, false, false) && !npc.isInsideRadius(player, 300, false, false) && Rnd.get(10) == 0) {
                    npc.broadcastNpcSay(ORFEN_CHAT[Rnd.get(3)].replace("$s1", player.getName()));
                    player.teleportTo(npc.getX(), npc.getY(), npc.getZ(), 0);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
                }
            } else if (!npc.isCastingNow() && npc.getCurrentHp() - (double) damage < (double) npc.getMaxHp() / 2.0D) {
                npc.setTarget(player);
                npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
            }
        }

        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onKill(Npc npc, Creature killer) {
        if (!Config.FWA_FIXTIMEPATTERNOFORFEN.isEmpty()) {
            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            GrandBossManager.getInstance().setBossStatus(29014, 1);
            this.startQuestTimer("orfen_unlock", getRespawnInterval(), null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29014);
            info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
            GrandBossManager.getInstance().setStatsSet(29014, info);
            this.cancelQuestTimer("check_orfen_pos", npc, null);
        } else {
            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            GrandBossManager.getInstance().setBossStatus(29014, 1);
            long respawnTime = (long) Config.SPAWN_INTERVAL_ORFEN + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_ORFEN, Config.RANDOM_SPAWN_TIME_ORFEN);
            respawnTime *= 3600000L;
            this.startQuestTimer("orfen_unlock", respawnTime, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29014);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29014, info);
            this.cancelQuestTimer("check_orfen_pos", npc, null);
        }

        return super.onKill(npc, killer);
    }

    private void spawnBoss(GrandBoss npc) {
        GrandBossManager.getInstance().addBoss(npc);
        npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
        this.startQuestTimer("check_orfen_pos", 60000L, npc, null, true);
        _lastAttackTime = System.currentTimeMillis();
    }
}