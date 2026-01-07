/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.time.SchedulingPattern;
import net.sf.l2j.commons.time.SchedulingPattern.InvalidPatternException;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

public class Core extends L2AttackableAIScript {
    private static final int CORE = 29006;
    private static final int DEATH_KNIGHT = 29007;
    private static final int DOOM_WRAITH = 29008;
    private static final int SUSCEPTOR = 29011;
    private static final byte ALIVE = 0;
    private static final byte DEAD = 1;
    private final List<Monster> _minions = new ArrayList();

    public Core() {
        super("ai/individual");
        StatSet info = GrandBossManager.getInstance().getStatsSet(29006);
        int status = GrandBossManager.getInstance().getBossStatus(29006);
        if (status == 1) {
            long temp = info.getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("core_unlock", temp, null, null, false);
            } else {
                GrandBoss core = (GrandBoss) this.addSpawn(29006, 17726, 108915, -6480, 0, false, 0L, false);
                GrandBossManager.getInstance().setBossStatus(29006, 0);
                this.spawnBoss(core);
            }
        } else {
            int loc_x = info.getInteger("loc_x");
            int loc_y = info.getInteger("loc_y");
            int loc_z = info.getInteger("loc_z");
            int heading = info.getInteger("heading");
            int hp = info.getInteger("currentHP");
            int mp = info.getInteger("currentMP");
            GrandBoss core = (GrandBoss) this.addSpawn(29006, loc_x, loc_y, loc_z, heading, false, 0L, false);
            core.setCurrentHpMp(hp, mp);
            this.spawnBoss(core);
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFCORE);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFCORE + "\" in " + Core.class.getSimpleName(), var5);
        }
    }

    protected void registerNpcs() {
        this.addAttackId(new int[]{29006});
        this.addKillId(new int[]{29006, 29007, 29008, 29011});
    }

    public void spawnBoss(GrandBoss npc) {
        GrandBossManager.getInstance().addBoss(npc);
        npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));

        Monster mob;
        int i;
        int x;
        for (i = 0; i < 5; ++i) {
            x = 16800 + i * 360;
            mob = (Monster) this.addSpawn(29007, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0L, false);
            mob.setMinion(true);
            this._minions.add(mob);
            mob = (Monster) this.addSpawn(29007, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0L, false);
            mob.setMinion(true);
            this._minions.add(mob);
            int x2 = 16800 + i * 600;
            mob = (Monster) this.addSpawn(29008, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0L, false);
            mob.setMinion(true);
            this._minions.add(mob);
        }

        for (i = 0; i < 4; ++i) {
            x = 16800 + i * 450;
            mob = (Monster) this.addSpawn(29011, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0L, false);
            mob.setMinion(true);
            this._minions.add(mob);
        }

    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("core_unlock")) {
            GrandBoss core = (GrandBoss) this.addSpawn(29006, 17726, 108915, -6480, 0, false, 0L, false);
            GrandBossManager.getInstance().setBossStatus(29006, 0);
            this.spawnBoss(core);
        } else if (event.equalsIgnoreCase("spawn_minion")) {
            Monster mob = (Monster) this.addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L, false);
            mob.setMinion(true);
            this._minions.add(mob);
        } else if (event.equalsIgnoreCase("despawn_minions")) {
            for (int i = 0; i < this._minions.size(); ++i) {
                Attackable mob = this._minions.get(i);
                if (mob != null) {
                    mob.decayMe();
                }
            }

            this._minions.clear();
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (attacker instanceof Playable) {
            if (npc.isScriptValue(1)) {
                if (Rnd.get(100) == 0) {
                    npc.broadcastNpcSay("Removing intruders.");
                }
            } else {
                npc.setScriptValue(1);
                npc.broadcastNpcSay("A non-permitted target has been discovered.");
                npc.broadcastNpcSay("Starting intruder removal system.");
            }
        }

        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onKill(Npc npc, Creature killer) {
        if (!Config.FWA_FIXTIMEPATTERNOFCORE.isEmpty()) {
            if (npc.getNpcId() == 29006) {
                npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
                npc.broadcastNpcSay("A fatal error has occurred.");
                npc.broadcastNpcSay("System is being shut down...");
                npc.broadcastNpcSay("......");
                this.addSpawn(31842, 16502, 110165, -6394, 0, false, 900000L, false);
                this.addSpawn(31842, 18948, 110166, -6397, 0, false, 900000L, false);
                GrandBossManager.getInstance().setBossStatus(29006, 1);
                long respawnTime = (long) Config.SPAWN_INTERVAL_CORE + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_CORE, Config.RANDOM_SPAWN_TIME_CORE);
                respawnTime *= 3600000L;
                this.startQuestTimer("core_unlock", respawnTime, null, null, false);
                StatSet info = GrandBossManager.getInstance().getStatsSet(29006);
                info.set("respawn_time", System.currentTimeMillis() + respawnTime);
                GrandBossManager.getInstance().setStatsSet(29006, info);
                this.startQuestTimer("despawn_minions", 20000L, null, null, false);
                this.cancelQuestTimers("spawn_minion");
            } else if (GrandBossManager.getInstance().getBossStatus(29006) == 0 && this._minions != null && this._minions.contains(npc)) {
                this._minions.remove(npc);
                this.startQuestTimer("spawn_minion", 60000L, npc, null, false);
            }
        } else if (npc.getNpcId() == 29006) {
            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            npc.broadcastNpcSay("A fatal error has occurred.");
            npc.broadcastNpcSay("System is being shut down...");
            npc.broadcastNpcSay("......");
            this.addSpawn(31842, 16502, 110165, -6394, 0, false, 900000L, false);
            this.addSpawn(31842, 18948, 110166, -6397, 0, false, 900000L, false);
            GrandBossManager.getInstance().setBossStatus(29006, 1);
            this.startQuestTimer("core_unlock", getRespawnInterval(), null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29006);
            info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
            GrandBossManager.getInstance().setStatsSet(29006, info);
            this.startQuestTimer("despawn_minions", 20000L, null, null, false);
            this.cancelQuestTimers("spawn_minion");
        } else if (GrandBossManager.getInstance().getBossStatus(29006) == 0 && this._minions != null && this._minions.contains(npc)) {
            this._minions.remove(npc);
            this.startQuestTimer("spawn_minion", 60000L, npc, null, false);
        }

        return super.onKill(npc, killer);
    }
}