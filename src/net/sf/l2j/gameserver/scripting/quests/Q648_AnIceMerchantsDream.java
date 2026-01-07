package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q648_AnIceMerchantsDream extends Quest {
    private static final String qn = "Q648_AnIceMerchantsDream";

    private static final String qn2 = "Q115_TheOtherSideOfTruth";

    private static final int SILVER_HEMOCYTE = 8057;

    private static final int SILVER_ICE_CRYSTAL = 8077;

    private static final int BLACK_ICE_CRYSTAL = 8078;

    private static final Map<String, int[]> REWARDS = new HashMap<>();

    private static final int RAFFORTY = 32020;

    private static final int ICE_SHELF = 32023;

    private static final Map<Integer, int[]> CHANCES = new HashMap<>();

    public Q648_AnIceMerchantsDream() {
        super(648, "An Ice Merchant's Dream");
        REWARDS.put("a", new int[]{8077, 23, 1894});
        REWARDS.put("b", new int[]{8077, 6, 1881});
        REWARDS.put("c", new int[]{8077, 8, 1880});
        REWARDS.put("d", new int[]{8078, 1800, 729});
        REWARDS.put("e", new int[]{8078, 240, 730});
        REWARDS.put("f", new int[]{8078, 500, 947});
        REWARDS.put("g", new int[]{8078, 80, 948});
        CHANCES.put(Integer.valueOf(22080), new int[]{285000, 48000});
        CHANCES.put(Integer.valueOf(22081), new int[]{443000, 0});
        CHANCES.put(Integer.valueOf(22082), new int[]{510000, 0});
        CHANCES.put(Integer.valueOf(22084), new int[]{477000, 49000});
        CHANCES.put(Integer.valueOf(22085), new int[]{420000, 43000});
        CHANCES.put(Integer.valueOf(22086), new int[]{490000, 50000});
        CHANCES.put(Integer.valueOf(22087), new int[]{787000, 81000});
        CHANCES.put(Integer.valueOf(22088), new int[]{480000, 49000});
        CHANCES.put(Integer.valueOf(22089), new int[]{550000, 56000});
        CHANCES.put(Integer.valueOf(22090), new int[]{570000, 58000});
        CHANCES.put(Integer.valueOf(22092), new int[]{623000, 0});
        CHANCES.put(Integer.valueOf(22093), new int[]{910000, 93000});
        CHANCES.put(Integer.valueOf(22094), new int[]{553000, 57000});
        CHANCES.put(Integer.valueOf(22096), new int[]{593000, 61000});
        CHANCES.put(Integer.valueOf(22097), new int[]{693000, 71000});
        CHANCES.put(Integer.valueOf(22098), new int[]{717000, 74000});
        setItemsIds(8057, 8077, 8078);
        addStartNpc(32020, 32023);
        addTalkId(32020, 32023);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q648_AnIceMerchantsDream");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32020-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32020-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32020-14.htm") || event.equalsIgnoreCase("32020-15.htm")) {
            int black = st.getQuestItemsCount(8078);
            int silver = st.getQuestItemsCount(8077);
            if (silver + black > 0) {
                st.takeItems(8078, -1);
                st.takeItems(8077, -1);
                st.rewardItems(57, silver * 300 + black * 1200);
            } else {
                htmltext = "32020-16a.htm";
            }
        } else if (event.startsWith("32020-17")) {
            int[] reward = REWARDS.get(event.substring(8, 9));
            if (st.getQuestItemsCount(reward[0]) >= reward[1]) {
                st.takeItems(reward[0], reward[1]);
                st.rewardItems(reward[2], 1);
            } else {
                htmltext = "32020-15a.htm";
            }
        } else if (event.equalsIgnoreCase("32020-20.htm") || event.equalsIgnoreCase("32020-22.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("32023-05.htm")) {
            if (st.getInt("exCond") == 0)
                st.set("exCond", String.valueOf((Rnd.get(4) + 1) * 10));
        } else if (event.startsWith("32023-06-")) {
            int exCond = st.getInt("exCond");
            if (exCond > 0) {
                htmltext = "32023-06.htm";
                st.set("exCond", String.valueOf(exCond + (event.endsWith("chisel") ? 1 : 2)));
                st.playSound("ItemSound2.broken_key");
                st.takeItems(8077, 1);
            }
        } else if (event.startsWith("32023-07-")) {
            int exCond = st.getInt("exCond");
            if (exCond > 0) {
                int val = exCond / 10;
                if (val == exCond - val * 10 + (event.endsWith("knife") ? 0 : 2)) {
                    htmltext = "32023-07.htm";
                    st.playSound("ItemSound3.sys_enchant_success");
                    st.rewardItems(8078, 1);
                } else {
                    htmltext = "32023-08.htm";
                    st.playSound("ItemSound3.sys_enchant_failed");
                }
                st.set("exCond", "0");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q648_AnIceMerchantsDream");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (npc.getNpcId() == 32020) {
                    if (player.getLevel() < 53) {
                        htmltext = "32020-01.htm";
                        break;
                    }
                    QuestState st2 = player.getQuestState("Q115_TheOtherSideOfTruth");
                    htmltext = (st2 != null && st2.isCompleted()) ? "32020-02.htm" : "32020-03.htm";
                    break;
                }
                htmltext = "32023-01.htm";
                break;
            case 1:
                if (npc.getNpcId() == 32020) {
                    boolean hasItem = st.hasAtLeastOneQuestItem(8077, 8078);
                    QuestState st2 = player.getQuestState("Q115_TheOtherSideOfTruth");
                    if (st2 != null && st2.isCompleted()) {
                        htmltext = hasItem ? "32020-11.htm" : "32020-09.htm";
                        if (st.getInt("cond") == 1) {
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                        }
                        break;
                    }
                    htmltext = hasItem ? "32020-10.htm" : "32020-08.htm";
                    break;
                }
                if (!st.hasQuestItems(8077)) {
                    htmltext = "32023-02.htm";
                    break;
                }
                if (st.getInt("exCond") % 10 == 0) {
                    htmltext = "32023-03.htm";
                    st.set("exCond", "0");
                    break;
                }
                htmltext = "32023-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int[] chance = CHANCES.get(Integer.valueOf(npc.getNpcId()));
        st.dropItems(8077, 1, 0, chance[0]);
        if (st.getInt("cond") == 2 && chance[1] > 0)
            st.dropItems(8057, 1, 0, chance[1]);
        return null;
    }
}
