/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.Iterator;

public class DrChaos extends L2AttackableAIScript {
    private static final int DOCTOR_CHAOS = 32033;
    private static final int CHAOS_GOLEM = 25512;
    private static final byte NORMAL = 0;
    private static final byte CRAZY = 1;
    private static final byte DEAD = 2;
    private static final String[] SHOUTS = new String[]{"Bwah-ha-ha! Your doom is at hand! Behold the Ultra Secret Super Weapon!", "Foolish, insignificant creatures! How dare you challenge me!", "I see that none will challenge me now!"};
    private long _lastAttackTime = 0L;
    private int _pissedOffTimer;

    public DrChaos() {
        super("ai/individual");
        this.addFirstTalkId(32033);
        this.addSpawnId(32033);
        StatSet info = GrandBossManager.getInstance().getStatsSet(25512);
        int status = GrandBossManager.getInstance().getBossStatus(25512);
        if (status == 2) {
            long temp = info.getLong("respawn_time") - System.currentTimeMillis();
            if (temp > 0L) {
                this.startQuestTimer("reset_drchaos", temp, null, null, false);
            } else {
                this.addSpawn(32033, 96320, -110912, -3328, 8191, false, 0L, false);
                GrandBossManager.getInstance().setBossStatus(25512, 0);
            }
        } else if (status == 1) {
            int loc_x = info.getInteger("loc_x");
            int loc_y = info.getInteger("loc_y");
            int loc_z = info.getInteger("loc_z");
            int heading = info.getInteger("heading");
            int hp = info.getInteger("currentHP");
            int mp = info.getInteger("currentMP");
            GrandBoss golem = (GrandBoss) this.addSpawn(25512, loc_x, loc_y, loc_z, heading, false, 0L, false);
            GrandBossManager.getInstance().addBoss(golem);
            golem.setCurrentHpMp(hp, mp);
            golem.setRunning();
            this._lastAttackTime = System.currentTimeMillis();
            this.startQuestTimer("golem_despawn", 60000L, golem, null, true);
        } else {
            this.addSpawn(32033, 96320, -110912, -3328, 8191, false, 0L, false);
        }

    }

