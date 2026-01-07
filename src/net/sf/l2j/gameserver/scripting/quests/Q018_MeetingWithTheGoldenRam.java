package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q018_MeetingWithTheGoldenRam extends Quest {
    private static final String qn = "Q018_MeetingWithTheGoldenRam";

    private static final int SUPPLY_BOX = 7245;

    private static final int DONAL = 31314;

    private static final int DAISY = 31315;

    private static final int ABERCROMBIE = 31555;

    public Q018_MeetingWithTheGoldenRam() {
        super(18, "Meeting with the Golden Ram");
        setItemsIds(7245);
        addStartNpc(31314);
        addTalkId(31314, 31315, 31555);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q018_MeetingWithTheGoldenRam");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31314-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31315-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7245, 1);
        } else if (event.equalsIgnoreCase("31555-02.htm")) {
            st.takeItems(7245, 1);
            st.rewardItems(57, 15000);
            st.rewardExpAndSp(50000L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q018_MeetingWithTheGoldenRam");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 66) ? "31314-02.htm" : "31314-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31314:
                        htmltext = "31314-04.htm";
                        break;
                    case 31315:
                        if (cond == 1) {
                            htmltext = "31315-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31315-03.htm";
                        break;
                    case 31555:
                        if (cond == 2)
                            htmltext = "31555-01.htm";
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
