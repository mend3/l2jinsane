package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q028_ChestCaughtWithABaitOfIcyAir extends Quest {
    private static final String qn = "Q028_ChestCaughtWithABaitOfIcyAir";

    private static final int OFULLE = 31572;

    private static final int KIKI = 31442;

    private static final int BIG_YELLOW_TREASURE_CHEST = 6503;

    private static final int KIKI_LETTER = 7626;

    private static final int ELVEN_RING = 881;

    public Q028_ChestCaughtWithABaitOfIcyAir() {
        super(28, "Chest caught with a bait of icy air");
        setItemsIds(7626);
        addStartNpc(31572);
        addTalkId(31572, 31442);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q028_ChestCaughtWithABaitOfIcyAir");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31572-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31572-07.htm")) {
            if (st.hasQuestItems(6503)) {
                st.set("cond", "2");
                st.takeItems(6503, 1);
                st.giveItems(7626, 1);
            } else {
                htmltext = "31572-08.htm";
            }
        } else if (event.equalsIgnoreCase("31442-02.htm")) {
            if (st.hasQuestItems(7626)) {
                htmltext = "31442-02.htm";
                st.takeItems(7626, 1);
                st.giveItems(881, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "31442-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        QuestState st = player.getQuestState("Q028_ChestCaughtWithABaitOfIcyAir");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 36) {
                    htmltext = "31572-02.htm";
                    break;
                }
                st2 = player.getQuestState("Q051_OFullesSpecialBait");
                if (st2 != null && st2.isCompleted()) {
                    htmltext = "31572-01.htm";
                    break;
                }
                htmltext = "31572-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31572:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(6503) ? "31572-06.htm" : "31572-05.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31572-09.htm";
                        break;
                    case 31442:
                        if (cond == 2)
                            htmltext = "31442-01.htm";
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
