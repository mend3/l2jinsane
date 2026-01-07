package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q019_GoToThePastureland extends Quest {
    private static final String qn = "Q019_GoToThePastureland";

    private static final int YOUNG_WILD_BEAST_MEAT = 7547;

    private static final int VLADIMIR = 31302;

    private static final int TUNATUN = 31537;

    public Q019_GoToThePastureland() {
        super(19, "Go to the Pastureland!");
        setItemsIds(7547);
        addStartNpc(31302);
        addTalkId(31302, 31537);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q019_GoToThePastureland");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31302-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7547, 1);
        } else if (event.equalsIgnoreCase("019_finish")) {
            if (st.hasQuestItems(7547)) {
                htmltext = "31537-01.htm";
                st.takeItems(7547, 1);
                st.rewardItems(57, 30000);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "31537-02.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q019_GoToThePastureland");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 63) ? "31302-03.htm" : "31302-00.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31302:
                        htmltext = "31302-02.htm";
                        break;
                    case 31537:
                        htmltext = "31537-00.htm";
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
