package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q168_DeliverSupplies extends Quest {
    private static final String qn = "Q168_DeliverSupplies";

    private static final int JENNA_LETTER = 1153;

    private static final int SENTRY_BLADE_1 = 1154;

    private static final int SENTRY_BLADE_2 = 1155;

    private static final int SENTRY_BLADE_3 = 1156;

    private static final int OLD_BRONZE_SWORD = 1157;

    private static final int JENNA = 30349;

    private static final int ROSELYN = 30355;

    private static final int KRISTIN = 30357;

    private static final int HARANT = 30360;

    public Q168_DeliverSupplies() {
        super(168, "Deliver Supplies");
        setItemsIds(1153, 1154, 1155, 1156, 1157);
        addStartNpc(30349);
        addTalkId(30349, 30355, 30357, 30360);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q168_DeliverSupplies");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30349-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1153, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q168_DeliverSupplies");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30349-00.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "30349-01.htm";
                    break;
                }
                htmltext = "30349-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30349:
                        if (cond == 1) {
                            htmltext = "30349-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30349-05.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1154, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30349-07.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30349-06.htm";
                            st.takeItems(1157, 2);
                            st.rewardItems(57, 820);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30360:
                        if (cond == 1) {
                            htmltext = "30360-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1153, 1);
                            st.giveItems(1154, 1);
                            st.giveItems(1155, 1);
                            st.giveItems(1156, 1);
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30360-02.htm";
                        break;
                    case 30355:
                        if (cond == 3) {
                            if (st.hasQuestItems(1155)) {
                                htmltext = "30355-01.htm";
                                st.takeItems(1155, 1);
                                st.giveItems(1157, 1);
                                if (st.getQuestItemsCount(1157) == 2) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                }
                                break;
                            }
                            htmltext = "30355-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30355-02.htm";
                        break;
                    case 30357:
                        if (cond == 3) {
                            if (st.hasQuestItems(1156)) {
                                htmltext = "30357-01.htm";
                                st.takeItems(1156, 1);
                                st.giveItems(1157, 1);
                                if (st.getQuestItemsCount(1157) == 2) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                }
                                break;
                            }
                            htmltext = "30357-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30357-02.htm";
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
