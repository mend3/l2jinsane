package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q152_ShardsOfGolem extends Quest {
    private static final String qn = "Q152_ShardsOfGolem";

    private static final int HARRIS_RECEIPT_1 = 1008;

    private static final int HARRIS_RECEIPT_2 = 1009;

    private static final int GOLEM_SHARD = 1010;

    private static final int TOOL_BOX = 1011;

    private static final int WOODEN_BREASTPLATE = 23;

    private static final int HARRIS = 30035;

    private static final int ALTRAN = 30283;

    private static final int STONE_GOLEM = 20016;

    public Q152_ShardsOfGolem() {
        super(152, "Shards of Golem");
        setItemsIds(1008, 1009, 1010, 1011);
        addStartNpc(30035);
        addTalkId(30035, 30283);
        addKillId(20016);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q152_ShardsOfGolem");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30035-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1008, 1);
        } else if (event.equalsIgnoreCase("30283-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1008, 1);
            st.giveItems(1009, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q152_ShardsOfGolem");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 10) ? "30035-01a.htm" : "30035-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30035:
                        if (cond < 4) {
                            htmltext = "30035-03.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30035-04.htm";
                            st.takeItems(1009, 1);
                            st.takeItems(1011, 1);
                            st.giveItems(23, 1);
                            st.rewardExpAndSp(5000L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30283:
                        if (cond == 1) {
                            htmltext = "30283-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30283-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30283-04.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1010, -1);
                            st.giveItems(1011, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30283-05.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "2");
        if (st == null)
            return null;
        if (st.dropItems(1010, 1, 5, 300000))
            st.set("cond", "3");
        return null;
    }
}
