package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q124_MeetingTheElroki extends Quest {
    public static final String qn = "Q124_MeetingTheElroki";

    private static final int MARQUEZ = 32113;

    private static final int MUSHIKA = 32114;

    private static final int ASAMAH = 32115;

    private static final int KARAKAWEI = 32117;

    private static final int MANTARASA = 32118;

    public Q124_MeetingTheElroki() {
        super(124, "Meeting the Elroki");
        addStartNpc(32113);
        addTalkId(32113, 32114, 32115, 32117, 32118);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q124_MeetingTheElroki");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32113-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32113-04.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32114-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32115-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32117-02.htm")) {
            if (st.getInt("cond") == 4)
                st.set("progress", "1");
        } else if (event.equalsIgnoreCase("32117-03.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32118-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8778, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q124_MeetingTheElroki");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "32113-01a.htm" : "32113-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 32113:
                        if (cond == 1) {
                            htmltext = "32113-03.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "32113-04a.htm";
                        break;
                    case 32114:
                        if (cond == 2) {
                            htmltext = "32114-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "32114-03.htm";
                        break;
                    case 32115:
                        if (cond == 3) {
                            htmltext = "32115-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "32115-05.htm";
                            st.takeItems(8778, -1);
                            st.rewardItems(57, 71318);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 32117:
                        if (cond == 4) {
                            htmltext = "32117-01.htm";
                            if (st.getInt("progress") == 1)
                                htmltext = "32117-02.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "32117-04.htm";
                        break;
                    case 32118:
                        if (cond == 5) {
                            htmltext = "32118-01.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "32118-03.htm";
                        break;
                }
                break;
            case 2:
                if (npc.getNpcId() == 32115) {
                    htmltext = "32115-06.htm";
                    break;
                }
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
