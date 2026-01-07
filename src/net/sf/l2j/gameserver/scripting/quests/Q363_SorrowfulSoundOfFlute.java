package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q363_SorrowfulSoundOfFlute extends Quest {
    private static final String qn = "Q363_SorrowfulSoundOfFlute";

    private static final int NANARIN = 30956;

    private static final int OPIX = 30595;

    private static final int ALDO = 30057;

    private static final int RANSPO = 30594;

    private static final int HOLVAS = 30058;

    private static final int BARBADO = 30959;

    private static final int POITAN = 30458;

    private static final int NANARIN_FLUTE = 4319;

    private static final int BLACK_BEER = 4320;

    private static final int CLOTHES = 4318;

    private static final int THEME_OF_SOLITUDE = 4420;

    public Q363_SorrowfulSoundOfFlute() {
        super(363, "Sorrowful Sound of Flute");
        setItemsIds(4319, 4320, 4318);
        addStartNpc(30956);
        addTalkId(30956, 30595, 30057, 30594, 30058, 30959, 30458);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q363_SorrowfulSoundOfFlute");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30956-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30956-05.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4318, 1);
        } else if (event.equalsIgnoreCase("30956-06.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4319, 1);
        } else if (event.equalsIgnoreCase("30956-07.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4320, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q363_SorrowfulSoundOfFlute");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30956-03.htm" : "30956-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30956:
                        if (cond == 1) {
                            htmltext = "30956-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30956-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30956-08.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (st.getInt("success") == 1) {
                                htmltext = "30956-09.htm";
                                st.giveItems(4420, 1);
                                st.playSound("ItemSound.quest_finish");
                            } else {
                                htmltext = "30956-10.htm";
                                st.playSound("ItemSound.quest_giveup");
                            }
                            st.exitQuest(true);
                        }
                        break;
                    case 30057:
                    case 30058:
                    case 30458:
                    case 30594:
                    case 30595:
                        htmltext = npc.getNpcId() + "-01.htm";
                        if (cond == 1) {
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                        }
                        break;
                    case 30959:
                        if (cond == 3) {
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            if (st.hasQuestItems(4319)) {
                                htmltext = "30959-02.htm";
                                st.set("success", "1");
                            } else {
                                htmltext = "30959-01.htm";
                            }
                            st.takeItems(4320, -1);
                            st.takeItems(4318, -1);
                            st.takeItems(4319, -1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30959-03.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
