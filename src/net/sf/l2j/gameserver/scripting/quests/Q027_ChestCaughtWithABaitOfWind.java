package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q027_ChestCaughtWithABaitOfWind extends Quest {
    private static final String qn = "Q027_ChestCaughtWithABaitOfWind";

    private static final int LANOSCO = 31570;

    private static final int SHALING = 31434;

    private static final int LARGE_BLUE_TREASURE_CHEST = 6500;

    private static final int STRANGE_BLUEPRINT = 7625;

    private static final int BLACK_PEARL_RING = 880;

    public Q027_ChestCaughtWithABaitOfWind() {
        super(27, "Chest caught with a bait of wind");
        setItemsIds(7625);
        addStartNpc(31570);
        addTalkId(31570, 31434);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q027_ChestCaughtWithABaitOfWind");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31570-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31570-07.htm")) {
            if (st.hasQuestItems(6500)) {
                st.set("cond", "2");
                st.takeItems(6500, 1);
                st.giveItems(7625, 1);
            } else {
                htmltext = "31570-08.htm";
            }
        } else if (event.equalsIgnoreCase("31434-02.htm")) {
            if (st.hasQuestItems(7625)) {
                htmltext = "31434-02.htm";
                st.takeItems(7625, 1);
                st.giveItems(880, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "31434-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        QuestState st = player.getQuestState("Q027_ChestCaughtWithABaitOfWind");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 27) {
                    htmltext = "31570-02.htm";
                    break;
                }
                st2 = player.getQuestState("Q050_LanoscosSpecialBait");
                if (st2 != null && st2.isCompleted()) {
                    htmltext = "31570-01.htm";
                    break;
                }
                htmltext = "31570-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31570:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(6500) ? "31570-06.htm" : "31570-05.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31570-09.htm";
                        break;
                    case 31434:
                        if (cond == 2)
                            htmltext = "31434-01.htm";
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
