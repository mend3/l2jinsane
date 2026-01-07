package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q115_TheOtherSideOfTruth extends Quest {
    private static final String qn = "Q115_TheOtherSideOfTruth";

    private static final int MISA_LETTER = 8079;

    private static final int RAFFORTY_LETTER = 8080;

    private static final int PIECE_OF_TABLET = 8081;

    private static final int REPORT_PIECE = 8082;

    private static final int RAFFORTY = 32020;

    private static final int MISA = 32018;

    private static final int KIERRE = 32022;

    private static final int SCULPTURE_1 = 32021;

    private static final int SCULPTURE_2 = 32077;

    private static final int SCULPTURE_3 = 32078;

    private static final int SCULPTURE_4 = 32079;

    private static final int SUSPICIOUS_MAN = 32019;

    private static final Map<Integer, int[]> NPC_VALUES = new HashMap<>();

    public Q115_TheOtherSideOfTruth() {
        super(115, "The Other Side of Truth");
        NPC_VALUES.put(Integer.valueOf(32021), new int[]{1, 2, 1, 6, 10, 12, 14});
        NPC_VALUES.put(Integer.valueOf(32077), new int[]{2, 4, 1, 5, 9, 12, 13});
        NPC_VALUES.put(Integer.valueOf(32078), new int[]{4, 8, 3, 3, 9, 10, 11});
        NPC_VALUES.put(Integer.valueOf(32079), new int[]{8, 0, 7, 3, 5, 6, 7});
        setItemsIds(8079, 8080, 8081, 8082);
        addStartNpc(32020);
        addTalkId(32020, 32018, 32022, 32021, 32077, 32078, 32079);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q115_TheOtherSideOfTruth");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32020-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32020-05.htm") || event.equalsIgnoreCase("32020-08.htm") || event.equalsIgnoreCase("32020-13.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("32020-07.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8079, 1);
        } else if (event.equalsIgnoreCase("32020-11.htm") || event.equalsIgnoreCase("32020-12.htm")) {
            if (st.getInt("cond") == 3) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("32020-17.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32020-23.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8082, 1);
        } else if (event.equalsIgnoreCase("32020-27.htm")) {
            if (!st.hasQuestItems(8081)) {
                st.set("cond", "11");
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "32020-25.htm";
                st.takeItems(8081, 1);
                st.rewardItems(57, 60040);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        } else if (event.equalsIgnoreCase("32020-28.htm")) {
            if (!st.hasQuestItems(8081)) {
                st.set("cond", "11");
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "32020-26.htm";
                st.takeItems(8081, 1);
                st.rewardItems(57, 60040);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        } else if (event.equalsIgnoreCase("32018-05.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8080, 1);
        } else if (event.equalsIgnoreCase("sculpture-03.htm")) {
            int[] infos = NPC_VALUES.get(Integer.valueOf(npc.getNpcId()));
            int ex = st.getInt("ex");
            int numberToModulo = (infos[1] == 0) ? ex : (ex % infos[1]);
            if (numberToModulo <= infos[2])
                if (ex == infos[3] || ex == infos[4] || ex == infos[5]) {
                    st.set("ex", String.valueOf(ex + infos[0]));
                    st.giveItems(8081, 1);
                    st.playSound("ItemSound.quest_itemget");
                }
        } else if (event.equalsIgnoreCase("sculpture-04.htm")) {
            int[] infos = NPC_VALUES.get(Integer.valueOf(npc.getNpcId()));
            int ex = st.getInt("ex");
            int numberToModulo = (infos[1] == 0) ? ex : (ex % infos[1]);
            if (numberToModulo <= infos[2] && (
                    ex == infos[3] || ex == infos[4] || ex == infos[5]))
                st.set("ex", String.valueOf(ex + infos[0]));
        } else if (event.equalsIgnoreCase("sculpture-06.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            Npc stranger = addSpawn(32019, player.getX() + 50, player.getY() + 50, player.getZ(), 0, false, 3100L, false);
            stranger.broadcastNpcSay("This looks like the right place...");
            startQuestTimer("despawn_1", 3000L, stranger, player, false);
        } else if (event.equalsIgnoreCase("32022-02.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8082, 1);
            Npc stranger = addSpawn(32019, player.getX() + 50, player.getY() + 50, player.getZ(), 0, false, 5100L, false);
            stranger.broadcastNpcSay("We meet again.");
            startQuestTimer("despawn_2", 5000L, stranger, player, false);
        } else {
            if (event.equalsIgnoreCase("despawn_1")) {
                npc.broadcastNpcSay("I see someone. Is this fate?");
                return null;
            }
            if (event.equalsIgnoreCase("despawn_2")) {
                npc.broadcastNpcSay("Don't bother trying to find out more about me. Follow your own destiny.");
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q115_TheOtherSideOfTruth");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 53) ? "32020-02.htm" : "32020-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 32020:
                        if (cond == 1) {
                            htmltext = "32020-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "32020-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32020-09.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "32020-16.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "32020-18.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(8080, 1);
                            break;
                        }
                        if (cond == 6) {
                            if (!st.hasQuestItems(8080)) {
                                htmltext = "32020-20.htm";
                                st.giveItems(8080, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "32020-19.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "32020-19.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "32020-21.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "32020-22.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "32020-24.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "32020-29.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "32020-30.htm";
                            st.takeItems(8081, 1);
                            st.rewardItems(57, 60040);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 32018:
                        if (cond == 1) {
                            htmltext = "32018-02.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(8079, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "32018-03.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "32018-04.htm";
                            break;
                        }
                        if (cond > 6) {
                            htmltext = "32018-06.htm";
                            break;
                        }
                        htmltext = "32018-01.htm";
                        break;
                    case 32022:
                        if (cond == 8) {
                            htmltext = "32022-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            if (!st.hasQuestItems(8082)) {
                                htmltext = "32022-04.htm";
                                st.giveItems(8082, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "32022-03.htm";
                            break;
                        }
                        if (cond == 11)
                            htmltext = "32022-05.htm";
                        break;
                    case 32021:
                    case 32077:
                    case 32078:
                    case 32079:
                        if (cond == 7) {
                            int[] infos = NPC_VALUES.get(Integer.valueOf(npc.getNpcId()));
                            int ex = st.getInt("ex");
                            int numberToModulo = (infos[1] == 0) ? ex : (ex % infos[1]);
                            if (numberToModulo <= infos[2]) {
                                if (ex == infos[3] || ex == infos[4] || ex == infos[5]) {
                                    htmltext = "sculpture-02.htm";
                                    break;
                                }
                                if (ex == infos[6]) {
                                    htmltext = "sculpture-05.htm";
                                    break;
                                }
                                st.set("ex", String.valueOf(ex + infos[0]));
                                htmltext = "sculpture-01.htm";
                                break;
                            }
                            htmltext = "sculpture-01a.htm";
                            break;
                        }
                        if (cond > 7 && cond < 11) {
                            htmltext = "sculpture-07.htm";
                            break;
                        }
                        if (cond == 11) {
                            if (!st.hasQuestItems(8081)) {
                                htmltext = "sculpture-08.htm";
                                st.set("cond", "12");
                                st.playSound("ItemSound.quest_middle");
                                st.giveItems(8081, 1);
                                break;
                            }
                            htmltext = "sculpture-09.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "sculpture-09.htm";
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
