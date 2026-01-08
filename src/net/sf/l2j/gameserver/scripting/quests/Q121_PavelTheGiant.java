package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q121_PavelTheGiant extends Quest {
    private static final String qn = "Q121_PavelTheGiant";

    private static final int NEWYEAR = 31961;

    private static final int YUMI = 32041;

    public Q121_PavelTheGiant() {
        super(121, "Pavel the Giant");
        addStartNpc(31961);
        addTalkId(31961, 32041);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q121_PavelTheGiant");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31961-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32041-2.htm")) {
            st.rewardExpAndSp(10000L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q121_PavelTheGiant");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 46) ? "31961-1a.htm" : "31961-1.htm";
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 31961 -> "31961-2a.htm";
                    case 32041 -> "32041-1.htm";
                    default -> htmltext;
                };
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
