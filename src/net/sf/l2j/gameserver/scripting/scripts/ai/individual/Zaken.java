/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.time.SchedulingPattern;
import net.sf.l2j.commons.time.SchedulingPattern.InvalidPatternException;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Zaken extends L2AttackableAIScript {
    private static final BossZone ZONE = ZoneManager.getInstance().getZoneById(110000, BossZone.class);
    private static final Set<Player> VICTIMS = ConcurrentHashMap.newKeySet();
    private static final Location[] LOCS = new Location[]{new Location(53950, 219860, -3488), new Location(55980, 219820, -3488), new Location(54950, 218790, -3488), new Location(55970, 217770, -3488), new Location(53930, 217760, -3488), new Location(55970, 217770, -3216), new Location(55980, 219920, -3216), new Location(54960, 218790, -3216), new Location(53950, 219860, -3216), new Location(53930, 217760, -3216), new Location(55970, 217770, -2944), new Location(55980, 219920, -2944), new Location(54960, 218790, -2944), new Location(53950, 219860, -2944), new Location(53930, 217760, -2944)};
    private static final int ZAKEN = 29022;
    private static final int DOLL_BLADER = 29023;
    private static final int VALE_MASTER = 29024;
    private static final int PIRATE_CAPTAIN = 29026;
    private static final int PIRATE_ZOMBIE = 29027;
    private static final byte ALIVE = 0;
    private static final byte DEAD = 1;
    private final Location _zakenLocation = new Location(0, 0, 0);
    private int _teleportCheck;
    private int _minionStatus;
    private int _hate;
    private boolean _hasTeleported;
    private Creature _mostHated;

    public Zaken() {
        super("ai/individual");
        StatSet info = GrandBossManager.getInstance().getStatsSet(29022);
        if (GrandBossManager.getInstance().getBossStatus(29022) == 1) {
            long temp = info.getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("zaken_unlock", temp, null, null, false);
            } else {
                this.spawnBoss(true);
            }
        } else {
            this.spawnBoss(false);
        }

    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern = null;
        long now = System.currentTimeMillis();

        try {
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFZAKEN);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFZAKEN + "\" in " + Zaken.class.getSimpleName(), var5);
        }
    }

    private static void callSkills(Npc npc, WorldObject target) {
        if (!npc.isCastingNow()) {
            npc.setTarget(target);
            int chance = Rnd.get(225);
            if (chance < 1) {
                npc.doCast(FrequentSkill.ZAKEN_TELE.getSkill());
            } else if (chance < 2) {
                npc.doCast(FrequentSkill.ZAKEN_MASS_TELE.getSkill());
            } else if (chance < 4) {
                npc.doCast(FrequentSkill.ZAKEN_HOLD.getSkill());
            } else if (chance < 8) {
                npc.doCast(FrequentSkill.ZAKEN_DRAIN.getSkill());
            } else if (chance < 15 && target != ((Attackable) npc).getMostHated() && npc.isInsideRadius(target, 100, false, false)) {
                npc.doCast(FrequentSkill.ZAKEN_MASS_DUAL_ATTACK.getSkill());
            }

            if (Rnd.nextBoolean() && target == ((Attackable) npc).getMostHated()) {
                npc.doCast(FrequentSkill.ZAKEN_DUAL_ATTACK.getSkill());
            }

        }
    }

    protected void registerNpcs() {
        this.addAggroRangeEnterId(29022, 29023, 29024, 29026, 29027);
        this.addAttackId(29022);
        this.addFactionCallId(29023, 29024, 29026, 29027);
        this.addKillId(29022, 29023, 29024, 29026, 29027);
        this.addSkillSeeId(29022);
        this.addSpellFinishedId(29022);
        this.addGameTimeNotify();
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (GrandBossManager.getInstance().getBossStatus(29022) == 1 && !event.equalsIgnoreCase("zaken_unlock")) {
            return super.onAdvEvent(event, npc, player);
        } else {
            if (event.equalsIgnoreCase("1001")) {
                L2Skill skill;
                if (!GameTimeTaskManager.getInstance().isNight()) {
                    skill = FrequentSkill.ZAKEN_NIGHT_TO_DAY.getSkill();
                    if (npc.getFirstEffect(skill) == null) {
                        skill.getEffects(npc, npc);
                        this._teleportCheck = 3;
                    }

                    skill = FrequentSkill.ZAKEN_REGEN_DAY.getSkill();
                    if (npc.getFirstEffect(skill) == null) {
                        skill.getEffects(npc, npc);
                    }
                } else {
                    skill = FrequentSkill.ZAKEN_DAY_TO_NIGHT.getSkill();
                    if (npc.getFirstEffect(skill) == null) {
                        skill.getEffects(npc, npc);
                        this._zakenLocation.set(npc.getPosition());
                    }

                    skill = FrequentSkill.ZAKEN_REGEN_NIGHT.getSkill();
                    if (npc.getFirstEffect(skill) == null) {
                        skill.getEffects(npc, npc);
                    }

                    Creature mostHated = ((Attackable) npc).getMostHated();
                    if (npc.getAI().getDesire().getIntention() == IntentionType.ATTACK && !this._hasTeleported) {
                        boolean willTeleport = mostHated == null || !mostHated.isInsideRadius(this._zakenLocation, 1500, true, false);

                        if (willTeleport) {
                            Iterator var7 = VICTIMS.iterator();

                            while (var7.hasNext()) {
                                Player ply = (Player) var7.next();
                                if (ply.isInsideRadius(this._zakenLocation, 1500, true, false)) {
                                    willTeleport = false;
                                }
                            }
                        }

                        if (willTeleport) {
                            VICTIMS.clear();
                            npc.doCast(FrequentSkill.ZAKEN_SELF_TELE.getSkill());
                        }
                    }

                    if (Rnd.get(20) < 1 && !this._hasTeleported) {
                        this._zakenLocation.set(npc.getPosition());
                    }

                    if (npc.getAI().getDesire().getIntention() == IntentionType.ATTACK && mostHated != null) {
                        if (this._hate == 0) {
                            this._mostHated = mostHated;
                            this._hate = 1;
                        } else if (this._mostHated == mostHated) {
                            ++this._hate;
                        } else {
                            this._hate = 1;
                            this._mostHated = mostHated;
                        }
                    }

                    if (npc.getAI().getDesire().getIntention() == IntentionType.IDLE) {
                        this._hate = 0;
                    }

                    if (this._hate > 5) {
                        ((Attackable) npc).stopHating(this._mostHated);
                        this._hate = 0;
                    }
                }

                if (Rnd.get(40) < 1) {
                    npc.doCast(FrequentSkill.ZAKEN_SELF_TELE.getSkill());
                }

                this.startQuestTimer("1001", 30000L, npc, null, false);
            } else if (event.equalsIgnoreCase("1002")) {
                VICTIMS.clear();
                npc.doCast(FrequentSkill.ZAKEN_SELF_TELE.getSkill());
                this._hasTeleported = false;
            } else if (event.equalsIgnoreCase("1003")) {
                if (this._minionStatus == 1) {
                    this.spawnMinionOnEveryLocation(29026, 1);
                    this._minionStatus = 2;
                } else if (this._minionStatus == 2) {
                    this.spawnMinionOnEveryLocation(29023, 1);
                    this._minionStatus = 3;
                } else if (this._minionStatus == 3) {
                    this.spawnMinionOnEveryLocation(29024, 2);
                    this._minionStatus = 4;
                } else if (this._minionStatus == 4) {
                    this.spawnMinionOnEveryLocation(29027, 5);
                    this._minionStatus = 5;
                } else if (this._minionStatus == 5) {
                    this.addSpawn(29023, 52675, 219371, -3290, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 52687, 219596, -3368, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 52672, 219740, -3418, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 52857, 219992, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 52959, 219997, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 53381, 220151, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54236, 220948, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54885, 220144, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55264, 219860, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55399, 220263, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55679, 220129, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 56276, 220783, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 57173, 220234, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 56267, 218826, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56294, 219482, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 56094, 219113, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56364, 218967, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 57113, 218079, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56186, 217153, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55440, 218081, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55202, 217940, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55225, 218236, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54973, 218075, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 53412, 218077, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54226, 218797, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54394, 219067, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54139, 219253, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 54262, 219480, -3488, Rnd.get(65536), false, 0L, true);
                    this._minionStatus = 6;
                } else if (this._minionStatus == 6) {
                    this.addSpawn(29027, 53412, 218077, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54413, 217132, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 54841, 217132, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 55372, 217128, -3343, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 55893, 217122, -3488, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 56282, 217237, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 56963, 218080, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 56267, 218826, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56294, 219482, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 56094, 219113, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56364, 218967, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 56276, 220783, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 57173, 220234, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54885, 220144, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55264, 219860, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55399, 220263, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55679, 220129, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54236, 220948, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54464, 219095, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54226, 218797, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54394, 219067, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54139, 219253, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 54262, 219480, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 53412, 218077, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55440, 218081, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55202, 217940, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55225, 218236, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54973, 218075, -3216, Rnd.get(65536), false, 0L, true);
                    this._minionStatus = 7;
                } else if (this._minionStatus == 7) {
                    this.addSpawn(29027, 54228, 217504, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54181, 217168, -3216, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 54714, 217123, -3168, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 55298, 217127, -3073, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 55787, 217130, -2993, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 56284, 217216, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 56963, 218080, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 56267, 218826, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56294, 219482, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 56094, 219113, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 56364, 218967, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 56276, 220783, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 57173, 220234, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54885, 220144, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55264, 219860, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55399, 220263, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55679, 220129, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54236, 220948, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54464, 219095, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54226, 218797, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29024, 54394, 219067, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54139, 219253, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29023, 54262, 219480, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 53412, 218077, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 54280, 217200, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55440, 218081, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29026, 55202, 217940, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 55225, 218236, -2944, Rnd.get(65536), false, 0L, true);
                    this.addSpawn(29027, 54973, 218075, -2944, Rnd.get(65536), false, 0L, true);
                    this.cancelQuestTimer("1003", null, null);
                }
            } else if (event.equalsIgnoreCase("zaken_unlock")) {
                this.spawnBoss(true);
            } else if (event.equalsIgnoreCase("CreateOnePrivateEx")) {
                this.addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), Rnd.get(65535), false, 0L, true);
            }

            return super.onAdvEvent(event, npc, player);
        }
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        Playable realBypasser = isPet && player.getSummon() != null ? player.getSummon() : player;
        if (ZONE.isInsideZone(npc)) {
            ((Attackable) npc).addDamageHate(realBypasser, 1, 200);
        }

        if (npc.getNpcId() == 29022) {
            if (Rnd.get(3) < 1 && VICTIMS.size() < 5) {
                VICTIMS.add(player);
            }

            if (Rnd.get(15) < 1) {
                callSkills(npc, realBypasser);
            }
        } else if (testCursesOnAggro(npc, realBypasser)) {
            return null;
        }

        return super.onAggro(npc, player, isPet);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (attacker instanceof Playable && testCursesOnAttack(npc, attacker)) {
            return null;
        } else {
            if (Rnd.get(10) < 1) {
                callSkills(npc, attacker);
            }

            if (!GameTimeTaskManager.getInstance().isNight() && npc.getCurrentHp() < (double) (npc.getMaxHp() * this._teleportCheck / 4)) {
                --this._teleportCheck;
                npc.doCast(FrequentSkill.ZAKEN_SELF_TELE.getSkill());
            }

            return super.onAttack(npc, attacker, damage, skill);
        }
    }

    public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        if (caller.getNpcId() == 29022 && GameTimeTaskManager.getInstance().isNight() && npc.getAI().getDesire().getIntention() == IntentionType.IDLE && !this._hasTeleported && caller.getCurrentHp() < 0.9D * (double) caller.getMaxHp() && Rnd.get(450) < 1) {
            this._hasTeleported = true;
            this._zakenLocation.set(npc.getPosition());
            this.startQuestTimer("1002", 300L, caller, null, false);
        }

        return super.onFactionCall(npc, caller, attacker, isPet);
    }

    public String onKill(Npc npc, Creature killer) {
        if (!Config.FWA_FIXTIMEPATTERNOFZAKEN.isEmpty()) {
            if (npc.getNpcId() == 29022) {
                npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
                GrandBossManager.getInstance().setBossStatus(29022, 1);
                this.cancelQuestTimer("1001", npc, null);
                this.cancelQuestTimer("1003", null, null);
                this.startQuestTimer("zaken_unlock", getRespawnInterval(), null, null, false);
                StatSet info = GrandBossManager.getInstance().getStatsSet(29022);
                info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
                GrandBossManager.getInstance().setStatsSet(29022, info);
            } else if (GrandBossManager.getInstance().getBossStatus(29022) == 0) {
                this.startQuestTimer("CreateOnePrivateEx", (30 + Rnd.get(60)) * 1000L, npc, null, false);
            }
        } else if (npc.getNpcId() == 29022) {
            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            GrandBossManager.getInstance().setBossStatus(29022, 1);
            long respawnTime = (long) (Config.SPAWN_INTERVAL_ZAKEN + Rnd.get(-Config.RANDOM_SPAWN_TIME_ZAKEN, Config.RANDOM_SPAWN_TIME_ZAKEN)) * 3600000L;
            this.cancelQuestTimer("1001", npc, null);
            this.cancelQuestTimer("1003", null, null);
            this.startQuestTimer("zaken_unlock", respawnTime, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29022);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29022, info);
        } else if (GrandBossManager.getInstance().getBossStatus(29022) == 0) {
            this.startQuestTimer("CreateOnePrivateEx", (30 + Rnd.get(60)) * 1000L, npc, null, false);
        }

        return super.onKill(npc, killer);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (Rnd.get(12) < 1) {
            callSkills(npc, caster);
        }

        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        switch (skill.getId()) {
            case 4216:
                ((Attackable) npc).stopHating(player);
                player.teleportTo(Rnd.get(LOCS), 0);
                break;
            case 4217:
                Iterator var4 = VICTIMS.iterator();

                while (var4.hasNext()) {
                    Player ply = (Player) var4.next();
                    if (ply.isInsideRadius(player, 250, true, false)) {
                        ((Attackable) npc).stopHating(ply);
                        ply.teleportTo(Rnd.get(LOCS), 0);
                    }
                }

                ((Attackable) npc).stopHating(player);
                player.teleportTo(Rnd.get(LOCS), 0);
                break;
            case 4222:
                ((Attackable) npc).cleanAllHate();
                npc.teleportTo(this._zakenLocation, 0);
        }

        return super.onSpellFinished(npc, player, skill);
    }

    public void onGameTime() {
        if (GameTimeTaskManager.getInstance().getGameTime() == 0) {
            Door door = DoorData.getInstance().getDoor(21240006);
            if (door != null) {
                door.openMe();
            }
        }

    }

    private void spawnBoss(boolean freshStart) {
        GrandBoss zaken;
        if (freshStart) {
            GrandBossManager.getInstance().setBossStatus(29022, 0);
            Location loc = Rnd.get(LOCS);
            zaken = (GrandBoss) this.addSpawn(29022, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0L, false);
        } else {
            StatSet info = GrandBossManager.getInstance().getStatsSet(29022);
            zaken = (GrandBoss) this.addSpawn(29022, info.getInteger("loc_x"), info.getInteger("loc_y"), info.getInteger("loc_z"), info.getInteger("heading"), false, 0L, false);
            zaken.setCurrentHpMp(info.getInteger("currentHP"), info.getInteger("currentMP"));
        }

        GrandBossManager.getInstance().addBoss(zaken);
        this._teleportCheck = 3;
        this._hate = 0;
        this._hasTeleported = false;
        this._mostHated = null;
        this._zakenLocation.set(zaken.getPosition());
        VICTIMS.clear();
        if (ZONE.isInsideZone(zaken)) {
            this._minionStatus = 1;
            this.startQuestTimer("1003", 1700L, null, null, true);
        }

        this.startQuestTimer("1001", 1000L, zaken, null, false);
        zaken.broadcastPacket(new PlaySound(1, "BS01_A", zaken));
    }

    private void spawnMinionOnEveryLocation(int npcId, int roundsNumber) {
        Location[] var3 = LOCS;
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Location loc = var3[var5];

            for (int i = 0; i < roundsNumber; ++i) {
                int x = loc.getX() + Rnd.get(650);
                int y = loc.getY() + Rnd.get(650);
                this.addSpawn(npcId, x, y, loc.getZ(), Rnd.get(65536), false, 0L, true);
            }
        }

    }
}