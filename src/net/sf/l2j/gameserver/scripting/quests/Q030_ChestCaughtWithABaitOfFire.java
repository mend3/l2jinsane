package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q030_ChestCaughtWithABaitOfFire extends Quest {
    private static final String qn = "Q030_ChestCaughtWithABaitOfFire";

    private static final int LINNAEUS = 31577;

    private static final int RUKAL = 30629;

    private static final int RED_TREASURE_BOX = 6511;

    private static final int MUSICAL_SCORE = 7628;

    private static final int NECKLACE_OF_PROTECTION = 916;

    public Q030_ChestCaughtWithABaitOfFire() {
        super(30, "Chest caught with a bait of fire");
        setItemsIds(7628);
        addStartNpc(31577);
        addTalkId(31577, 30629);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q030_ChestCaughtWithABaitOfFire");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31577-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31577-07.htm")) {
            if (st.hasQuestItems(6511)) {
                st.set("cond", "2");
                st.takeItems(6511, 1);
                st.giveItems(7628, 1);
            } else {
                htmltext = "31577-08.htm";
            }
        } else if (event.equalsIgnoreCase("30629-02.htm")) {
            if (st.hasQuestItems(7628)) {
                htmltext = "30629-02.htm";
                st.takeItems(7628, 1);
                st.giveItems(916, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "30629-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        QuestState st = player.getQuestState("Q030_ChestCaughtWithABaitOfFire");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 60) {
                    htmltext = "31577-02.htm";
                    break;
                }
                st2 = player.getQuestState("Q053_LinnaeusSpecialBait");
                if (st2 != null && st2.isCompleted()) {
                    htmltext = "31577-01.htm";
                    break;
                }
                htmltext = "31577-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31577:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(6511) ? "31577-06.htm" : "31577-05.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31577-09.htm";
                        break;
                    case 30629:
                        if (cond == 2)
                            htmltext = "30629-01.htm";
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
