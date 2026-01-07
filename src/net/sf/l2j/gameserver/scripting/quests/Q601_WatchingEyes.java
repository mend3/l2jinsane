package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q601_WatchingEyes extends Quest {
    private static final String qn = "Q601_WatchingEyes";

    private static final int PROOF_OF_AVENGER = 7188;

    private static final int[][] REWARDS = new int[][]{{6699, 90000, 20}, {6698, 80000, 40}, {6700, 40000, 50}, {0, 230000, 100}};

    public Q601_WatchingEyes() {
        super(601, "Watching Eyes");
        setItemsIds(7188);
        addStartNpc(31683);
        addTalkId(31683);
        addKillId(21306, 21308, 21309, 21310, 21311);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q601_WatchingEyes");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31683-03.htm")) {
            if (player.getLevel() < 71) {
                htmltext = "31683-02.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            }
        } else if (event.equalsIgnoreCase("31683-07.htm")) {
            st.takeItems(7188, -1);
            int random = Rnd.get(100);
            for (int[] element : REWARDS) {
                if (random < element[2]) {
                    st.rewardItems(57, element[1]);
                    if (element[0] != 0) {
                        st.giveItems(element[0], 5);
                        st.rewardExpAndSp(120000L, 10000);
                    }
                    break;
                }
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q601_WatchingEyes");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "31683-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = st.hasQuestItems(7188) ? "31683-05.htm" : "31683-04.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "31683-06.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(7188, 1, 100, 500000))
            st.set("cond", "2");
        return null;
    }
}
