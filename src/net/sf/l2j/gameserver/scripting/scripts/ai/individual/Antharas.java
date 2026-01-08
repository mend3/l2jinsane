/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.time.SchedulingPattern;
import net.sf.l2j.commons.time.SchedulingPattern.InvalidPatternException;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Antharas extends L2AttackableAIScript {
    public static final int ANTHARAS = 29019;
    public static final byte DORMANT = 0;
    public static final byte WAITING = 1;
    public static final byte FIGHTING = 2;
    public static final byte DEAD = 3;
    private static final BossZone ANTHARAS_LAIR = ZoneManager.getInstance().getZoneById(110001, BossZone.class);
    private static final int[] ANTHARAS_IDS = new int[]{29066, 29067, 29068};
    private final List<Npc> _monsters = new CopyOnWriteArrayList<>();
    private long _timeTracker = 0L;
    private Player _actualVictim;
    private int _antharasId;
    private L2Skill _skillRegen;
    private int _minionTimer;

    public Antharas() {
        super("ai/individual");
        StatSet info = GrandBossManager.getInstance().getStatsSet(29019);
        switch (GrandBossManager.getInstance().getBossStatus(29019)) {
            case 1:
                this.startQuestTimer("beginning", Config.WAIT_TIME_ANTHARAS, null, null, false);
                break;
            case 2:
                int loc_x = info.getInteger("loc_x");
                int loc_y = info.getInteger("loc_y");
                int loc_z = info.getInteger("loc_z");
                int heading = info.getInteger("heading");
                int hp = info.getInteger("currentHP");
                int mp = info.getInteger("currentMP");
                this.updateAntharas();
                Npc antharas = this.addSpawn(this._antharasId, loc_x, loc_y, loc_z, heading, false, 0L, false);
                GrandBossManager.getInstance().addBoss(29019, (GrandBoss) antharas);
                antharas.setCurrentHpMp(hp, mp);
                antharas.setRunning();
                this._timeTracker = System.currentTimeMillis();
                this.startQuestTimer("regen_task", 60000L, antharas, null, true);
                this.startQuestTimer("skill_task", 2000L, antharas, null, true);
                this.startQuestTimer("minions_spawn", this._minionTimer, antharas, null, true);
                break;
            case 3:
                long temp = info.getLong("respawn_time") - System.currentTimeMillis();
                if (temp > 0L) {
                    this.startQuestTimer("antharas_unlock", temp, null, null, false);
                } else {
                    GrandBossManager.getInstance().setBossStatus(29019, 0);
                }
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFANTHARAS);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFANTHARAS + "\" in " + Antharas.class.getSimpleName(), var5);
        }
    }

    private static L2Skill getRandomSkill(Npc npc) {
        double hpRatio = npc.getCurrentHp() / (double) npc.getMaxHp();
        int[] playersAround = getPlayersCountInPositions(1100, npc, false);
        if (hpRatio < 0.25D) {
            if (Rnd.get(100) < 30) {
                return FrequentSkill.ANTHARAS_MOUTH.getSkill();
            }

            if (playersAround[1] >= 10 && Rnd.get(100) < 80) {
                return FrequentSkill.ANTHARAS_TAIL.getSkill();
            }

            if (playersAround[0] >= 10) {
                if (Rnd.get(100) < 40) {
                    return FrequentSkill.ANTHARAS_DEBUFF.getSkill();
                }

                if (Rnd.get(100) < 10) {
                    return FrequentSkill.ANTHARAS_JUMP.getSkill();
                }
            }

            if (Rnd.get(100) < 10) {
                return FrequentSkill.ANTHARAS_METEOR.getSkill();
            }
        } else if (hpRatio < 0.5D) {
            if (playersAround[1] >= 10 && Rnd.get(100) < 80) {
                return FrequentSkill.ANTHARAS_TAIL.getSkill();
            }

            if (playersAround[0] >= 10) {
                if (Rnd.get(100) < 40) {
                    return FrequentSkill.ANTHARAS_DEBUFF.getSkill();
                }

                if (Rnd.get(100) < 10) {
                    return FrequentSkill.ANTHARAS_JUMP.getSkill();
                }
            }

            if (Rnd.get(100) < 7) {
                return FrequentSkill.ANTHARAS_METEOR.getSkill();
            }
        } else if (hpRatio < 0.75D) {
            if (playersAround[1] >= 10 && Rnd.get(100) < 80) {
                return FrequentSkill.ANTHARAS_TAIL.getSkill();
            }

            if (playersAround[0] >= 10 && Rnd.get(100) < 10) {
                return FrequentSkill.ANTHARAS_JUMP.getSkill();
            }

            if (Rnd.get(100) < 5) {
                return FrequentSkill.ANTHARAS_METEOR.getSkill();
            }
        } else {
            if (playersAround[1] >= 10 && Rnd.get(100) < 80) {
                return FrequentSkill.ANTHARAS_TAIL.getSkill();
            }

            if (Rnd.get(100) < 3) {
                return FrequentSkill.ANTHARAS_METEOR.getSkill();
            }
        }

        if (Rnd.get(100) < 6) {
            return FrequentSkill.ANTHARAS_BREATH.getSkill();
        } else if (Rnd.get(100) < 50) {
            return FrequentSkill.ANTHARAS_NORMAL_ATTACK.getSkill();
        } else if (Rnd.get(100) < 5) {
            return Rnd.get(100) < 50 ? FrequentSkill.ANTHARAS_FEAR.getSkill() : FrequentSkill.ANTHARAS_SHORT_FEAR.getSkill();
        } else {
            return FrequentSkill.ANTHARAS_NORMAL_ATTACK_EX.getSkill();
        }
    }

    protected void registerNpcs() {
        this.addEventIds(ANTHARAS_IDS, ScriptEventType.ON_ATTACK, ScriptEventType.ON_SPAWN);
        this.addKillId(29066, 29067, 29068, 29069, 29070, 29071, 29072, 29073, 29074, 29075, 29076);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("regen_task")) {
            if (this._timeTracker + 1800000L < System.currentTimeMillis()) {
                GrandBossManager.getInstance().setBossStatus(29019, 0);
                ANTHARAS_LAIR.oustAllPlayers();
                this.dropTimers(npc);
                npc.deleteMe();
                return null;
            }

            this._skillRegen.getEffects(npc, npc);
        } else if (event.equalsIgnoreCase("spawn_1")) {
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_2")) {
            npc.broadcastPacket(new SocialAction(npc, 1));
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_3")) {
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_4")) {
            npc.broadcastPacket(new SocialAction(npc, 2));
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_5")) {
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("spawn_6")) {
            this._timeTracker = System.currentTimeMillis();
            GrandBossManager.getInstance().setBossStatus(29019, 2);
            npc.setIsInvul(false);
            npc.setRunning();
            this.startQuestTimer("regen_task", 60000L, npc, null, true);
            this.startQuestTimer("skill_task", 2000L, npc, null, true);
            this.startQuestTimer("minions_spawn", this._minionTimer, npc, null, true);
        } else if (event.equalsIgnoreCase("skill_task")) {
            this.callSkillAI(npc);
        } else if (event.equalsIgnoreCase("minions_spawn")) {
            boolean isBehemoth = Rnd.get(100) < 60;
            int mobNumber = isBehemoth ? 2 : 3;

            for (int i = 0; i < mobNumber && this._monsters.size() <= 9; ++i) {
                int npcId = isBehemoth ? 29069 : Rnd.get(29070, 29076);
                Npc dragon = this.addSpawn(npcId, npc.getX() + Rnd.get(-200, 200), npc.getY() + Rnd.get(-200, 200), npc.getZ(), 0, false, 0L, true);
                ((Monster) dragon).setMinion(true);
                this._monsters.add(dragon);
                Player victim = getRandomPlayer(dragon);
                if (victim != null) {
                    attack((Attackable) dragon, victim);
                }

                if (!isBehemoth) {
                    this.startQuestTimer("self_destruct", this._minionTimer / 3, dragon, null, false);
                }
            }
        } else if (event.equalsIgnoreCase("self_destruct")) {
            L2Skill skill;
            switch (npc.getNpcId()) {
                case 29070:
                case 29071:
                case 29072:
                case 29073:
                case 29074:
                case 29075:
                    skill = SkillTable.getInstance().getInfo(5097, 1);
                    break;
                default:
                    skill = SkillTable.getInstance().getInfo(5094, 1);
            }

            npc.doCast(skill);
        } else if (event.equalsIgnoreCase("beginning")) {
            this.updateAntharas();
            Npc antharas = this.addSpawn(this._antharasId, 181323, 114850, -7623, 32542, false, 0L, false);
            GrandBossManager.getInstance().addBoss(29019, (GrandBoss) antharas);
            antharas.setIsInvul(true);
            this.startQuestTimer("spawn_1", 16L, antharas, null, false);
            this.startQuestTimer("spawn_2", 3016L, antharas, null, false);
            this.startQuestTimer("spawn_3", 13016L, antharas, null, false);
            this.startQuestTimer("spawn_4", 13216L, antharas, null, false);
            this.startQuestTimer("spawn_5", 24016L, antharas, null, false);
            this.startQuestTimer("spawn_6", 25916L, antharas, null, false);
        } else if (event.equalsIgnoreCase("die_1")) {
            this.addSpawn(31859, 177615, 114941, -7709, 0, false, 900000L, false);
            this.startQuestTimer("remove_players", 900000L, null, null, false);
        } else if (event.equalsIgnoreCase("antharas_unlock")) {
            GrandBossManager.getInstance().setBossStatus(29019, 0);
        } else if (event.equalsIgnoreCase("remove_players")) {
            ANTHARAS_LAIR.oustAllPlayers();
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
        if (!Config.FWA_FIXTIMEPATTERNOFANTHARAS.isEmpty()) {
            if (npc.getNpcId() == this._antharasId) {
                this.dropTimers(npc);
                ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 20, -10, 10000, 13000, 0, 0, 0, 0));
                ANTHARAS_LAIR.broadcastPacket(new PlaySound(1, "BS01_D", npc));
                this.startQuestTimer("die_1", 8000L, null, null, false);
                GrandBossManager.getInstance().setBossStatus(29019, 3);
                this.startQuestTimer("antharas_unlock", getRespawnInterval(), null, null, false);
                StatSet info = GrandBossManager.getInstance().getStatsSet(29019);
                info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
                GrandBossManager.getInstance().setStatsSet(29019, info);
            } else {
                this.cancelQuestTimer("self_destruct", npc, null);
                this._monsters.remove(npc);
            }
        } else if (npc.getNpcId() == this._antharasId) {
            this.dropTimers(npc);
            ANTHARAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 20, -10, 10000, 13000, 0, 0, 0, 0));
            ANTHARAS_LAIR.broadcastPacket(new PlaySound(1, "BS01_D", npc));
            this.startQuestTimer("die_1", 8000L, null, null, false);
            GrandBossManager.getInstance().setBossStatus(29019, 3);
            long respawnTime = (long) Config.SPAWN_INTERVAL_ANTHARAS + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_ANTHARAS, Config.RANDOM_SPAWN_TIME_ANTHARAS);
            respawnTime *= 3600000L;
            this.startQuestTimer("antharas_unlock", respawnTime, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29019);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29019, info);
        } else {
            this.cancelQuestTimer("self_destruct", npc, null);
            this._monsters.remove(npc);
        }

        return super.onKill(npc, killer);
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
                L2Skill skill = getRandomSkill(npc);
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

    private void updateAntharas() {
        int playersNumber = ANTHARAS_LAIR.getAllowedPlayers().size();
        if (playersNumber < 45) {
            this._antharasId = ANTHARAS_IDS[0];
            this._skillRegen = SkillTable.getInstance().getInfo(4239, 1);
            this._minionTimer = 180000;
        } else if (playersNumber < 63) {
            this._antharasId = ANTHARAS_IDS[1];
            this._skillRegen = SkillTable.getInstance().getInfo(4240, 1);
            this._minionTimer = 150000;
        } else {
            this._antharasId = ANTHARAS_IDS[2];
            this._skillRegen = SkillTable.getInstance().getInfo(4241, 1);
            this._minionTimer = 120000;
        }

    }

    private void dropTimers(Npc npc) {
        this.cancelQuestTimer("regen_task", npc, null);
        this.cancelQuestTimer("skill_task", npc, null);
        this.cancelQuestTimer("minions_spawn", npc, null);

        for (Npc mob : this._monsters) {
            this.cancelQuestTimer("self_destruct", mob, null);
            mob.deleteMe();
        }

        this._monsters.clear();
    }
}