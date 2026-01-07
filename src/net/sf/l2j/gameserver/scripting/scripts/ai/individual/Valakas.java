/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.time.SchedulingPattern;
import net.sf.l2j.commons.time.SchedulingPattern.InvalidPatternException;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.Iterator;

public class Valakas extends L2AttackableAIScript {
    public static final byte DORMANT = 0;
    public static final byte WAITING = 1;
    public static final byte FIGHTING = 2;
    public static final byte DEAD = 3;
    public static final int VALAKAS = 29028;
    private static final BossZone VALAKAS_LAIR = ZoneManager.getInstance().getZoneById(110010, BossZone.class);
    private static final int[] FRONT_SKILLS = new int[]{4681, 4682, 4683, 4684, 4689};
    private static final int[] BEHIND_SKILLS = new int[]{4685, 4686, 4688};
    private static final int LAVA_SKIN = 4680;
    private static final int METEOR_SWARM = 4690;
    private static final SpawnLocation[] CUBE_LOC = new SpawnLocation[]{new SpawnLocation(214880, -116144, -1644, 0), new SpawnLocation(213696, -116592, -1644, 0), new SpawnLocation(212112, -116688, -1644, 0), new SpawnLocation(211184, -115472, -1664, 0), new SpawnLocation(210336, -114592, -1644, 0), new SpawnLocation(211360, -113904, -1644, 0), new SpawnLocation(213152, -112352, -1644, 0), new SpawnLocation(214032, -113232, -1644, 0), new SpawnLocation(214752, -114592, -1644, 0), new SpawnLocation(209824, -115568, -1421, 0), new SpawnLocation(210528, -112192, -1403, 0), new SpawnLocation(213120, -111136, -1408, 0), new SpawnLocation(215184, -111504, -1392, 0), new SpawnLocation(215456, -117328, -1392, 0), new SpawnLocation(213200, -118160, -1424, 0)};
    private long _timeTracker = 0L;
    private Player _actualVictim;

