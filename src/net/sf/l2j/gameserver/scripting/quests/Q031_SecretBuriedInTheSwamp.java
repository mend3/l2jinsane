package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q031_SecretBuriedInTheSwamp extends Quest {
    private static final String qn = "Q031_SecretBuriedInTheSwamp";

    private static final int KRORIN_JOURNAL = 7252;

    private static final int ABERCROMBIE = 31555;

    private static final int FORGOTTEN_MONUMENT_1 = 31661;

    private static final int FORGOTTEN_MONUMENT_2 = 31662;

    private static final int FORGOTTEN_MONUMENT_3 = 31663;

    private static final int FORGOTTEN_MONUMENT_4 = 31664;

    private static final int CORPSE_OF_DWARF = 31665;

    public Q031_SecretBuriedInTheSwamp() {
        super(31, "Secret Buried in the Swamp");
        setItemsIds(7252);
        addStartNpc(31555);
        addTalkId(31555, 31665, 31661, 31662, 31663, 31664);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q031_SecretBuriedInTheSwamp");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31555-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31665-01.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7252, 1);
        } else if (event.equalsIgnoreCase("31555-04.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31661-01.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31662-01.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31663-01.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31664-01.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31555-07.htm")) {
            st.takeItems(7252, 1);
            st.rewardItems(57, 40000);
            st.rewardExpAndSp(130000L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q031_SecretBuriedInTheSwamp");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 66) ? "31555-00a.htm" : "31555-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31555:
                        if (cond == 1) {
                            htmltext = "31555-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31555-03.htm";
                            break;
                        }
                        if (cond > 2 && cond < 7) {
                            htmltext = "31555-05.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "31555-06.htm";
                        break;
                    case 31665:
                        if (cond == 1) {
                            htmltext = "31665-00.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "31665-02.htm";
                        break;
                    case 31661:
                        if (cond == 3) {
                            htmltext = "31661-00.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31661-02.htm";
                        break;
                    case 31662:
                        if (cond == 4) {
                            htmltext = "31662-00.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "31662-02.htm";
                        break;
                    case 31663:
                        if (cond == 5) {
                            htmltext = "31663-00.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "31663-02.htm";
                        break;
                    case 31664:
                        if (cond == 6) {
                            htmltext = "31664-00.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "31664-02.htm";
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
