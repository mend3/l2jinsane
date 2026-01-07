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

public class Q604_DaimonTheWhiteEyed_Part2 extends Quest {
    private static final String qn = "Q604_DaimonTheWhiteEyed_Part2";

    private static final int DAIMON_THE_WHITE_EYED = 25290;

    private static final int EYE_OF_ARGOS = 31683;

    private static final int DAIMON_ALTAR = 31541;

    private static final int UNFINISHED_SUMMON_CRYSTAL = 7192;

    private static final int SUMMON_CRYSTAL = 7193;

    private static final int ESSENCE_OF_DAIMON = 7194;

    private static final int[] REWARDS = new int[]{4595, 4596, 4597, 4598, 4599, 4600};

    private static final int CHECK_INTERVAL = 600000;

    private static final int IDLE_INTERVAL = 3;

    private Npc _npc = null;

    private int _status = -1;

    public Q604_DaimonTheWhiteEyed_Part2() {
        super(604, "Daimon The White-Eyed - Part 2");
        setItemsIds(7193, 7194);
        addStartNpc(31683);
        addTalkId(31683, 31541);
        addAttackId(25290);
        addKillId(25290);
        switch (RaidBossManager.getInstance().getStatus(25290)) {
            case ALIVE:
                spawnNpc();
            case DEAD:
                startQuestTimer("check", 600000L, null, null, true);
                break;
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equals("check")) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25290);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                if (this._status >= 0 && this._status-- == 0)
                    despawnRaid(raid);
                spawnNpc();
            }
            return null;
        }
        String htmltext = event;
        QuestState st = player.getQuestState("Q604_DaimonTheWhiteEyed_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31683-03.htm")) {
            if (st.hasQuestItems(7192)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(7192, 1);
                st.giveItems(7193, 1);
            } else {
                htmltext = "31683-04.htm";
            }
        } else if (event.equalsIgnoreCase("31683-08.htm")) {
            if (st.hasQuestItems(7194)) {
                st.takeItems(7194, 1);
                st.rewardItems(Rnd.get(REWARDS), 5);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31683-09.htm";
            }
        } else if (event.equalsIgnoreCase("31541-02.htm")) {
            if (st.hasQuestItems(7193)) {
                if (this._status < 0) {
                    if (spawnRaid()) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(7193, 1);
                    }
                } else {
                    htmltext = "31541-04.htm";
                }
            } else {
                htmltext = "31541-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q604_DaimonTheWhiteEyed_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 73) {
                    htmltext = "31683-02.htm";
                    st.exitQuest(true);
                    break;
                }
                htmltext = "31683-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31683:
                        if (cond == 1) {
                            htmltext = "31683-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31683-06.htm";
                            break;
                        }
                        htmltext = "31683-07.htm";
                        break;
                    case 31541:
                        if (cond == 1) {
                            htmltext = "31541-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31541-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        if (player != null)
            this._status = 3;
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "2")) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(7194, 1);
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
            this._npc = addSpawn(31541, 186304, -43744, -3193, 57000, false, 0L, false);
    }

    private boolean spawnRaid() {
        BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25290);
        if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
            Npc raid = bs.getBoss();
            raid.getSpawn().setLoc(185900, -44000, -3160, Rnd.get(65536));
            raid.teleportTo(185900, -44000, -3160, 100);
            raid.broadcastNpcSay("Who called me?");
            this._status = 3;
            return true;
        }
        return false;
    }

    private void despawnRaid(Npc raid) {
        raid.getSpawn().setLoc(-106500, -252700, -15542, 0);
        if (!raid.isDead())
            raid.teleportTo(-106500, -252700, -15542, 0);
        this._status = -1;
    }
}
