package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q378_MagnificentFeast extends Quest {
    private static final String qn = "Q378_MagnificentFeast";

    private static final int RANSPO = 30594;

    private static final int WINE_15 = 5956;

    private static final int WINE_30 = 5957;

    private static final int WINE_60 = 5958;

    private static final int MUSICAL_SCORE = 4421;

    private static final int SALAD_RECIPE = 1455;

    private static final int SAUCE_RECIPE = 1456;

    private static final int STEAK_RECIPE = 1457;

    private static final int RITRON_DESSERT = 5959;

    private static final Map<String, int[]> REWARDS = new HashMap<>();

    public Q378_MagnificentFeast() {
        super(378, "Magnificent Feast");
        REWARDS.put("9", new int[]{847, 1, 5700});
        REWARDS.put("10", new int[]{846, 2, 0});
        REWARDS.put("12", new int[]{909, 1, 25400});
        REWARDS.put("17", new int[]{846, 2, 1200});
        REWARDS.put("18", new int[]{879, 1, 6900});
        REWARDS.put("20", new int[]{890, 2, 8500});
        REWARDS.put("33", new int[]{879, 1, 8100});
        REWARDS.put("34", new int[]{910, 1, 0});
        REWARDS.put("36", new int[]{848, 1, 2200});
        addStartNpc(30594);
        addTalkId(30594);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q378_MagnificentFeast");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30594-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30594-4a.htm")) {
            if (st.hasQuestItems(5956)) {
                st.set("cond", "2");
                st.set("score", "1");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(5956, 1);
            } else {
                htmltext = "30594-4.htm";
            }
        } else if (event.equalsIgnoreCase("30594-4b.htm")) {
            if (st.hasQuestItems(5957)) {
                st.set("cond", "2");
                st.set("score", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(5957, 1);
            } else {
                htmltext = "30594-4.htm";
            }
        } else if (event.equalsIgnoreCase("30594-4c.htm")) {
            if (st.hasQuestItems(5958)) {
                st.set("cond", "2");
                st.set("score", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(5958, 1);
            } else {
                htmltext = "30594-4.htm";
            }
        } else if (event.equalsIgnoreCase("30594-6.htm")) {
            if (st.hasQuestItems(4421)) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(4421, 1);
            } else {
                htmltext = "30594-5.htm";
            }
        } else {
            int score = st.getInt("score");
            if (event.equalsIgnoreCase("30594-8a.htm")) {
                if (st.hasQuestItems(1455)) {
                    st.set("cond", "4");
                    st.set("score", String.valueOf(score + 8));
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1455, 1);
                } else {
                    htmltext = "30594-8.htm";
                }
            } else if (event.equalsIgnoreCase("30594-8b.htm")) {
                if (st.hasQuestItems(1456)) {
                    st.set("cond", "4");
                    st.set("score", String.valueOf(score + 16));
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1456, 1);
                } else {
                    htmltext = "30594-8.htm";
                }
            } else if (event.equalsIgnoreCase("30594-8c.htm")) {
                if (st.hasQuestItems(1457)) {
                    st.set("cond", "4");
                    st.set("score", String.valueOf(score + 32));
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1457, 1);
                } else {
                    htmltext = "30594-8.htm";
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q378_MagnificentFeast");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "30594-0.htm" : "30594-1.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30594-3.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = !st.hasQuestItems(4421) ? "30594-5.htm" : "30594-5a.htm";
                    break;
                }
                if (cond == 3) {
                    htmltext = "30594-7.htm";
                    break;
                }
                if (cond == 4) {
                    String score = st.get("score");
                    if (REWARDS.containsKey(score) && st.hasQuestItems(5959)) {
                        htmltext = "30594-10.htm";
                        st.takeItems(5959, 1);
                        st.giveItems(((int[]) REWARDS.get(score))[0], ((int[]) REWARDS.get(score))[1]);
                        int adena = ((int[]) REWARDS.get(score))[2];
                        if (adena > 0)
                            st.rewardItems(57, adena);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    }
                    htmltext = "30594-9.htm";
                }
                break;
        }
        return htmltext;
    }
}
