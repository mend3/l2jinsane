package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q017_LightAndDarkness extends Quest {
    private static final String qn = "Q017_LightAndDarkness";

    private static final int BLOOD_OF_SAINT = 7168;

    private static final int HIERARCH = 31517;

    private static final int SAINT_ALTAR_1 = 31508;

    private static final int SAINT_ALTAR_2 = 31509;

    private static final int SAINT_ALTAR_3 = 31510;

    private static final int SAINT_ALTAR_4 = 31511;

    public Q017_LightAndDarkness() {
        super(17, "Light and Darkness");
        setItemsIds(7168);
        addStartNpc(31517);
        addTalkId(31517, 31508, 31509, 31510, 31511);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q017_LightAndDarkness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31517-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7168, 4);
        } else if (event.equalsIgnoreCase("31508-02.htm")) {
            if (st.hasQuestItems(7168)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7168, 1);
            } else {
                htmltext = "31508-03.htm";
            }
        } else if (event.equalsIgnoreCase("31509-02.htm")) {
            if (st.hasQuestItems(7168)) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7168, 1);
            } else {
                htmltext = "31509-03.htm";
            }
        } else if (event.equalsIgnoreCase("31510-02.htm")) {
            if (st.hasQuestItems(7168)) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7168, 1);
            } else {
                htmltext = "31510-03.htm";
            }
        } else if (event.equalsIgnoreCase("31511-02.htm")) {
            if (st.hasQuestItems(7168)) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7168, 1);
            } else {
                htmltext = "31511-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q017_LightAndDarkness");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 61) ? "31517-03.htm" : "31517-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31517:
                        if (cond == 5) {
                            htmltext = "31517-07.htm";
                            st.rewardExpAndSp(105527L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                            break;
                        }
                        if (st.hasQuestItems(7168)) {
                            htmltext = "31517-05.htm";
                            break;
                        }
                        htmltext = "31517-06.htm";
                        st.exitQuest(true);
                        break;
                    case 31508:
                        if (cond == 1) {
                            htmltext = "31508-01.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "31508-04.htm";
                        break;
                    case 31509:
                        if (cond == 2) {
                            htmltext = "31509-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "31509-04.htm";
                        break;
                    case 31510:
                        if (cond == 3) {
                            htmltext = "31510-01.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31510-04.htm";
                        break;
                    case 31511:
                        if (cond == 4) {
                            htmltext = "31511-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "31511-04.htm";
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
