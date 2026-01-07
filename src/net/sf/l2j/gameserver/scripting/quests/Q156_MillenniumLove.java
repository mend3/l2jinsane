package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q156_MillenniumLove extends Quest {
    private static final String qn = "Q156_MillenniumLove";

    private static final int LILITH_LETTER = 1022;

    private static final int THEON_DIARY = 1023;

    private static final int LILITH = 30368;

    private static final int BAENEDES = 30369;

    public Q156_MillenniumLove() {
        super(156, "Millennium Love");
        setItemsIds(1022, 1023);
        addStartNpc(30368);
        addTalkId(30368, 30369);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q156_MillenniumLove");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30368-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1022, 1);
        } else if (event.equalsIgnoreCase("30369-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1022, 1);
            st.giveItems(1023, 1);
        } else if (event.equalsIgnoreCase("30369-03.htm")) {
            st.takeItems(1022, 1);
            st.rewardExpAndSp(3000L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q156_MillenniumLove");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30368-00.htm" : "30368-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30368:
                        if (st.hasQuestItems(1022)) {
                            htmltext = "30368-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1023)) {
                            htmltext = "30368-06.htm";
                            st.takeItems(1023, 1);
                            st.giveItems(5250, 1);
                            st.rewardExpAndSp(3000L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30369:
                        if (st.hasQuestItems(1022)) {
                            htmltext = "30369-01.htm";
                            break;
                        }
                        if (st.hasQuestItems(1023))
                            htmltext = "30369-04.htm";
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
