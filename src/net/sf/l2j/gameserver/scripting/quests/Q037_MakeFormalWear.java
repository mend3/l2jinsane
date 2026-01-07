package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q037_MakeFormalWear extends Quest {
    private static final String qn = "Q037_MakeFormalWear";

    private static final int ALEXIS = 30842;

    private static final int LEIKAR = 31520;

    private static final int JEREMY = 31521;

    private static final int MIST = 31627;

    private static final int MYSTERIOUS_CLOTH = 7076;

    private static final int JEWEL_BOX = 7077;

    private static final int SEWING_KIT = 7078;

    private static final int DRESS_SHOES_BOX = 7113;

    private static final int SIGNET_RING = 7164;

    private static final int ICE_WINE = 7160;

    private static final int BOX_OF_COOKIES = 7159;

    private static final int FORMAL_WEAR = 6408;

    public Q037_MakeFormalWear() {
        super(37, "Make Formal Wear");
        setItemsIds(7164, 7160, 7159);
        addStartNpc(30842);
        addTalkId(30842, 31520, 31521, 31627);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q037_MakeFormalWear");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30842-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31520-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7164, 1);
        } else if (event.equalsIgnoreCase("31521-1.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7164, 1);
            st.giveItems(7160, 1);
        } else if (event.equalsIgnoreCase("31627-1.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7160, 1);
        } else if (event.equalsIgnoreCase("31521-3.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7159, 1);
        } else if (event.equalsIgnoreCase("31520-3.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7159, 1);
        } else if (event.equalsIgnoreCase("31520-5.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7077, 1);
            st.takeItems(7076, 1);
            st.takeItems(7078, 1);
        } else if (event.equalsIgnoreCase("31520-7.htm")) {
            st.takeItems(7113, 1);
            st.giveItems(6408, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q037_MakeFormalWear");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "30842-0a.htm" : "30842-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30842:
                        if (cond == 1)
                            htmltext = "30842-2.htm";
                        break;
                    case 31520:
                        if (cond == 1) {
                            htmltext = "31520-0.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31520-1a.htm";
                            break;
                        }
                        if (cond == 5 || cond == 6) {
                            if (st.hasQuestItems(7076, 7077, 7078)) {
                                htmltext = "31520-4.htm";
                                break;
                            }
                            if (st.hasQuestItems(7159)) {
                                htmltext = "31520-2.htm";
                                break;
                            }
                            htmltext = "31520-3a.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = st.hasQuestItems(7113) ? "31520-6.htm" : "31520-5a.htm";
                        break;
                    case 31521:
                        if (st.hasQuestItems(7164)) {
                            htmltext = "31521-0.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "31521-1a.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31521-2.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "31521-3a.htm";
                        break;
                    case 31627:
                        if (cond == 3) {
                            htmltext = "31627-0.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31627-2.htm";
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
