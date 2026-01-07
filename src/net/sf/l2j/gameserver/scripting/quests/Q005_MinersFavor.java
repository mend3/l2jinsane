package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q005_MinersFavor extends Quest {
    private static final String qn = "Q005_MinersFavor";

    private static final int BOLTER = 30554;

    private static final int SHARI = 30517;

    private static final int GARITA = 30518;

    private static final int REED = 30520;

    private static final int BRUNON = 30526;

    private static final int BOLTERS_LIST = 1547;

    private static final int MINING_BOOTS = 1548;

    private static final int MINERS_PICK = 1549;

    private static final int BOOMBOOM_POWDER = 1550;

    private static final int REDSTONE_BEER = 1551;

    private static final int BOLTERS_SMELLY_SOCKS = 1552;

    private static final int NECKLACE = 906;

    public Q005_MinersFavor() {
        super(5, "Miner's Favor");
        setItemsIds(1547, 1548, 1549, 1550, 1551, 1552);
        addStartNpc(30554);
        addTalkId(30554, 30517, 30518, 30520, 30526);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q005_MinersFavor");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30554-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1547, 1);
            st.giveItems(1552, 1);
        } else if (event.equalsIgnoreCase("30526-02.htm")) {
            st.takeItems(1552, 1);
            st.giveItems(1549, 1);
            if (st.hasQuestItems(1548, 1550, 1551)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q005_MinersFavor");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 2) ? "30554-01.htm" : "30554-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30554:
                        if (cond == 1) {
                            htmltext = "30554-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30554-06.htm";
                            st.takeItems(1547, 1);
                            st.takeItems(1550, 1);
                            st.takeItems(1549, 1);
                            st.takeItems(1548, 1);
                            st.takeItems(1551, 1);
                            st.giveItems(906, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30517:
                        if (cond == 1 && !st.hasQuestItems(1550)) {
                            htmltext = "30517-01.htm";
                            st.giveItems(1550, 1);
                            if (st.hasQuestItems(1548, 1549, 1551)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30517-02.htm";
                        break;
                    case 30518:
                        if (cond == 1 && !st.hasQuestItems(1548)) {
                            htmltext = "30518-01.htm";
                            st.giveItems(1548, 1);
                            if (st.hasQuestItems(1549, 1550, 1551)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30518-02.htm";
                        break;
                    case 30520:
                        if (cond == 1 && !st.hasQuestItems(1551)) {
                            htmltext = "30520-01.htm";
                            st.giveItems(1551, 1);
                            if (st.hasQuestItems(1548, 1549, 1550)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30520-02.htm";
                        break;
                    case 30526:
                        if (cond == 1 && !st.hasQuestItems(1549)) {
                            htmltext = "30526-01.htm";
                            break;
                        }
                        htmltext = "30526-03.htm";
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
