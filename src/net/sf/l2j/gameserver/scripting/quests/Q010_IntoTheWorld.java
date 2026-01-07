package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q010_IntoTheWorld extends Quest {
    private static final String qn = "Q010_IntoTheWorld";

    private static final int VERY_EXPENSIVE_NECKLACE = 7574;

    private static final int SOE_GIRAN = 7559;

    private static final int MARK_OF_TRAVELER = 7570;

    private static final int REED = 30520;

    private static final int BALANKI = 30533;

    private static final int GERALD = 30650;

    public Q010_IntoTheWorld() {
        super(10, "Into the World");
        setItemsIds(7574);
        addStartNpc(30533);
        addTalkId(30533, 30520, 30650);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q010_IntoTheWorld");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30533-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30520-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7574, 1);
        } else if (event.equalsIgnoreCase("30650-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7574, 1);
        } else if (event.equalsIgnoreCase("30520-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30533-05.htm")) {
            st.giveItems(7559, 1);
            st.rewardItems(7570, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q010_IntoTheWorld");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 3 && player.getRace() == ClassRace.DWARF) {
                    htmltext = "30533-01.htm";
                    break;
                }
                htmltext = "30533-01a.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30533:
                        if (cond < 4) {
                            htmltext = "30533-03.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30533-04.htm";
                        break;
                    case 30520:
                        if (cond == 1) {
                            htmltext = "30520-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30520-02a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30520-03.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30520-04a.htm";
                        break;
                    case 30650:
                        if (cond == 2) {
                            htmltext = "30650-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30650-04.htm";
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