    protected void registerNpcs() {
        this.addKillId(25512);
        this.addAttackActId(25512);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("reset_drchaos")) {
            GrandBossManager.getInstance().setBossStatus(25512, 0);
            this.addSpawn(32033, 96320, -110912, -3328, 8191, false, 0L, false);
        } else if (event.equalsIgnoreCase("golem_despawn") && npc != null) {
            if (npc.getNpcId() == 25512 && this._lastAttackTime + 1800000L < System.currentTimeMillis()) {
                npc.deleteMe();
                this.addSpawn(32033, 96320, -110912, -3328, 8191, false, 0L, false);
                GrandBossManager.getInstance().setBossStatus(25512, 0);
                this.cancelQuestTimer("golem_despawn", npc, null);
            }
        } else if (event.equalsIgnoreCase("1")) {
            npc.broadcastPacket(new SocialAction(npc, 2));
            npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -200, 15, 5500, 13500, 0, 0, 1, 0));
        } else if (event.equalsIgnoreCase("2")) {
            npc.broadcastPacket(new SocialAction(npc, 3));
        } else if (event.equalsIgnoreCase("3")) {
            npc.broadcastPacket(new SocialAction(npc, 1));
        } else if (event.equalsIgnoreCase("4")) {
            npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3500, 5000, 0, 0, 1, 0));
            npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(95928, -110671, -3340));
        } else if (event.equalsIgnoreCase("5")) {
            npc.deleteMe();
            GrandBoss golem = (GrandBoss) this.addSpawn(25512, 96080, -110822, -3343, 0, false, 0L, false);
            GrandBossManager.getInstance().addBoss(golem);
            npc = golem;
            golem.broadcastPacket(new SpecialCamera(golem.getObjectId(), 30, 200, 20, 6000, 8000, 0, 0, 1, 0));
            golem.broadcastPacket(new SocialAction(golem, 1));
            golem.broadcastPacket(new PlaySound(1, "Rm03_A", golem));
            this._lastAttackTime = System.currentTimeMillis();
            this.startQuestTimer("golem_despawn", 60000L, golem, null, true);
        } else if (event.equalsIgnoreCase("paranoia_activity") && GrandBossManager.getInstance().getBossStatus(25512) == 0) {
            Iterator var6 = npc.getKnownTypeInRadius(Player.class, 500).iterator();

            while (var6.hasNext()) {
                Player obj = (Player) var6.next();
                if (!obj.isDead()) {
                    --this._pissedOffTimer;
                    if (this._pissedOffTimer == 15) {
                        npc.broadcastNpcSay("How dare you trespass into my territory! Have you no fear?");
                    } else if (this._pissedOffTimer <= 0) {
                        this.crazyMidgetBecomesAngry(npc);
                    }
                    break;
                }
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    public String onFirstTalk(Npc npc, Player player) {
        String htmltext = "";
        if (GrandBossManager.getInstance().getBossStatus(25512) == 0) {
            this._pissedOffTimer -= Rnd.get(1, 5);
            if (this._pissedOffTimer > 20) {
                htmltext = "<html><body>Doctor Chaos:<br>What?! Who are you? How did you come here?<br>You really look suspicious... Aren't those filthy members of Black Anvil guild send you? No? Mhhhhh... I don't trust you!</body></html>";
            } else if (this._pissedOffTimer > 10 && this._pissedOffTimer <= 20) {
                htmltext = "<html><body>Doctor Chaos:<br>Why are you standing here? Don't you see it's a private propertie? Don't look at him with those eyes... Did you smile?! Don't make fun of me! He will ... destroy ... you ... if you continue!</body></html>";
            } else if (this._pissedOffTimer > 0 && this._pissedOffTimer <= 10) {
                htmltext = "<html><body>Doctor Chaos:<br>I know why you are here, traitor! He discovered your plans! You are assassin ... sent by the Black Anvil guild! But you won't kill the Emperor of Evil!</body></html>";
            } else if (this._pissedOffTimer <= 0) {
                this.crazyMidgetBecomesAngry(npc);
            }
        }

        return htmltext;
    }

    public String onSpawn(Npc npc) {
        this._pissedOffTimer = 30;
        this.startQuestTimer("paranoia_activity", 1000L, npc, null, true);
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        this.cancelQuestTimer("golem_despawn", npc, null);
        npc.broadcastNpcSay("Urggh! You will pay dearly for this insult.");
        long respawnTime = (36 + Rnd.get(-24, 24)) * 3600000L;
        GrandBossManager.getInstance().setBossStatus(25512, 2);
        this.startQuestTimer("reset_drchaos", respawnTime, null, null, false);
        StatSet info = GrandBossManager.getInstance().getStatsSet(25512);
        info.set("respawn_time", System.currentTimeMillis() + respawnTime);
        GrandBossManager.getInstance().setStatsSet(25512, info);
        return null;
    }

    public String onAttackAct(Npc npc, Player victim) {
        int chance = Rnd.get(300);
        if (chance < 3) {
            npc.broadcastNpcSay(SHOUTS[chance]);
        }

        return null;
    }

    private void crazyMidgetBecomesAngry(Npc npc) {
        if (GrandBossManager.getInstance().getBossStatus(25512) == 0) {
            GrandBossManager.getInstance().setBossStatus(25512, 1);
            this.cancelQuestTimer("paranoia_activity", npc, null);
            npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(96323, -110914, -3328));
            npc.broadcastNpcSay("Fools! Why haven't you fled yet? Prepare to learn a lesson!");
            this.startQuestTimer("1", 2000L, npc, null, false);
            this.startQuestTimer("2", 4000L, npc, null, false);
            this.startQuestTimer("3", 6500L, npc, null, false);
            this.startQuestTimer("4", 12500L, npc, null, false);
            this.startQuestTimer("5", 17000L, npc, null, false);
        }
    }
}