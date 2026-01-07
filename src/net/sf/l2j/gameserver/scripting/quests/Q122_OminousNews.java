package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q122_OminousNews extends Quest {
    private static final String qn = "Q122_OminousNews";

    private static final int MOIRA = 31979;

    private static final int KARUDA = 32017;

    public Q122_OminousNews() {
        super(122, "Ominous News");
        addStartNpc(31979);
        addTalkId(31979, 32017);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q122_OminousNews");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31979-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32017-02.htm")) {
            st.rewardItems(57, 1695);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q122_OminousNews");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "31979-01.htm" : "31979-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31979:
                        htmltext = "31979-03.htm";
                        break;
                    case 32017:
                        htmltext = "32017-01.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
