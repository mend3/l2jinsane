/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sailren extends L2AttackableAIScript {
    public static final int SAILREN = 29065;
    public static final byte DORMANT = 0;
    public static final byte FIGHTING = 1;
    public static final byte DEAD = 2;
    private static final BossZone SAILREN_LAIR = ZoneManager.getInstance().getZoneById(110011, BossZone.class);
    private static final int VELOCIRAPTOR = 22223;
    private static final int PTEROSAUR = 22199;
    private static final int TREX = 22217;
    private static final int DUMMY = 32110;
    private static final int CUBE = 32107;
    private static final long INTERVAL_CHECK = 600000L;
    private static final SpawnLocation SAILREN_LOC = new SpawnLocation(27549, -6638, -2008, 0);
    private static long _timeTracker = 0L;
    private final List<Npc> _mobs = new CopyOnWriteArrayList();

    public Sailren() {
        super("ai/individual");
        StatSet info = GrandBossManager.getInstance().getStatsSet(29065);
        switch (GrandBossManager.getInstance().getBossStatus(29065)) {
            case 1:
                int loc_x = info.getInteger("loc_x");
                int loc_y = info.getInteger("loc_y");
                int loc_z = info.getInteger("loc_z");
                int heading = info.getInteger("heading");
                int hp = info.getInteger("currentHP");
                int mp = info.getInteger("currentMP");
                Npc sailren = this.addSpawn(29065, loc_x, loc_y, loc_z, heading, false, 0L, false);
                GrandBossManager.getInstance().addBoss((GrandBoss) sailren);
                this._mobs.add(sailren);
                sailren.setCurrentHpMp(hp, mp);
                sailren.setRunning();
                this.startQuestTimer("inactivity", 600000L, null, null, true);
                break;
            case 2:
                long temp = info.getLong("respawn_time") - System.currentTimeMillis();
                if (temp > 0L) {
                    this.startQuestTimer("unlock", temp, null, null, false);
                } else {
                    GrandBossManager.getInstance().setBossStatus(29065, 0);
                }
        }

    }

    protected void registerNpcs() {
        this.addAttackId(22223, 22199, 22217, 29065);
        this.addKillId(22223, 22199, 22217, 29065);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        Npc mob;
        if (event.equalsIgnoreCase("beginning")) {
            _timeTracker = 0L;

            for (int i = 0; i < 3; ++i) {
                mob = this.addSpawn(22223, SAILREN_LOC, true, 0L, false);
                mob.getAI().setIntention(IntentionType.ACTIVE);
                mob.setRunning();
                this._mobs.add(mob);
            }

            this.startQuestTimer("inactivity", 600000L, null, null, true);
        } else {
            Npc temp;
            if (event.equalsIgnoreCase("spawn")) {
                temp = this.addSpawn(32110, SAILREN_LOC, false, 26000L, false);
                SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
                this.startQuestTimer("skill", 2500L, temp, null, true);
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(temp.getObjectId(), 60, 110, 30, 4000, 4000, 0, 65, 1, 0));
                this.startQuestTimer("camera_0", 3900L, temp, null, false);
                this.startQuestTimer("camera_1", 6800L, temp, null, false);
                this.startQuestTimer("camera_2", 9700L, temp, null, false);
                this.startQuestTimer("camera_3", 12600L, temp, null, false);
                this.startQuestTimer("camera_4", 15500L, temp, null, false);
                this.startQuestTimer("camera_5", 18400L, temp, null, false);
            } else if (event.equalsIgnoreCase("skill")) {
                SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
            } else if (event.equalsIgnoreCase("camera_0")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 100, 180, 30, 3000, 3000, 0, 50, 1, 0));
            } else if (event.equalsIgnoreCase("camera_1")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 150, 270, 25, 3000, 3000, 0, 30, 1, 0));
            } else if (event.equalsIgnoreCase("camera_2")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 360, 20, 3000, 3000, 10, 15, 1, 0));
            } else if (event.equalsIgnoreCase("camera_3")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 450, 10, 3000, 3000, 0, 10, 1, 0));
            } else if (event.equalsIgnoreCase("camera_4")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 560, 0, 3000, 3000, 0, 10, 1, 0));
                temp = this.addSpawn(29065, SAILREN_LOC, false, 0L, false);
                GrandBossManager.getInstance().addBoss((GrandBoss) temp);
                this._mobs.add(temp);
                this.cancelQuestTimers("skill");
                SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5091, 1, 2500, 0));
                temp.broadcastPacket(new SocialAction(temp, 2));
            } else if (event.equalsIgnoreCase("camera_5")) {
                SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 70, 560, 0, 500, 7000, -15, 10, 1, 0));
            } else if (event.equalsIgnoreCase("unlock")) {
                GrandBossManager.getInstance().setBossStatus(29065, 0);
            } else if (event.equalsIgnoreCase("inactivity")) {
                if (System.currentTimeMillis() - _timeTracker >= 600000L) {
                    GrandBossManager.getInstance().setBossStatus(29065, 0);
                    if (!this._mobs.isEmpty()) {
                        Iterator var7 = this._mobs.iterator();

                        while (var7.hasNext()) {
                            mob = (Npc) var7.next();
                            mob.deleteMe();
                        }

                        this._mobs.clear();
                    }

                    SAILREN_LAIR.oustAllPlayers();
                    this.cancelQuestTimers("inactivity");
                }
            } else if (event.equalsIgnoreCase("oust")) {
                SAILREN_LAIR.oustAllPlayers();
            }
        }

        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        if (killer instanceof Playable) {
            Player player = killer.getActingPlayer();
            if (player == null || !this._mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(player.getObjectId())) {
                return null;
            }
        }

        Npc temp;
        switch (npc.getNpcId()) {
            case 22199:
                if (this._mobs.remove(npc)) {
                    temp = this.addSpawn(22217, SAILREN_LOC, false, 0L, false);
                    temp.setRunning();
                    temp.getAI().setIntention(IntentionType.ATTACK, killer);
                    temp.broadcastNpcSay("?");
                    this._mobs.add(temp);
                }
                break;
            case 22217:
                if (this._mobs.remove(npc)) {
                    this.startQuestTimer("spawn", Config.WAIT_TIME_SAILREN, npc, null, false);
                }
                break;
            case 22223:
                if (this._mobs.remove(npc) && this._mobs.isEmpty()) {
                    temp = this.addSpawn(22199, SAILREN_LOC, false, 0L, false);
                    temp.setRunning();
                    temp.getAI().setIntention(IntentionType.ATTACK, killer);
                    this._mobs.add(temp);
                }
                break;
            case 29065:
                if (this._mobs.remove(npc)) {
                    GrandBossManager.getInstance().setBossStatus(29065, 2);
                    this.addSpawn(32107, npc, false, 600000L, false);
                    this.cancelQuestTimers("inactivity");
                    long respawnTime = (long) Config.SPAWN_INTERVAL_SAILREN + (long) Rnd.get(-Config.RANDOM_SPAWN_TIME_SAILREN, Config.RANDOM_SPAWN_TIME_SAILREN);
                    respawnTime *= 3600000L;
                    this.startQuestTimer("oust", 600000L, null, null, false);
                    this.startQuestTimer("unlock", respawnTime, null, null, false);
                    StatSet info = GrandBossManager.getInstance().getStatsSet(29065);
                    info.set("respawn_time", System.currentTimeMillis() + respawnTime);
                    GrandBossManager.getInstance().setStatsSet(29065, info);
                }
        }

        return super.onKill(npc, killer);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (attacker instanceof Playable) {
            Player player = attacker.getActingPlayer();
            if (player == null || !this._mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(player.getObjectId())) {
                return null;
            }

            if (testCursesOnAttack(npc, attacker, 29065)) {
                return null;
            }

            _timeTracker = System.currentTimeMillis();
        }

        return super.onAttack(npc, attacker, damage, skill);
    }
}