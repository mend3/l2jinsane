package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q033_MakeAPairOfDressShoes extends Quest {
    private static final String qn = "Q033_MakeAPairOfDressShoes";

    private static final int WOODLEY = 30838;

    private static final int IAN = 30164;

    private static final int LEIKAR = 31520;

    private static final int LEATHER = 1882;

    private static final int THREAD = 1868;

    private static final int ADENA = 57;

    public static final int DRESS_SHOES_BOX = 7113;

    public Q033_MakeAPairOfDressShoes() {
        super(33, "Make a Pair of Dress Shoes");
        addStartNpc(30838);
        addTalkId(30838, 30164, 31520);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q033_MakeAPairOfDressShoes");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30838-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31520-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30838-3.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30838-5.htm")) {
            if (st.getQuestItemsCount(1882) >= 200 && st.getQuestItemsCount(1868) >= 600 && st.getQuestItemsCount(57) >= 200000) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(57, 200000);
                st.takeItems(1882, 200);
                st.takeItems(1868, 600);
            } else {
                htmltext = "30838-4a.htm";
            }
        } else if (event.equalsIgnoreCase("30164-1.htm")) {
            if (st.getQuestItemsCount(57) >= 300000) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(57, 300000);
            } else {
                htmltext = "30164-1a.htm";
            }
        } else if (event.equalsIgnoreCase("30838-7.htm")) {
            st.giveItems(DRESS_SHOES_BOX, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q033_MakeAPairOfDressShoes");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 60) {
                    QuestState fwear = player.getQuestState("Q037_MakeFormalWear");
                    if (fwear != null && fwear.getInt("cond") == 7) {
                        htmltext = "30838-0.htm";
                        break;
                    }
                    htmltext = "30838-0a.htm";
                    break;
                }
                htmltext = "30838-0b.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30838:
                        if (cond == 1) {
                            htmltext = "30838-1.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30838-2.htm";
                            break;
                        }
                        if (cond == 3) {
                            if (st.getQuestItemsCount(1882) >= 200 && st.getQuestItemsCount(1868) >= 600 && st.getQuestItemsCount(57) >= 200000) {
                                htmltext = "30838-4.htm";
                                break;
                            }
                            htmltext = "30838-4a.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30838-5a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30838-6.htm";
                        break;
                    case 31520:
                        if (cond == 1) {
                            htmltext = "31520-0.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "31520-1a.htm";
                        break;
                    case 30164:
                        if (cond == 4) {
                            htmltext = "30164-0.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30164-2.htm";
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
