package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.enums.BossStatus;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.BossSpawn;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q610_MagicalPowerOfWater_Part2 extends Quest {
    private static final String qn = "Q610_MagicalPowerOfWater_Part2";

    private static final int SOUL_OF_WATER_ASHUTAR = 25316;

    private static final int ASEFA = 31372;

    private static final int VARKAS_HOLY_ALTAR = 31560;

    private static final int GREEN_TOTEM = 7238;

    private static final int ICE_HEART_OF_ASHUTAR = 7239;

    private static final int CHECK_INTERVAL = 600000;

    private static final int IDLE_INTERVAL = 2;

    private Npc _npc = null;

    private int _status = -1;

    public Q610_MagicalPowerOfWater_Part2() {
        super(610, "Magical Power of Water - Part 2");
        setItemsIds(7239);
        addStartNpc(31372);
        addTalkId(31372, 31560);
        addAttackId(25316);
        addKillId(25316);
        switch (RaidBossManager.getInstance().getStatus(25316)) {
            case ALIVE:
                spawnNpc();
            case DEAD:
                startQuestTimer("check", 600000L, null, null, true);
                break;
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equals("check")) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25316);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                if (this._status >= 0 && this._status-- == 0)
                    despawnRaid(raid);
                spawnNpc();
            }
            return null;
        }
        String htmltext = event;
        QuestState st = player.getQuestState("Q610_MagicalPowerOfWater_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31372-04.htm")) {
            if (st.hasQuestItems(7238)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "31372-02.htm";
            }
        } else if (event.equalsIgnoreCase("31372-07.htm")) {
            if (st.hasQuestItems(7239)) {
                st.takeItems(7239, 1);
                st.rewardExpAndSp(10000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31372-08.htm";
            }
        } else if (event.equalsIgnoreCase("31560-02.htm")) {
            if (st.hasQuestItems(7238)) {
                if (this._status < 0) {
                    if (spawnRaid()) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(7238, 1);
                    }
                } else {
                    htmltext = "31560-04.htm";
                }
            } else {
                htmltext = "31560-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q610_MagicalPowerOfWater_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (!st.hasQuestItems(7238)) {
                    htmltext = "31372-02.htm";
                    break;
                }
                if (player.getLevel() < 75 && player.getAllianceWithVarkaKetra() < 2) {
                    htmltext = "31372-03.htm";
                    break;
                }
                htmltext = "31372-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31372:
                        htmltext = (cond < 3) ? "31372-05.htm" : "31372-06.htm";
                        break;
                    case 31560:
                        if (cond == 1) {
                            htmltext = "31560-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31560-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        if (player != null)
            this._status = 2;
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "2")) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(7239, 1);
            }
        despawnRaid(npc);
        if (this._npc != null) {
            this._npc.deleteMe();
            this._npc = null;
        }
        return null;
    }

    private void spawnNpc() {
        if (this._npc == null)
            this._npc = addSpawn(31560, 105452, -36775, -1050, 34000, false, 0L, false);
    }

    private boolean spawnRaid() {
        BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25316);
        if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
            Npc raid = bs.getBoss();
            raid.getSpawn().setLoc(104771, -36993, -1149, Rnd.get(65536));
            raid.teleportTo(104771, -36993, -1149, 100);
            raid.broadcastNpcSay("The water charm then is the storm and the tsunami strength! Opposes with it only has the blind alley!");
            this._status = 2;
            return true;
        }
        return false;
    }

    private void despawnRaid(Npc raid) {
        raid.getSpawn().setLoc(-105900, -252700, -15542, 0);
        if (!raid.isDead())
            raid.teleportTo(-105900, -252700, -15542, 0);
        this._status = -1;
    }
}
