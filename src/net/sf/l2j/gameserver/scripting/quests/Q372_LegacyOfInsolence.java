package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q372_LegacyOfInsolence extends Quest {
    private static final String qn = "Q372_LegacyOfInsolence";

    private static final int WALDERAL = 30844;

    private static final int PATRIN = 30929;

    private static final int HOLLY = 30839;

    private static final int CLAUDIA = 31001;

    private static final int DESMOND = 30855;

    private static final int[][] MONSTERS_DROPS = new int[][]{{20817, 20821, 20825, 20829, 21069, 21063}, {5966, 5966, 5966, 5967, 5968, 5969}, {300000, 400000, 460000, 400000, 250000, 250000}};

    private static final int[][] SCROLLS = new int[][]{{5989, 6001}, {5984, 5988}, {5979, 5983}, {5972, 5978}, {5972, 5978}};

    private static final int[][][] REWARDS_MATRICE = new int[][][]{{{13, 5496}, {26, 5508}, {40, 5525}, {58, 5368}, {76, 5392}, {100, 5426}}, {{13, 5497}, {26, 5509}, {40, 5526}, {58, 5370}, {76, 5394}, {100, 5428}}, {{20, 5502}, {40, 5514}, {58, 5527}, {73, 5380}, {87, 5404}, {100, 5430}}, {{20, 5503}, {40, 5515}, {58, 5528}, {73, 5382}, {87, 5406}, {100, 5432}}, {{33, 5496}, {66, 5508}, {89, 5525}, {100, 57}}, {{33, 5497}, {66, 5509}, {89, 5526}, {100, 57}}, {{35, 5502}, {70, 5514}, {87, 5527}, {100, 57}}, {{35, 5503}, {70, 5515}, {87, 5528}, {100, 57}}};

    public Q372_LegacyOfInsolence() {
        super(372, "Legacy of Insolence");
        addStartNpc(30844);
        addTalkId(30844, 30929, 30839, 31001, 30855);
        addKillId(MONSTERS_DROPS[0]);
    }

    private static String checkAndRewardItems(QuestState st, int itemType, int rewardType, int npcId) {
        int[] itemsToCheck = SCROLLS[itemType];
        int item;
        for (item = itemsToCheck[0]; item <= itemsToCheck[1]; item++) {
            if (!st.hasQuestItems(item))
                return "" + npcId + npcId;
        }
        for (item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
            st.takeItems(item, 1);
        int[][] rewards = REWARDS_MATRICE[rewardType];
        int chance = Rnd.get(100);
        for (int[] reward : rewards) {
            if (chance < reward[0]) {
                st.rewardItems(reward[1], 1);
                return npcId + "-02.htm";
            }
        }
        return "" + npcId + npcId;
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q372_LegacyOfInsolence");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30844-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30844-05b.htm")) {
            if (st.getInt("cond") == 1) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("30844-07.htm")) {
            for (int blueprint = 5989; blueprint <= 6001; blueprint++) {
                if (!st.hasQuestItems(blueprint)) {
                    htmltext = "30844-06.htm";
                    break;
                }
            }
        } else if (event.startsWith("30844-07-")) {
            checkAndRewardItems(st, 0, Integer.parseInt(event.substring(9, 10)), 30844);
        } else if (event.equalsIgnoreCase("30844-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q372_LegacyOfInsolence");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 59) ? "30844-01.htm" : "30844-02.htm";
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 30844 -> "30844-05.htm";
                    case 30839 -> checkAndRewardItems(st, 1, 4, 30839);
                    case 30929 -> checkAndRewardItems(st, 2, 5, 30929);
                    case 31001 -> checkAndRewardItems(st, 3, 6, 31001);
                    case 30855 -> checkAndRewardItems(st, 4, 7, 30855);
                    default -> htmltext;
                };
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        for (int i = 0; i < (MONSTERS_DROPS[0]).length; i++) {
            if (MONSTERS_DROPS[0][i] == npcId) {
                st.dropItems(MONSTERS_DROPS[1][i], 1, 0, MONSTERS_DROPS[2][i]);
                break;
            }
        }
        return null;
    }
}