    public Valakas() {
        super("ai/individual");
        StatSet info = GrandBossManager.getInstance().getStatsSet(29028);
        switch (GrandBossManager.getInstance().getBossStatus(29028)) {
            case 1:
                this.startQuestTimer("beginning", Config.WAIT_TIME_VALAKAS, null, null, false);
                break;
            case 2:
                int loc_x = info.getInteger("loc_x");
                int loc_y = info.getInteger("loc_y");
                int loc_z = info.getInteger("loc_z");
                int heading = info.getInteger("heading");
                int hp = info.getInteger("currentHP");
                int mp = info.getInteger("currentMP");
                Npc valakas = this.addSpawn(29028, loc_x, loc_y, loc_z, heading, false, 0L, false);
                GrandBossManager.getInstance().addBoss((GrandBoss) valakas);
                valakas.setCurrentHpMp(hp, mp);
                valakas.setRunning();
                this._timeTracker = System.currentTimeMillis();
                this.startQuestTimer("regen_task", 60000L, valakas, null, true);
                this.startQuestTimer("skill_task", 2000L, valakas, null, true);
                break;
            case 3:
                long temp = info.getLong("respawn_time") - System.currentTimeMillis();
                if (temp > 0L) {
                    this.startQuestTimer("valakas_unlock", temp, null, null, false);
                } else {
                    GrandBossManager.getInstance().setBossStatus(29028, 0);
                }
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFVALAKAS);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFVALAKAS + "\" in " + Valakas.class.getSimpleName(), var5);
        }
    }

    private static int getRandomSkill(Npc npc) {
        double hpRatio = npc.getCurrentHp() / (double) npc.getMaxHp();
        if (hpRatio < 0.25D && Rnd.get(1500) == 0 && npc.getFirstEffect(4680) == null) {
            return 4680;
        } else if (hpRatio < 0.5D && Rnd.get(60) == 0) {
            return 4690;
        } else {
            int[] playersAround = getPlayersCountInPositions(1200, npc, false);
            return playersAround[1] > playersAround[0] ? Rnd.get(BEHIND_SKILLS) : Rnd.get(FRONT_SKILLS);
        }
    }

    protected void registerNpcs() {
        this.addEventIds(29028, new ScriptEventType[]{ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL, ScriptEventType.ON_SPAWN, ScriptEventType.ON_AGGRO});
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("beginning")) {
            this._timeTracker = System.currentTimeMillis();
            npc = this.addSpawn(29028, 212852, -114842, -1632, 0, false, 0L, false);
            GrandBossManager.getInstance().addBoss((GrandBoss) npc);
            npc.setIsInvul(true);
            Iterator var4 = VALAKAS_LAIR.getKnownTypeInside(Player.class).iterator();

            while (var4.hasNext()) {
                Player plyr = (Player) var4.next();
                plyr.sendPacket(new PlaySound(1, "B03_A", npc));
                plyr.sendPacket(new SocialAction(npc, 3));
            }

            this.startQuestTimer("spawn_1", 2000L, npc, null, false);
            this.startQuestTimer("spawn_2", 3500L, npc, null, false);
            this.startQuestTimer("spawn_3", 6800L, npc, null, false);
            this.startQuestTimer("spawn_4", 9700L, npc, null, false);
            this.startQuestTimer("spawn_5", 12400L, npc, null, false);
            this.startQuestTimer("spawn_6", 12401L, npc, null, false);
            this.startQuestTimer("spawn_7", 15601L, npc, null, false);
            this.startQuestTimer("spawn_8", 17001L, npc, null, false);
            this.startQuestTimer("spawn_9", 23701L, npc, null, false);
            this.startQuestTimer("spawn_10", 29401L, npc, null, false);
        } else if (event.equalsIgnoreCase("regen_task")) {
            if (GrandBossManager.getInstance().getBossStatus(29028) == 2 && this._timeTracker + 900000L < System.currentTimeMillis()) {
                GrandBossManager.getInstance().setBossStatus(29028, 0);
                VALAKAS_LAIR.oustAllPlayers();
                this.cancelQuestTimer("regen_task", npc, null);
                this.cancelQuestTimer("skill_task", npc, null);
                npc.deleteMe();
                return null;
            }

            if (Rnd.get(30) == 0) {
                double hpRatio = npc.getCurrentHp() / (double) npc.getMaxHp();
                L2Skill skillRegen;
                if (hpRatio < 0.25D) {
                    skillRegen = SkillTable.getInstance().getInfo(4691, 4);
                } else if (hpRatio < 0.5D) {
                    skillRegen = SkillTable.getInstance().getInfo(4691, 3);
                } else if (hpRatio < 0.75D) {
                    skillRegen = SkillTable.getInstance().getInfo(4691, 2);
                } else {
                    skillRegen = SkillTable.getInstance().getInfo(4691, 1);
                }

                skillRegen.getEffects(npc, npc);
            }
        } else if (event.equalsIgnoreCase("spawn_1")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1800, 180, -1, 1500, 10000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_2")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 180, -5, 3000, 10000, 0, -5, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_3")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, 180, -8, 600, 10000, 0, 60, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_4")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 800, 180, -8, 2700, 10000, 0, 30, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_5")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 250, 70, 0, 10000, 30, 80, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_6")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 250, 70, 2500, 10000, 30, 80, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_7")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 150, 30, 0, 10000, -10, 60, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_8")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 150, 20, 2900, 10000, -10, 30, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_9")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, -10, 3400, 4000, 10, -15, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_10")) {
            GrandBossManager.getInstance().setBossStatus(29028, 2);
            npc.setIsInvul(false);
            this.startQuestTimer("regen_task", 60000L, npc, null, true);
            this.startQuestTimer("skill_task", 2000L, npc, null, true);
        } else if (event.equalsIgnoreCase("die_1")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 2000, 130, -1, 0, 10000, 0, 0, 1, 1));
        } else if (event.equalsIgnoreCase("die_2")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 210, -5, 3000, 10000, -13, 0, 1, 1));
        } else if (event.equalsIgnoreCase("die_3")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 200, -8, 3000, 10000, 0, 15, 1, 1));
        } else if (event.equalsIgnoreCase("die_4")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1000, 190, 0, 500, 10000, 0, 10, 1, 1));
        } else if (event.equalsIgnoreCase("die_5")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 120, 0, 2500, 10000, 12, 40, 1, 1));
        } else if (event.equalsIgnoreCase("die_6")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 20, 0, 700, 10000, 10, 10, 1, 1));
        } else if (event.equalsIgnoreCase("die_7")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 1000, 10000, 20, 70, 1, 1));
        } else if (event.equalsIgnoreCase("die_8")) {
            VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 300, 250, 20, -20, 1, 1));
            SpawnLocation[] var11 = CUBE_LOC;
            int var10 = var11.length;

            for (int var6 = 0; var6 < var10; ++var6) {
                SpawnLocation loc = var11[var6];
                this.addSpawn(31759, loc, false, 900000L, false);
            }

            this.startQuestTimer("remove_players", 900000L, null, null, false);
        } else if (event.equalsIgnoreCase("skill_task")) {
            this.callSkillAI(npc);
        } else if (event.equalsIgnoreCase("valakas_unlock")) {
            GrandBossManager.getInstance().setBossStatus(29028, 0);
        } else if (event.equalsIgnoreCase("remove_players")) {
            VALAKAS_LAIR.oustAllPlayers();
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onSpawn(Npc npc) {
        npc.disableCoreAI(true);
        return super.onSpawn(npc);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isInvul()) {
            return null;
        } else {
            if (attacker instanceof Playable) {
                if (testCursesOnAttack(npc, attacker)) {
                    return null;
                }

                this._timeTracker = System.currentTimeMillis();
            }

            return super.onAttack(npc, attacker, damage, skill);
        }
    }

    public String onKill(Npc npc, Creature killer) {
        if (!Config.FWA_FIXTIMEPATTERNOFVALAKAS.isEmpty()) {
            this.cancelQuestTimer("regen_task", npc, null);
            this.cancelQuestTimer("skill_task", npc, null);
            VALAKAS_LAIR.broadcastPacket(new PlaySound(1, "B03_D", npc));
            this.startQuestTimer("die_1", 300L, npc, null, false);
            this.startQuestTimer("die_2", 600L, npc, null, false);
            this.startQuestTimer("die_3", 3800L, npc, null, false);
            this.startQuestTimer("die_4", 8200L, npc, null, false);
            this.startQuestTimer("die_5", 8700L, npc, null, false);
            this.startQuestTimer("die_6", 13300L, npc, null, false);
            this.startQuestTimer("die_7", 14000L, npc, null, false);
            this.startQuestTimer("die_8", 16500L, npc, null, false);
            GrandBossManager.getInstance().setBossStatus(29028, 3);
            this.startQuestTimer("valakas_unlock", getRespawnInterval(), null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29028);
            info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
            GrandBossManager.getInstance().setStatsSet(29028, info);
        } else {
            this.cancelQuestTimer("regen_task", npc, null);
            this.cancelQuestTimer("skill_task", npc, null);
            VALAKAS_LAIR.broadcastPacket(new PlaySound(1, "B03_D", npc));
            this.startQuestTimer("die_1", 300L, npc, null, false);
            this.startQuestTimer("die_2", 600L, npc, null, false);
            this.startQuestTimer("die_3", 3800L, npc, null, false);
            this.startQuestTimer("die_4", 8200L, npc, null, false);
            this.startQuestTimer("die_5", 8700L, npc, null, false);
            this.startQuestTimer("die_6", 13300L, npc, null, false);
            this.startQuestTimer("die_7", 14000L, npc, null, false);
            this.startQuestTimer("die_8", 16500L, npc, null, false);
            GrandBossManager.getInstance().setBossStatus(29028, 3);
            long respawnTime = (long) Config.SPAWN_INTERVAL_VALAKAS + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_VALAKAS, Config.RANDOM_SPAWN_TIME_VALAKAS);
            respawnTime *= 3600000L;
            this.startQuestTimer("valakas_unlock", respawnTime, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29028);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29028, info);
        }

        return super.onKill(npc, killer);
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        return null;
    }

    private void callSkillAI(Npc npc) {
        if (!npc.isInvul() && !npc.isCastingNow()) {
            if (this._actualVictim == null || this._actualVictim.isDead() || !npc.getKnownType(Player.class).contains(this._actualVictim) || Rnd.get(10) == 0) {
                this._actualVictim = getRandomPlayer(npc);
            }

            if (this._actualVictim == null) {
                if (Rnd.get(10) == 0) {
                    int x = npc.getX();
                    int y = npc.getY();
                    int z = npc.getZ();
                    int posX = x + Rnd.get(-1400, 1400);
                    int posY = y + Rnd.get(-1400, 1400);
                    if (GeoEngine.getInstance().canMoveToTarget(x, y, z, posX, posY, z)) {
                        npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(posX, posY, z));
                    }
                }

            } else {
                L2Skill skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
                if (MathUtil.checkIfInRange(skill.getCastRange() < 600 ? 600 : skill.getCastRange(), npc, this._actualVictim, true)) {
                    npc.getAI().setIntention(IntentionType.IDLE);
                    npc.setTarget(this._actualVictim);
                    npc.doCast(skill);
                } else {
                    npc.getAI().setIntention(IntentionType.FOLLOW, this._actualVictim, null);
                }

            }
        }
    }
}