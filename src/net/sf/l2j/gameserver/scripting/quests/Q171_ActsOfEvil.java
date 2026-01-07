package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q171_ActsOfEvil extends Quest {
    private static final String qn = "Q171_ActsOfEvil";

    private static final int BLADE_MOLD = 4239;

    private static final int TYRA_BILL = 4240;

    private static final int RANGER_REPORT_1 = 4241;

    private static final int RANGER_REPORT_2 = 4242;

    private static final int RANGER_REPORT_3 = 4243;

    private static final int RANGER_REPORT_4 = 4244;

    private static final int WEAPON_TRADE_CONTRACT = 4245;

    private static final int ATTACK_DIRECTIVES = 4246;

    private static final int CERTIFICATE = 4247;

    private static final int CARGO_BOX = 4248;

    private static final int OL_MAHUM_HEAD = 4249;

    private static final int ALVAH = 30381;

    private static final int ARODIN = 30207;

    private static final int TYRA = 30420;

    private static final int ROLENTO = 30437;

    private static final int NETI = 30425;

    private static final int BURAI = 30617;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q171_ActsOfEvil() {
        super(171, "Acts of Evil");
        CHANCES.put(Integer.valueOf(20496), Integer.valueOf(530000));
        CHANCES.put(Integer.valueOf(20497), Integer.valueOf(550000));
        CHANCES.put(Integer.valueOf(20498), Integer.valueOf(510000));
        CHANCES.put(Integer.valueOf(20499), Integer.valueOf(500000));
        setItemsIds(4239, 4240, 4241, 4242, 4243, 4244, 4245, 4246, 4247, 4248,
                4249);
        addStartNpc(30381);
        addTalkId(30381, 30207, 30420, 30437, 30425, 30617);
        addKillId(20496, 20497, 20498, 20499, 20062, 20064, 20066, 20438);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q171_ActsOfEvil");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30381-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30207-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30381-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30381-07.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(4245, 1);
        } else if (event.equalsIgnoreCase("30437-03.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4248, 1);
            st.giveItems(4247, 1);
        } else if (event.equalsIgnoreCase("30617-04.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(4246, 1);
            st.takeItems(4248, 1);
            st.takeItems(4247, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q171_ActsOfEvil");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 27) ? "30381-01a.htm" : "30381-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30381:
                        if (cond < 4) {
                            htmltext = "30381-02a.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30381-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            if (st.hasQuestItems(4241, 4242, 4243, 4244)) {
                                htmltext = "30381-05.htm";
                                st.set("cond", "6");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4241, 1);
                                st.takeItems(4242, 1);
                                st.takeItems(4243, 1);
                                st.takeItems(4244, 1);
                                break;
                            }
                            htmltext = "30381-04a.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.hasQuestItems(4245, 4246)) {
                                htmltext = "30381-06.htm";
                                break;
                            }
                            htmltext = "30381-05a.htm";
                            break;
                        }
                        if (cond > 6 && cond < 11) {
                            htmltext = "30381-07a.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30381-08.htm";
                            st.rewardItems(57, 90000);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30207:
                        if (cond == 1) {
                            htmltext = "30207-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30207-01a.htm";
                            break;
                        }
                        if (cond == 3) {
                            if (st.hasQuestItems(4240)) {
                                htmltext = "30207-03.htm";
                                st.set("cond", "4");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4240, 1);
                                break;
                            }
                            htmltext = "30207-01a.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30207-03a.htm";
                        break;
                    case 30420:
                        if (cond == 2) {
                            if (st.getQuestItemsCount(4239) >= 20) {
                                htmltext = "30420-01.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4239, -1);
                                st.giveItems(4240, 1);
                                break;
                            }
                            htmltext = "30420-01b.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30420-01a.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30420-02.htm";
                        break;
                    case 30425:
                        if (cond == 7) {
                            htmltext = "30425-01.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30425-02.htm";
                        break;
                    case 30437:
                        if (cond == 8) {
                            htmltext = "30437-01.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30437-03a.htm";
                        break;
                    case 30617:
                        if (cond == 9 && st.hasQuestItems(4247, 4248, 4246)) {
                            htmltext = "30617-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            if (st.getQuestItemsCount(4249) >= 30) {
                                htmltext = "30617-05.htm";
                                st.set("cond", "11");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4249, -1);
                                st.rewardItems(57, 8000);
                                break;
                            }
                            htmltext = "30617-04a.htm";
                        }
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 20496:
            case 20497:
            case 20498:
            case 20499:
                if (st.getInt("cond") == 2 && !st.dropItems(4239, 1, 20, CHANCES.get(Integer.valueOf(npcId)))) {
                    int count = st.getQuestItemsCount(4239);
                    if (count == 5 || (count >= 10 && Rnd.get(100) < 25))
                        addSpawn(27190, player, false, 0L, true);
                }
                break;
            case 20062:
            case 20064:
                if (st.getInt("cond") == 5) {
                    if (!st.hasQuestItems(4241)) {
                        st.giveItems(4241, 1);
                        st.playSound("ItemSound.quest_itemget");
                        break;
                    }
                    if (Rnd.get(100) < 20) {
                        if (!st.hasQuestItems(4242)) {
                            st.giveItems(4242, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (!st.hasQuestItems(4243)) {
                            st.giveItems(4243, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (!st.hasQuestItems(4244)) {
                            st.giveItems(4244, 1);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                }
                break;
            case 20438:
                if (st.getInt("cond") == 6 && Rnd.get(100) < 10 && !st.hasQuestItems(4245, 4246)) {
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(4245, 1);
                    st.giveItems(4246, 1);
                }
                break;
            case 20066:
                if (st.getInt("cond") == 10)
                    st.dropItems(4249, 1, 30, 500000);
                break;
        }
        return null;
    }
}
