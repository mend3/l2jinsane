package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q029_ChestCaughtWithABaitOfEarth extends Quest {
    private static final String qn = "Q029_ChestCaughtWithABaitOfEarth";

    private static final int WILLIE = 31574;

    private static final int ANABEL = 30909;

    private static final int SMALL_PURPLE_TREASURE_CHEST = 6507;

    private static final int SMALL_GLASS_BOX = 7627;

    private static final int PLATED_LEATHER_GLOVES = 2455;

    public Q029_ChestCaughtWithABaitOfEarth() {
        super(29, "Chest caught with a bait of earth");
        setItemsIds(7627);
        addStartNpc(31574);
        addTalkId(31574, 30909);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q029_ChestCaughtWithABaitOfEarth");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31574-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31574-07.htm")) {
            if (st.hasQuestItems(6507)) {
                st.set("cond", "2");
                st.takeItems(6507, 1);
                st.giveItems(7627, 1);
            } else {
                htmltext = "31574-08.htm";
            }
        } else if (event.equalsIgnoreCase("30909-02.htm")) {
            if (st.hasQuestItems(7627)) {
                htmltext = "30909-02.htm";
                st.takeItems(7627, 1);
                st.giveItems(2455, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "30909-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        QuestState st = player.getQuestState("Q029_ChestCaughtWithABaitOfEarth");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 48) {
                    htmltext = "31574-02.htm";
                    break;
                }
                st2 = player.getQuestState("Q052_WilliesSpecialBait");
                if (st2 != null && st2.isCompleted()) {
                    htmltext = "31574-01.htm";
                    break;
                }
                htmltext = "31574-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31574:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(6507) ? "31574-06.htm" : "31574-05.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31574-09.htm";
                        break;
                    case 30909:
                        if (cond == 2)
                            htmltext = "30909-01.htm";
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
