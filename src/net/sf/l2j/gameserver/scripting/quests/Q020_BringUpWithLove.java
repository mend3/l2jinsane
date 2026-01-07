package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q020_BringUpWithLove extends Quest {
    public static final String qn = "Q020_BringUpWithLove";

    private static final int JEWEL_OF_INNOCENCE = 7185;

    public Q020_BringUpWithLove() {
        super(20, "Bring Up With Love");
        setItemsIds(7185);
        addStartNpc(31537);
        addTalkId(31537);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q020_BringUpWithLove");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31537-09.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31537-12.htm")) {
            st.takeItems(7185, -1);
            st.rewardItems(57, 68500);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q020_BringUpWithLove");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 65) ? "31537-02.htm" : "31537-01.htm";
                break;
            case 1:
                if (st.getInt("cond") == 2) {
                    htmltext = "31537-11.htm";
                    break;
                }
                htmltext = "31537-10.htm";
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
