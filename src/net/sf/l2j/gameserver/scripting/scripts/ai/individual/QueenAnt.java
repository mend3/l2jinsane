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
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class QueenAnt extends L2AttackableAIScript {
    private static final BossZone ZONE = ZoneManager.getInstance().getZoneById(110017, BossZone.class);
    private static final int QUEEN = 29001;
    private static final int LARVA = 29002;
    private static final int NURSE = 29003;
    private static final int GUARD = 29004;
    private static final int ROYAL = 29005;
    private static final Location[] PLAYER_TELE_OUT = new Location[]{new Location(-19480, 187344, -5600), new Location(-17928, 180912, -5520), new Location(-23808, 182368, -5600)};
    private static final byte ALIVE = 0;
    private static final byte DEAD = 1;
    private Monster _larva = null;

    public QueenAnt() {
        super("ai/individual");
        if (GrandBossManager.getInstance().getBossStatus(29001) == 1) {
            long temp = GrandBossManager.getInstance().getStatsSet(29001).getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("queen_unlock", temp, null, null, false);
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
            timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFQA);
            long delay = timePattern.next(now) - now;
            return Math.max(60000L, delay);
        } catch (InvalidPatternException var5) {
            throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFQA + "\" in " + QueenAnt.class.getSimpleName(), var5);
        }
    }

    protected void registerNpcs() {
        this.addAttackId(29001, 29002, 29003, 29004, 29005);
        this.addAggroRangeEnterId(29002, 29003, 29004, 29005);
        this.addFactionCallId(29001, 29003);
        this.addKillId(29001, 29003, 29005);
        this.addSkillSeeId(29001, 29002, 29003, 29004, 29005);
        this.addSpawnId(29002, 29003);
        this.addExitZoneId(110017);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("action")) {
            if (Rnd.get(10) < 3) {
                npc.broadcastPacket(new SocialAction(npc, Rnd.nextBoolean() ? 3 : 4));
            }

            ((Monster) npc).getMinionList().getSpawnedMinions().stream().filter((m) -> {
                return m.getNpcId() == 29005 && !ZONE.isInsideZone(m);
            }).forEach(Monster::teleToMaster);
        } else if (event.equalsIgnoreCase("chaos")) {
            ((Monster) npc).getMinionList().getSpawnedMinions().stream().filter((m) -> {
                return m.getNpcId() == 29005 && m.isInCombat() && Rnd.get(100) < 66;
            }).forEach((m) -> {
                ((AttackableAI) m.getAI()).aggroReconsider();
            });
            this.startQuestTimer("chaos", 90000 + Rnd.get(240000), npc, null, false);
        } else if (event.equalsIgnoreCase("clean")) {
            this._larva.deleteMe();
            this._larva = null;
        } else if (event.equalsIgnoreCase("queen_unlock")) {
            if (Rnd.get(100) < 33) {
                ZONE.movePlayersTo(PLAYER_TELE_OUT[0]);
            } else if (Rnd.nextBoolean()) {
                ZONE.movePlayersTo(PLAYER_TELE_OUT[1]);
            } else {
                ZONE.movePlayersTo(PLAYER_TELE_OUT[2]);
            }

            this.spawnBoss(true);
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        Playable realBypasser = isPet && player.getSummon() != null ? player.getSummon() : player;
        return testCursesOnAggro(npc, realBypasser) ? null : super.onAggro(npc, player, isPet);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (attacker instanceof Playable) {
            if (testCursesOnAttack(npc, attacker, 29001)) {
                return null;
            }

            if (npc.getNpcId() == 29001 && !npc.isCastingNow()) {
                if (skill != null && skill.getElement() == 2 && Rnd.get(100) < 70) {
                    npc.setTarget(attacker);
                    ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
                } else {
                    double dist = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
                    if (dist > 500.0D && Rnd.get(100) < 10) {
                        npc.setTarget(attacker);
                        ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
                    } else if (dist > 150.0D && Rnd.get(100) < 10) {
                        npc.setTarget(attacker);
                        ((Monster) npc).useMagic(Rnd.get(10) < 8 ? FrequentSkill.QUEEN_ANT_STRIKE.getSkill() : FrequentSkill.QUEEN_ANT_SPRINKLE.getSkill());
                    } else if (dist < 250.0D && Rnd.get(100) < 5) {
                        npc.setTarget(attacker);
                        ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_BRANDISH.getSkill());
                    }
                }
            }
        }

        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onExitZone(Creature character, ZoneType zone) {
        if (character instanceof GrandBoss queen) {
            if (queen.getNpcId() == 29001) {
                queen.teleportTo(-21610, 181594, -5734, 0);
            }
        }

        return super.onExitZone(character, zone);
    }

    public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        if (npc.isCastingNow()) {
            return null;
        } else {
            switch (npc.getNpcId()) {
                case 29001:
                    Playable realAttacker = isPet && attacker.getSummon() != null ? attacker.getSummon() : attacker;
                    double dist = Math.sqrt(npc.getPlanDistanceSq(realAttacker.getX(), realAttacker.getY()));
                    if (dist > 500.0D && Rnd.get(100) < 3) {
                        npc.setTarget(realAttacker);
                        ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
                    } else if (dist > 150.0D && Rnd.get(100) < 3) {
                        npc.setTarget(realAttacker);
                        ((Monster) npc).useMagic(Rnd.get(10) < 8 ? FrequentSkill.QUEEN_ANT_STRIKE.getSkill() : FrequentSkill.QUEEN_ANT_SPRINKLE.getSkill());
                    } else if (dist < 250.0D && Rnd.get(100) < 2) {
                        npc.setTarget(realAttacker);
                        ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_BRANDISH.getSkill());
                    }
                    break;
                case 29003:
                    if (caller.getNpcId() == 29002) {
                        npc.setTarget(caller);
                        ((Monster) npc).useMagic(Rnd.nextBoolean() ? FrequentSkill.NURSE_HEAL_1.getSkill() : FrequentSkill.NURSE_HEAL_2.getSkill());
                    } else if (caller.getNpcId() == 29001) {
                        if (this._larva != null && this._larva.getCurrentHp() < (double) this._larva.getMaxHp()) {
                            npc.setTarget(this._larva);
                            ((Monster) npc).useMagic(Rnd.nextBoolean() ? FrequentSkill.NURSE_HEAL_1.getSkill() : FrequentSkill.NURSE_HEAL_2.getSkill());
                        } else {
                            npc.setTarget(caller);
                            ((Attackable) npc).useMagic(FrequentSkill.NURSE_HEAL_1.getSkill());
                        }
                    }
            }

            return null;
        }
    }

    public String onKill(Npc npc, Creature killer) {
        Monster master;
        Monster minion;
        if (!Config.FWA_FIXTIMEPATTERNOFQA.isEmpty()) {
            if (npc.getNpcId() != 29001) {
                minion = (Monster) npc;
                master = minion.getMaster();
                if (master != null && master.hasMinions()) {
                    master.getMinionList().onMinionDie(minion, npc.getNpcId() == 29003 ? 10000 : 280000 + Rnd.get(40) * 1000);
                }

                return null;
            }

            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            GrandBossManager.getInstance().setBossStatus(29001, 1);
            this.cancelQuestTimer("action", npc, null);
            this.cancelQuestTimer("chaos", npc, null);
            this.startQuestTimer("queen_unlock", getRespawnInterval(), null, null, false);
            this.startQuestTimer("clean", 5000L, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29001);
            info.set("respawn_time", System.currentTimeMillis() + getRespawnInterval());
            GrandBossManager.getInstance().setStatsSet(29001, info);
        } else {
            if (npc.getNpcId() != 29001) {
                minion = (Monster) npc;
                master = minion.getMaster();
                if (master != null && master.hasMinions()) {
                    master.getMinionList().onMinionDie(minion, npc.getNpcId() == 29003 ? 10000 : 280000 + Rnd.get(40) * 1000);
                }

                return null;
            }

            npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
            GrandBossManager.getInstance().setBossStatus(29001, 1);
            long respawnTime = (long) (Config.SPAWN_INTERVAL_AQ + Rnd.get(-Config.RANDOM_SPAWN_TIME_AQ, Config.RANDOM_SPAWN_TIME_AQ)) * 3600000L;
            this.cancelQuestTimer("action", npc, null);
            this.cancelQuestTimer("chaos", npc, null);
            this.startQuestTimer("queen_unlock", respawnTime, null, null, false);
            this.startQuestTimer("clean", 5000L, null, null, false);
            StatSet info = GrandBossManager.getInstance().getStatsSet(29001);
            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
            GrandBossManager.getInstance().setStatsSet(29001, info);
        }

        return super.onKill(npc, killer);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        Playable realAttacker = isPet && caster.getSummon() != null ? caster.getSummon() : caster;
        if (!Config.RAID_DISABLE_CURSE && realAttacker.getLevel() - npc.getLevel() > 8) {
            L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
            npc.broadcastPacket(new MagicSkillUse(npc, realAttacker, curse.getId(), curse.getLevel(), 300, 0));
            curse.getEffects(npc, realAttacker);
            ((Attackable) npc).stopHating(realAttacker);
            return null;
        } else {
            if (npc.getNpcId() == 29001 && !npc.isCastingNow() && skill.getAggroPoints() > 0 && Rnd.get(100) < 15) {
                npc.setTarget(realAttacker);
                ((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
            }

            return super.onSkillSee(npc, caster, skill, targets, isPet);
        }
    }

    public String onSpawn(Npc npc) {
        switch (npc.getNpcId()) {
            case 29002:
                npc.setIsMortal(false);
                npc.setIsImmobilized(true);
            case 29003:
                npc.disableCoreAI(true);
            default:
                return super.onSpawn(npc);
        }
    }

    private void spawnBoss(boolean freshStart) {
        GrandBoss queen;
        if (freshStart) {
            GrandBossManager.getInstance().setBossStatus(29001, 0);
            queen = (GrandBoss) this.addSpawn(29001, -21610, 181594, -5734, 0, false, 0L, false);
        } else {
            StatSet info = GrandBossManager.getInstance().getStatsSet(29001);
            queen = (GrandBoss) this.addSpawn(29001, info.getInteger("loc_x"), info.getInteger("loc_y"), info.getInteger("loc_z"), info.getInteger("heading"), false, 0L, false);
            queen.setCurrentHpMp(info.getInteger("currentHP"), info.getInteger("currentMP"));
        }

        GrandBossManager.getInstance().addBoss(queen);
        this.startQuestTimer("action", 10000L, queen, null, true);
        this.startQuestTimer("chaos", 90000 + Rnd.get(240000), queen, null, false);
        queen.broadcastPacket(new PlaySound(1, "BS01_A", queen));
        this._larva = (Monster) this.addSpawn(29002, -21600, 179482, -5846, Rnd.get(360), false, 0L, false);
    }
}