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
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Baium extends L2AttackableAIScript {
    public static final byte ASLEEP = 0;
    public static final byte AWAKE = 1;
    public static final byte DEAD = 2;
    private static final BossZone BAIUM_LAIR = ZoneManager.getInstance().getZoneById(110002, BossZone.class);
    private static final int STONE_BAIUM = 29025;
    private static final int LIVE_BAIUM = 29020;
    private static final int ARCHANGEL = 29021;
    private static final SpawnLocation[] ANGEL_LOCATION = new SpawnLocation[]{new SpawnLocation(114239, 17168, 10080, 63544), new SpawnLocation(115780, 15564, 10080, 13620), new SpawnLocation(114880, 16236, 10080, 5400), new SpawnLocation(115168, 17200, 10080, 0), new SpawnLocation(115792, 16608, 10080, 0)};
    private final List<Npc> _minions = new ArrayList<>(5);
    private Creature _actualVictim;
    private long _timeTracker = 0L;

    public Baium() {
        super("ai/individual");
        this.addStartNpc(29025);
        this.addTalkId(29025);
        StatSet info = GrandBossManager.getInstance().getStatsSet(29020);
        int status = GrandBossManager.getInstance().getBossStatus(29020);
        if (status == 2) {
            long temp = info.getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("baium_unlock", temp, null, null, false);
            } else {
                this.addSpawn(29025, 116033, 17447, 10104, 40188, false, 0L, false);
                GrandBossManager.getInstance().setBossStatus(29020, 0);
            }
        } else if (status == 1) {
            int loc_x = info.getInteger("loc_x");
            int loc_y = info.getInteger("loc_y");
            int loc_z = info.getInteger("loc_z");
            int heading = info.getInteger("heading");
            int hp = info.getInteger("currentHP");
            int mp = info.getInteger("currentMP");
            Npc baium = this.addSpawn(29020, loc_x, loc_y, loc_z, heading, false, 0L, false);
            GrandBossManager.getInstance().addBoss((GrandBoss) baium);
            baium.setCurrentHpMp(hp, mp);
            baium.setRunning();
            this._timeTracker = System.currentTimeMillis();
            this.startQuestTimer("baium_despawn", 60000L, baium, null, true);
            this.startQuestTimer("skill_range", 2000L, baium, null, true);
            SpawnLocation[] var10 = ANGEL_LOCATION;
            int var11 = var10.length;

            for (SpawnLocation loc : var10) {
                Npc angel = this.addSpawn(29021, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0L, true);
                ((Monster) angel).setMinion(true);
                angel.setRunning();
                this._minions.add(angel);
            }

            this.startQuestTimer("angels_aggro_reconsider", 5000L, null, null, true);
        } else {
            this.addSpawn(29025, 116033, 17447, 10104, 40188, false, 0L, false);
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFBAIUM);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFBAIUM + "\" in " + Baium.class.getSimpleName(), var5);
        }
    }

    private static Creature getRandomTarget(Npc npc) {
        int npcId = npc.getNpcId();
        List<Creature> result = new ArrayList<>();
        Iterator<Creature> var3 = npc.getKnownType(Creature.class).iterator();

        while (true) {
            Creature obj;
            do {
                do {
                    do {
                        do {
                            do {
                                if (!var3.hasNext()) {
                                    if (result.isEmpty() && npcId == 29020) {
                                        var3 = npc.getKnownType(Creature.class).iterator();

                                        while (true) {
                                            do {
                                                do {
                                                    do {
                                                        do {
                                                            if (!var3.hasNext()) {
                                                                return result.isEmpty() ? null : Rnd.get(result);
                                                            }

                                                            obj = var3.next();
                                                        } while (!(obj instanceof Player));
                                                    } while (obj.isDead());
                                                } while (!GeoEngine.getInstance().canSeeTarget(npc, obj));
                                            } while (obj.isGM() && ((Player) obj).getAppearance().getInvisible());

                                            result.add(obj);
                                        }
                                    }

                                    return result.isEmpty() ? null : Rnd.get(result);
                                }

                                obj = var3.next();
                            } while (!(obj instanceof Player));
                        } while (obj.isDead());
                    } while (!GeoEngine.getInstance().canSeeTarget(npc, obj));
                } while (obj.isGM() && ((Player) obj).getAppearance().getInvisible());
            } while (npcId == 29021 && obj.getActiveWeaponInstance() == null);

            result.add(obj);
        }
    }

    private static int getRandomSkill(Npc npc) {
        if (npc.getCurrentHp() / (double) npc.getMaxHp() < 0.1D && Rnd.get(10000) == 777) {
            return 4135;
        } else {
            int skill = 4127;
            int chance = Rnd.get(100);
            if (getPlayersCountInRadius(600, npc, false) < 20 && npc.getKnownTypeInRadius(Monster.class, 600).size() < 2) {
                if (npc.getCurrentHp() / (double) npc.getMaxHp() > 0.75D) {
                    if (chance < 10) {
                        skill = 4128;
                    } else if (chance >= 10 && chance < 20) {
                        skill = 4129;
                    }
                } else if (npc.getCurrentHp() / (double) npc.getMaxHp() > 0.5D) {
                    if (chance < 10) {
                        skill = 4131;
                    } else if (chance >= 10 && chance < 20) {
                        skill = 4128;
                    } else if (chance >= 20 && chance < 30) {
                        skill = 4129;
                    }
                } else if (npc.getCurrentHp() / (double) npc.getMaxHp() > 0.25D) {
                    if (chance < 10) {
                        skill = 4130;
                    } else if (chance >= 10 && chance < 20) {
                        skill = 4131;
                    } else if (chance >= 20 && chance < 30) {
                        skill = 4128;
                    } else if (chance >= 30 && chance < 40) {
                        skill = 4129;
                    }
                } else if (chance < 10) {
                    skill = 4130;
                } else if (chance >= 10 && chance < 20) {
                    skill = 4131;
                } else if (chance >= 20 && chance < 30) {
                    skill = 4128;
                } else if (chance >= 30 && chance < 40) {
                    skill = 4129;
                }
            } else if (chance < 25) {
                skill = 4130;
            } else if (chance >= 25 && chance < 50) {
                skill = 4131;
            } else if (chance >= 50 && chance < 75) {
                skill = 4128;
            } else if (chance >= 75 && chance < 100) {
                skill = 4129;
            }

            return skill;
        }
    }

    protected void registerNpcs() {
        this.addEventIds(29020, ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL, ScriptEventType.ON_SPAWN);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (npc != null && npc.getNpcId() == 29020) {
            if (event.equalsIgnoreCase("skill_range")) {
                this.callSkillAI(npc);
            } else if (event.equalsIgnoreCase("baium_neck")) {
                npc.broadcastPacket(new SocialAction(npc, 3));
            } else if (event.equalsIgnoreCase("sacrifice_waker")) {
                if (player != null) {
                    if (!MathUtil.checkIfInShortRadius(300, player, npc, true)) {
                        BAIUM_LAIR.allowPlayerEntry(player, 10);
                        player.teleportTo(115929, 17349, 10077, 0);
                    }

                    if (Rnd.get(100) < 60) {
                        player.doDie(npc);
                    }
                }
            } else if (event.equalsIgnoreCase("baium_roar")) {
                npc.broadcastPacket(new SocialAction(npc, 1));
                SpawnLocation[] var11 = ANGEL_LOCATION;
                int var10 = var11.length;

                for (SpawnLocation loc : var11) {
                    Npc angel = this.addSpawn(29021, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0L, true);
                    ((Monster) angel).setMinion(true);
                    angel.setRunning();
                    this._minions.add(angel);
                }

                this.startQuestTimer("angels_aggro_reconsider", 5000L, null, null, true);
            } else if (event.equalsIgnoreCase("baium_move")) {
                npc.setIsInvul(false);
                npc.setRunning();
                this._timeTracker = System.currentTimeMillis();
                this.startQuestTimer("baium_despawn", 60000L, npc, null, true);
                this.startQuestTimer("skill_range", 2000L, npc, null, true);
            } else if (event.equalsIgnoreCase("baium_despawn")) {
                if (this._timeTracker + 1800000L < System.currentTimeMillis()) {
                    npc.deleteMe();

                    for (Npc minion : this._minions) {
                        minion.getSpawn().setRespawnState(false);
                        minion.deleteMe();
                    }

                    this._minions.clear();
                    this.addSpawn(29025, 116033, 17447, 10104, 40188, false, 0L, false);
                    GrandBossManager.getInstance().setBossStatus(29020, 0);
                    BAIUM_LAIR.oustAllPlayers();
                    this.cancelQuestTimer("baium_despawn", npc, null);
                } else if (this._timeTracker + 300000L < System.currentTimeMillis() && npc.getCurrentHp() / (double) npc.getMaxHp() < 0.75D) {
                    npc.setTarget(npc);
                    npc.doCast(SkillTable.getInstance().getInfo(4135, 1));
                } else if (!BAIUM_LAIR.isInsideZone(npc)) {
                    npc.teleportTo(116033, 17447, 10104, 0);
                }
            }
        } else if (event.equalsIgnoreCase("baium_unlock")) {
            GrandBossManager.getInstance().setBossStatus(29020, 0);
            this.addSpawn(29025, 116033, 17447, 10104, 40188, false, 0L, false);
        } else if (event.equalsIgnoreCase("angels_aggro_reconsider")) {
            boolean updateTarget = false;

            for (Npc minion : this._minions) {
                if (minion != null) {
                    Attackable angel = (Attackable) minion;
                    Creature victim = angel.getMostHated();
                    if (Rnd.get(100) < 10) {
                        updateTarget = true;
                    } else if (victim != null) {
                        if (victim instanceof Player && victim.getActiveWeaponInstance() == null) {
                            angel.stopHating(victim);
                            updateTarget = true;
                        }
                    } else {
                        updateTarget = true;
                    }

                    Creature randomTarget;
                    if (updateTarget) {
                        randomTarget = getRandomTarget(minion);
                        if (randomTarget != null && victim != randomTarget) {
                            angel.setRunning();
                            angel.addDamageHate(randomTarget, 0, 999);
                            angel.getAI().setIntention(IntentionType.ATTACK, randomTarget);
                            angel.getAI().setIntention(IntentionType.ATTACK, randomTarget);
                        }
                    }

                    if (angel.getMostHated() == null) {
                        randomTarget = getRandomTarget(minion);
                        if (randomTarget != null) {
                            angel.addDamageHate(randomTarget, 0, 999);
                            angel.getAI().setIntention(IntentionType.ATTACK, randomTarget);
                        }
                    }
                }
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = "";
        if (GrandBossManager.getInstance().getBossStatus(29020) == 0) {
            GrandBossManager.getInstance().setBossStatus(29020, 1);
            Npc baium = this.addSpawn(29020, npc, false, 0L, false);
            baium.setIsInvul(true);
            GrandBossManager.getInstance().addBoss((GrandBoss) baium);
            baium.broadcastPacket(new SocialAction(baium, 2));
            baium.broadcastPacket(new Earthquake(baium.getX(), baium.getY(), baium.getZ(), 40, 10));
            this.startQuestTimer("baium_neck", 13000L, baium, null, false);
            this.startQuestTimer("sacrifice_waker", 24000L, baium, player, false);
            this.startQuestTimer("baium_roar", 28000L, baium, null, false);
            this.startQuestTimer("baium_move", 35000L, baium, null, false);
            npc.deleteMe();
        }

        return htmltext;
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
        if (!Config.FWA_FIXTIMEPATTERNOFBAIUM.isEmpty()) {
            this.cancelQuestTimer("baium_despawn", npc, null);
            npc.broadcastPacket(new PlaySound(1, "BS01_D", npc));
            this.addSpawn(29055, 115203, 16620, 10078, 0, false, 900000L, false);
            GrandBossManager.getInstance().setBossStatus(29020, 2);
            this.startQuestTimer("baium_unlock", getRespawnInterval(), null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29020);
            info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
            GrandBossManager.getInstance().setStatsSet(29020, info);

            for (Npc minion : this._minions) {
                minion.getSpawn().setRespawnState(false);
                minion.deleteMe();
            }

            this._minions.clear();
            this.cancelQuestTimer("skill_range", npc, null);
            this.cancelQuestTimer("angels_aggro_reconsider", null, null);
        } else {
            this.cancelQuestTimer("baium_despawn", npc, null);
            npc.broadcastPacket(new PlaySound(1, "BS01_D", npc));
            this.addSpawn(29055, 115203, 16620, 10078, 0, false, 900000L, false);
            long respawnTime = (long) Config.SPAWN_INTERVAL_BAIUM + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_BAIUM, Config.RANDOM_SPAWN_TIME_BAIUM);
            respawnTime *= 3600000L;
            GrandBossManager.getInstance().setBossStatus(29020, 2);
            this.startQuestTimer("baium_unlock", respawnTime, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29020);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29020, info);

            for (Npc minion : this._minions) {
                minion.getSpawn().setRespawnState(false);
                minion.deleteMe();
            }

            this._minions.clear();
            this.cancelQuestTimer("skill_range", npc, null);
            this.cancelQuestTimer("angels_aggro_reconsider", null, null);
        }

        return super.onKill(npc, killer);
    }

    private void callSkillAI(Npc npc) {
        if (!npc.isInvul() && !npc.isCastingNow()) {
            if (this._actualVictim == null || this._actualVictim.isDead() || !npc.getKnownType(Player.class).contains(this._actualVictim) || this._actualVictim instanceof Monster && Rnd.get(10) < 5 || Rnd.get(10) == 0) {
                this._actualVictim = getRandomTarget(npc);
            }

            if (this._actualVictim != null) {
                L2Skill skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
                if (MathUtil.checkIfInRange((int) ((double) skill.getCastRange() + npc.getCollisionRadius()), npc, this._actualVictim, true)) {
                    npc.getAI().setIntention(IntentionType.IDLE);
                    npc.setTarget(skill.getId() == 4135 ? npc : this._actualVictim);
                    npc.doCast(skill);
                } else {
                    npc.getAI().setIntention(IntentionType.FOLLOW, this._actualVictim, null);
                }

            }
        }
    }
}