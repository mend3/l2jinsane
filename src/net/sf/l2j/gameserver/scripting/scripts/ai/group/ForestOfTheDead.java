package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.enums.BossStatus;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.spawn.BossSpawn;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

import java.util.ArrayList;
import java.util.List;

public class ForestOfTheDead extends L2AttackableAIScript {
    public static final SpawnLocation HELLMANN_DAY_LOC = new SpawnLocation(-104100, -252700, -15542, 0);

    public static final SpawnLocation HELLMANN_NIGHT_LOC = new SpawnLocation(59050, -42200, -3003, 100);

    private static final String qn = "Q024_InhabitantsOfTheForestOfTheDead";

    private static final int HELLMANN = 25328;

    private static final int NIGHT_DORIAN = 25332;

    private static final int LIDIA_MAID = 31532;

    private static final int DAY_VIOLET = 31386;

    private static final int DAY_KURSTIN = 31387;

    private static final int DAY_MINA = 31388;

    private static final int DAY_DORIAN = 31389;

    private static final int SILVER_CROSS = 7153;

    private static final int BROKEN_SILVER_CROSS = 7154;

    private final List<Npc> _cursedVillageNpcs = new ArrayList<>();

    private final Npc _lidiaMaid;

    public ForestOfTheDead() {
        super("ai/group");
        this._cursedVillageNpcs.add(addSpawn(31386, 59618, -42774, -3000, 5636, false, 0L, false));
        this._cursedVillageNpcs.add(addSpawn(31387, 58790, -42646, -3000, 240, false, 0L, false));
        this._cursedVillageNpcs.add(addSpawn(31388, 59626, -41684, -3000, 48457, false, 0L, false));
        this._cursedVillageNpcs.add(addSpawn(31389, 60161, -42086, -3000, 30212, false, 0L, false));
        this._lidiaMaid = addSpawn(31532, 47108, -36189, -1624, -22192, false, 0L, false);
        if (!GameTimeTaskManager.getInstance().isNight()) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25328);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                raid.getSpawn().setLoc(HELLMANN_DAY_LOC);
                raid.teleportTo(HELLMANN_DAY_LOC, 0);
            }
            this._lidiaMaid.getSpawn().setRespawnState(false);
            this._lidiaMaid.deleteMe();
        } else {
            despawnCursedVillageNpcs();
            this._lidiaMaid.getSpawn().setRespawnState(true);
            this._lidiaMaid.getSpawn().doSpawn(false);
        }
    }

    protected void registerNpcs() {
        addCreatureSeeId(25332);
        addGameTimeNotify();
    }

    public String onCreatureSee(Npc npc, Creature creature) {
        if (creature instanceof Player) {
            Player player = creature.getActingPlayer();
            QuestState st = player.getQuestState("Q024_InhabitantsOfTheForestOfTheDead");
            if (st == null)
                return super.onCreatureSee(npc, creature);
            if (st.getInt("cond") == 3) {
                st.set("cond", "4");
                st.takeItems(7153, -1);
                st.giveItems(7154, 1);
                st.playSound("ItemSound.quest_middle");
                npc.broadcastNpcSay("That sign!");
            }
        }
        return super.onCreatureSee(npc, creature);
    }

    public void onGameTime() {
        if (GameTimeTaskManager.getInstance().getGameTime() == 360) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25328);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                raid.getSpawn().setLoc(HELLMANN_DAY_LOC);
                raid.teleportTo(HELLMANN_DAY_LOC, 0);
            }
            spawnCursedVillageNpcs();
        } else if (GameTimeTaskManager.getInstance().getGameTime() == 0) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25328);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                raid.getSpawn().setLoc(HELLMANN_NIGHT_LOC);
                raid.teleportTo(HELLMANN_NIGHT_LOC, 0);
            }
            despawnCursedVillageNpcs();
        }
    }

    private void spawnCursedVillageNpcs() {
        for (Npc npc : this._cursedVillageNpcs) {
            npc.getSpawn().setRespawnState(true);
            npc.getSpawn().doSpawn(false);
        }
    }

    private void despawnCursedVillageNpcs() {
        for (Npc npc : this._cursedVillageNpcs) {
            npc.getSpawn().setRespawnState(false);
            npc.deleteMe();
        }
    }
}
