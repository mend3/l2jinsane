package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q384_WarehouseKeepersPastime extends Quest {
    private static final String qn = "Q384_WarehouseKeepersPastime";

    private static final int CLIFF = 30182;

    private static final int BAXT = 30685;

    private static final int MEDAL = 5964;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final int[][] INDEX_MAP = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {1, 4, 7}, {2, 5, 8}, {3, 6, 9}, {1, 5, 9}, {3, 5, 7}};

    private static final int[][] _rewards_10_win = new int[][]{{16, 1888}, {32, 1887}, {50, 1894}, {80, 952}, {89, 1890}, {98, 1893}, {100, 951}};

    private static final int[][] _rewards_10_lose = new int[][]{{50, 4041}, {80, 952}, {98, 1892}, {100, 917}};

    private static final int[][] _rewards_100_win = new int[][]{{50, 883}, {80, 951}, {98, 852}, {100, 401}};

    private static final int[][] _rewards_100_lose = new int[][]{{50, 951}, {80, 500}, {98, 2437}, {100, 135}};

    public Q384_WarehouseKeepersPastime() {
        super(384, "Warehouse Keeper's Pastime");
        CHANCES.put(20947, 160000);
        CHANCES.put(20948, 180000);
        CHANCES.put(20945, 120000);
        CHANCES.put(20946, 150000);
        CHANCES.put(20635, 150000);
        CHANCES.put(20773, 610000);
        CHANCES.put(20774, 600000);
        CHANCES.put(20760, 240000);
        CHANCES.put(20758, 240000);
        CHANCES.put(20759, 230000);
        CHANCES.put(20242, 220000);
        CHANCES.put(20281, 220000);
        CHANCES.put(20556, 140000);
        CHANCES.put(20668, 210000);
        CHANCES.put(20241, 220000);
        CHANCES.put(20286, 220000);
        CHANCES.put(20949, 190000);
        CHANCES.put(20950, 200000);
        CHANCES.put(20942, 90000);
        CHANCES.put(20943, 120000);
        CHANCES.put(20944, 110000);
        CHANCES.put(20559, 140000);
        CHANCES.put(20243, 210000);
        CHANCES.put(20282, 210000);
        CHANCES.put(20677, 340000);
        CHANCES.put(20605, 150000);
        setItemsIds(5964);
        addStartNpc(30182);
        addTalkId(30182, 30685);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    private static String fillBoard(QuestState st, String htmltext) {
        String[] playerArray = st.get("playerArray").split("");
        String[] board = st.get("board").split("");
        for (int i = 1; i < 10; i++)
            htmltext = htmltext.replace("<?Cell" + i + "?>", ArraysUtil.contains((Object[]) playerArray, board[i]) ? board[i] : "?");
        return htmltext;
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q384_WarehouseKeepersPastime");
        if (st == null)
            return htmltext;
        int npcId = npc.getNpcId();
        if (event.equalsIgnoreCase("30182-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase(npcId + "-08.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase(npcId + "-11.htm")) {
            if (st.getQuestItemsCount(5964) < 10) {
                htmltext = npcId + "-12.htm";
            } else {
                st.set("bet", "10");
                st.set("board", StringUtil.scrambleString("123456789"));
                st.takeItems(5964, 10);
            }
        } else if (event.equalsIgnoreCase(npcId + "-13.htm")) {
            if (st.getQuestItemsCount(5964) < 100) {
                htmltext = npcId + "-12.htm";
            } else {
                st.set("bet", "100");
                st.set("board", StringUtil.scrambleString("123456789"));
                st.takeItems(5964, 100);
            }
        } else if (event.startsWith("select_1-")) {
            st.set("playerArray", event.substring(9));
            htmltext = fillBoard(st, getHtmlText(npcId + "-14.htm"));
        } else if (event.startsWith("select_2-")) {
            String number = event.substring(9);
            String playerArray = st.get("playerArray");
            if (ArraysUtil.contains((Object[]) playerArray.split(""), number)) {
                htmltext = fillBoard(st, getHtmlText(npcId + "-" + npcId + ".htm"));
            } else {
                st.set("playerArray", playerArray.concat(number));
                htmltext = fillBoard(st, getHtmlText(npcId + "-" + npcId + ".htm"));
            }
        } else if (event.startsWith("select_3-")) {
            String number = event.substring(9);
            String playerArray = st.get("playerArray");
            if (ArraysUtil.contains((Object[]) playerArray.split(""), number)) {
                htmltext = fillBoard(st, getHtmlText(npcId + "-26.htm"));
            } else {
                String[] playerChoice = playerArray.concat(number).split("");
                String[] board = st.get("board").split("");
                int winningLines = 0;
                for (int[] map : INDEX_MAP) {
                    boolean won = true;
                    for (int index : map)
                        won &= ArraysUtil.contains((Object[]) playerChoice, board[index]);
                    if (won)
                        winningLines++;
                }
                if (winningLines == 3) {
                    htmltext = getHtmlText(npcId + "-23.htm");
                    int chance = Rnd.get(100);
                    for (int[] reward : (st.get("bet") == "10") ? _rewards_10_win : _rewards_100_win) {
                        if (chance < reward[0]) {
                            st.giveItems(reward[1], 1);
                            if (reward[1] == 2437)
                                st.giveItems(2463, 1);
                            break;
                        }
                    }
                } else if (winningLines == 0) {
                    htmltext = getHtmlText(npcId + "-25.htm");
                    int chance = Rnd.get(100);
                    for (int[] reward : (st.get("bet") == "10") ? _rewards_10_lose : _rewards_100_lose) {
                        if (chance < reward[0]) {
                            st.giveItems(reward[1], 1);
                            break;
                        }
                    }
                } else {
                    htmltext = getHtmlText(npcId + "-24.htm");
                }
                for (int i = 1; i < 10; i++) {
                    htmltext = htmltext.replace("<?Cell" + i + "?>", board[i]);
                    htmltext = htmltext.replace("<?FontColor" + i + "?>", ArraysUtil.contains((Object[]) playerChoice, board[i]) ? "ff0000" : "ffffff");
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q384_WarehouseKeepersPastime");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 40) ? "30182-04.htm" : "30182-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30182:
                        htmltext = (st.getQuestItemsCount(5964) < 10) ? "30182-06.htm" : "30182-07.htm";
                        break;
                    case 30685:
                        htmltext = (st.getQuestItemsCount(5964) < 10) ? "30685-01.htm" : "30685-02.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5964, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
