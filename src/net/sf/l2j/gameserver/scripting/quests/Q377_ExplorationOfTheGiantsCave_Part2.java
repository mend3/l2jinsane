package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q377_ExplorationOfTheGiantsCave_Part2 extends Quest {
    private static final String qn = "Q377_ExplorationOfTheGiantsCave_Part2";

    private static final int ANCIENT_BOOK = 5955;

    private static final int DICTIONARY_INTERMEDIATE = 5892;

    private static final int[][] BOOKS = new int[][]{{5945, 5946, 5947, 5948, 5949}, {5950, 5951, 5952, 5953, 5954}};

    private static final int[][] RECIPES = new int[][]{{5338, 5336}, {5420, 5422}};

    public Q377_ExplorationOfTheGiantsCave_Part2() {
        super(377, "Exploration of the Giants' Cave, Part 2");
        addStartNpc(31147);
        addTalkId(31147);
        addKillId(20654, 20656, 20657, 20658);
    }

    private static String checkItems(QuestState st) {
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
        QuestState st = player.getQuestState("Q377_ExplorationOfTheGiantsCave_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31147-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31147-04.htm")) {
            htmltext = checkItems(st);
        } else if (event.equalsIgnoreCase("31147-07.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q377_ExplorationOfTheGiantsCave_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 57 || !st.hasQuestItems(5892)) ? "31147-01.htm" : "31147-02.htm";
                break;
            case 1:
                htmltext = checkItems(st);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5955, 1, 0, 18000);
        return null;
    }
}
