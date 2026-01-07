package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q376_ExplorationOfTheGiantsCave_Part1 extends Quest {
    private static final String qn = "Q376_ExplorationOfTheGiantsCave_Part1";

    private static final int SOBLING = 31147;

    private static final int CLIFF = 30182;

    private static final int PARCHMENT = 5944;

    private static final int DICTIONARY_BASIC = 5891;

    private static final int MYSTERIOUS_BOOK = 5890;

    private static final int DICTIONARY_INTERMEDIATE = 5892;

    private static final int[][] BOOKS = new int[][]{{5937, 5938, 5939, 5940, 5941}, {5932, 5933, 5934, 5935, 5936}, {5922, 5923, 5924, 5925, 5926}, {5927, 5928, 5929, 5930, 5931}};

    private static final int[][] RECIPES = new int[][]{{5346, 5354}, {5332, 5334}, {5416, 5418}, {5424, 5340}};

    public Q376_ExplorationOfTheGiantsCave_Part1() {
        super(376, "Exploration of the Giants' Cave, Part 1");
        setItemsIds(5891, 5890);
        addStartNpc(31147);
        addTalkId(31147, 30182);
        addKillId(20647, 20648, 20649, 20650);
    }

    private static String checkItems(QuestState st) {
        if (st.hasQuestItems(5890)) {
            int cond = st.getInt("cond");
            if (cond == 1) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                return "31147-07.htm";
            }
            return "31147-08.htm";
        }
        for (int type = 0; type < BOOKS.length; type++) {
            boolean complete = true;
            for (int book : BOOKS[type]) {
                if (!st.hasQuestItems(book))
                    complete = false;
            }
            if (complete) {
                for (int book : BOOKS[type])
                    st.takeItems(book, 1);
                st.giveItems(Rnd.get(RECIPES[type]), 1);
                return "31147-04.htm";
            }
        }
        return "31147-05.htm";
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q376_ExplorationOfTheGiantsCave_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31147-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("condBook", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5891, 1);
        } else if (event.equalsIgnoreCase("31147-04.htm")) {
            htmltext = checkItems(st);
        } else if (event.equalsIgnoreCase("31147-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30182-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(5890, -1);
            st.giveItems(5892, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q376_ExplorationOfTheGiantsCave_Part1");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 51) ? "31147-01.htm" : "31147-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31147:
                        htmltext = checkItems(st);
                        break;
                    case 30182:
                        if (cond == 2 && st.hasQuestItems(5890)) {
                            htmltext = "30182-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30182-03.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5944, 1, 0, 20000);
        st = getRandomPartyMember(player, npc, "condBook", "1");
        if (st == null)
            return null;
        if (st.dropItems(5890, 1, 1, 1000))
            st.unset("condBook");
        return null;
    }
}
