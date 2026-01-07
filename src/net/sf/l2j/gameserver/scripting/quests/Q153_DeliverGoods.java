package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q153_DeliverGoods extends Quest {
    private static final String qn = "Q153_DeliverGoods";

    private static final int JACKSON = 30002;

    private static final int SILVIA = 30003;

    private static final int ARNOLD = 30041;

    private static final int RANT = 30054;

    private static final int DELIVERY_LIST = 1012;

    private static final int HEAVY_WOOD_BOX = 1013;

    private static final int CLOTH_BUNDLE = 1014;

    private static final int CLAY_POT = 1015;

    private static final int JACKSON_RECEIPT = 1016;

    private static final int SILVIA_RECEIPT = 1017;

    private static final int RANT_RECEIPT = 1018;

    private static final int SOULSHOT_NO_GRADE = 1835;

    private static final int RING_OF_KNOWLEDGE = 875;

    public Q153_DeliverGoods() {
        super(153, "Deliver Goods");
        setItemsIds(1012, 1013, 1014, 1015, 1016, 1017, 1018);
        addStartNpc(30041);
        addTalkId(30002, 30003, 30041, 30054);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q153_DeliverGoods");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30041-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1012, 1);
            st.giveItems(1015, 1);
            st.giveItems(1014, 1);
            st.giveItems(1013, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q153_DeliverGoods");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 2) ? "30041-00.htm" : "30041-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30041:
                        if (st.getInt("cond") == 1) {
                            htmltext = "30041-03.htm";
                            break;
                        }
                        if (st.getInt("cond") == 2) {
                            htmltext = "30041-04.htm";
                            st.takeItems(1012, 1);
                            st.takeItems(1016, 1);
                            st.takeItems(1017, 1);
                            st.takeItems(1018, 1);
                            st.giveItems(875, 1);
                            st.giveItems(875, 1);
                            st.rewardExpAndSp(600L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30002:
                        if (st.hasQuestItems(1013)) {
                            htmltext = "30002-01.htm";
                            st.takeItems(1013, 1);
                            st.giveItems(1016, 1);
                            if (st.hasQuestItems(1017, 1018)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30002-02.htm";
                        break;
                    case 30003:
                        if (st.hasQuestItems(1014)) {
                            htmltext = "30003-01.htm";
                            st.takeItems(1014, 1);
                            st.giveItems(1017, 1);
                            st.giveItems(1835, 3);
                            if (st.hasQuestItems(1016, 1018)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30003-02.htm";
                        break;
                    case 30054:
                        if (st.hasQuestItems(1015)) {
                            htmltext = "30054-01.htm";
                            st.takeItems(1015, 1);
                            st.giveItems(1018, 1);
                            if (st.hasQuestItems(1016, 1017)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30054-02.htm";
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
