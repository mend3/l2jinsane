package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q213_TrialOfTheSeeker extends Quest {
    private static final String qn = "Q213_TrialOfTheSeeker";

    private static final int DUFNER_LETTER = 2647;

    private static final int TERRY_ORDER_1 = 2648;

    private static final int TERRY_ORDER_2 = 2649;

    private static final int TERRY_LETTER = 2650;

    private static final int VIKTOR_LETTER = 2651;

    private static final int HAWKEYE_LETTER = 2652;

    private static final int MYSTERIOUS_RUNESTONE = 2653;

    private static final int OL_MAHUM_RUNESTONE = 2654;

    private static final int TUREK_RUNESTONE = 2655;

    private static final int ANT_RUNESTONE = 2656;

    private static final int TURAK_BUGBEAR_RUNESTONE = 2657;

    private static final int TERRY_BOX = 2658;

    private static final int VIKTOR_REQUEST = 2659;

    private static final int MEDUSA_SCALES = 2660;

    private static final int SHILEN_RUNESTONE = 2661;

    private static final int ANALYSIS_REQUEST = 2662;

    private static final int MARINA_LETTER = 2663;

    private static final int EXPERIMENT_TOOLS = 2664;

    private static final int ANALYSIS_RESULT = 2665;

    private static final int TERRY_ORDER_3 = 2666;

    private static final int LIST_OF_HOST = 2667;

    private static final int ABYSS_RUNESTONE_1 = 2668;

    private static final int ABYSS_RUNESTONE_2 = 2669;

    private static final int ABYSS_RUNESTONE_3 = 2670;

    private static final int ABYSS_RUNESTONE_4 = 2671;

    private static final int TERRY_REPORT = 2672;

    private static final int MARK_OF_SEEKER = 2673;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int TERRY = 30064;

    private static final int DUFNER = 30106;

    private static final int BRUNON = 30526;

    private static final int VIKTOR = 30684;

    private static final int MARINA = 30715;

    private static final int NEER_GHOUL_BERSERKER = 20198;

    private static final int ANT_CAPTAIN = 20080;

    private static final int OL_MAHUM_CAPTAIN = 20211;

    private static final int TURAK_BUGBEAR_WARRIOR = 20249;

    private static final int TUREK_ORC_WARLORD = 20495;

    private static final int MEDUSA = 20158;

    private static final int ANT_WARRIOR_CAPTAIN = 20088;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int BREKA_ORC_OVERLORD = 20270;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    public Q213_TrialOfTheSeeker() {
        super(213, "Trial of the Seeker");
        setItemsIds(2647, 2648, 2649, 2650, 2651, 2652, 2653, 2654, 2655, 2656,
                2657, 2658, 2659, 2660, 2661, 2662, 2663, 2664, 2665, 2666,
                2667, 2668, 2669, 2670, 2671, 2672);
        addStartNpc(30106);
        addTalkId(30064, 30106, 30526, 30684, 30715);
        addKillId(20198, 20080, 20211, 20249, 20495, 20088, 20234, 20270, 20580, 20158);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q213_TrialOfTheSeeker");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30106-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2647, 1);
            if (!player.getMemos().getBool("secondClassChange35", false)) {
                htmltext = "30106-05a.htm";
                st.giveItems(7562, DF_REWARD_35.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange35", true);
            }
        } else if (event.equalsIgnoreCase("30064-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2647, 1);
            st.giveItems(2648, 1);
        } else if (event.equalsIgnoreCase("30064-06.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2653, 1);
            st.takeItems(2648, 1);
            st.giveItems(2649, 1);
        } else if (event.equalsIgnoreCase("30064-10.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2656, 1);
            st.takeItems(2654, 1);
            st.takeItems(2657, 1);
            st.takeItems(2655, 1);
            st.takeItems(2649, 1);
            st.giveItems(2658, 1);
            st.giveItems(2650, 1);
        } else if (event.equalsIgnoreCase("30064-18.htm")) {
            if (player.getLevel() < 36) {
                htmltext = "30064-17.htm";
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(2665, 1);
                st.giveItems(2666, 1);
            } else {
                st.set("cond", "16");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(2665, 1);
                st.giveItems(2667, 1);
            }
        } else if (event.equalsIgnoreCase("30684-05.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2650, 1);
            st.giveItems(2651, 1);
        } else if (event.equalsIgnoreCase("30684-11.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2650, 1);
            st.takeItems(2658, 1);
            st.takeItems(2652, 1);
            st.takeItems(2651, 1);
            st.giveItems(2659, 1);
        } else if (event.equalsIgnoreCase("30684-15.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2659, 1);
            st.takeItems(2660, 10);
            st.giveItems(2662, 1);
            st.giveItems(2661, 1);
        } else if (event.equalsIgnoreCase("30715-02.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2661, 1);
            st.takeItems(2662, 1);
            st.giveItems(2663, 1);
        } else if (event.equalsIgnoreCase("30715-05.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2664, 1);
            st.giveItems(2665, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q213_TrialOfTheSeeker");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() == ClassId.ROGUE || player.getClassId() == ClassId.ELVEN_SCOUT || player.getClassId() == ClassId.ASSASSIN) {
                    htmltext = (player.getLevel() < 35) ? "30106-02.htm" : "30106-03.htm";
                    break;
                }
                htmltext = "30106-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30106:
                        if (cond == 1) {
                            htmltext = "30106-06.htm";
                            break;
                        }
                        if (cond > 1) {
                            if (!st.hasQuestItems(2672)) {
                                htmltext = "30106-07.htm";
                                break;
                            }
                            htmltext = "30106-08.htm";
                            st.takeItems(2672, 1);
                            st.giveItems(2673, 1);
                            st.rewardExpAndSp(72126L, 11000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30064:
                        if (cond == 1) {
                            htmltext = "30064-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30064-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30064-05.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30064-08.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30064-09.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30064-11.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30064-12.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2651, 1);
                            st.giveItems(2652, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30064-13.htm";
                            break;
                        }
                        if (cond > 8 && cond < 14) {
                            htmltext = "30064-14.htm";
                            break;
                        }
                        if (cond == 14) {
                            if (!st.hasQuestItems(2666)) {
                                htmltext = "30064-15.htm";
                                break;
                            }
                            if (player.getLevel() < 36) {
                                htmltext = "30064-20.htm";
                                break;
                            }
                            htmltext = "30064-21.htm";
                            st.set("cond", "15");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2666, 1);
                            st.giveItems(2667, 1);
                            break;
                        }
                        if (cond == 15 || cond == 16) {
                            htmltext = "30064-22.htm";
                            break;
                        }
                        if (cond == 17) {
                            if (!st.hasQuestItems(2672)) {
                                htmltext = "30064-23.htm";
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(2667, 1);
                                st.takeItems(2668, 1);
                                st.takeItems(2669, 1);
                                st.takeItems(2670, 1);
                                st.takeItems(2671, 1);
                                st.giveItems(2672, 1);
                                break;
                            }
                            htmltext = "30064-24.htm";
                        }
                        break;
                    case 30684:
                        if (cond == 6) {
                            htmltext = "30684-01.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30684-05.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30684-12.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30684-13.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30684-14.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30684-16.htm";
                            break;
                        }
                        if (cond > 11)
                            htmltext = "30684-17.htm";
                        break;
                    case 30715:
                        if (cond == 11) {
                            htmltext = "30715-01.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30715-03.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30715-04.htm";
                            break;
                        }
                        if (st.hasQuestItems(2665))
                            htmltext = "30715-06.htm";
                        break;
                    case 30526:
                        if (cond == 12) {
                            htmltext = "30526-01.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2663, 1);
                            st.giveItems(2664, 1);
                            break;
                        }
                        if (cond == 13)
                            htmltext = "30526-02.htm";
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
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20198:
                if (cond == 2 && st.dropItems(2653, 1, 1, 100000))
                    st.set("cond", "3");
                break;
            case 20080:
                if (cond == 4 && st.dropItems(2656, 1, 1, 250000) && st.hasQuestItems(2654, 2657, 2655))
                    st.set("cond", "5");
                break;
            case 20211:
                if (cond == 4 && st.dropItems(2654, 1, 1, 250000) && st.hasQuestItems(2656, 2657, 2655))
                    st.set("cond", "5");
                break;
            case 20249:
                if (cond == 4 && st.dropItems(2657, 1, 1, 250000) && st.hasQuestItems(2656, 2654, 2655))
                    st.set("cond", "5");
                break;
            case 20495:
                if (cond == 4 && st.dropItems(2655, 1, 1, 250000) && st.hasQuestItems(2656, 2654, 2657))
                    st.set("cond", "5");
                break;
            case 20158:
                if (cond == 9 && st.dropItems(2660, 1, 10, 300000))
                    st.set("cond", "10");
                break;
            case 20234:
                if ((cond == 15 || cond == 16) && st.dropItems(2668, 1, 1, 250000) && st.hasQuestItems(2669, 2670, 2671))
                    st.set("cond", "17");
                break;
            case 20270:
                if ((cond == 15 || cond == 16) && st.dropItems(2669, 1, 1, 250000) && st.hasQuestItems(2668, 2670, 2671))
                    st.set("cond", "17");
                break;
            case 20088:
                if ((cond == 15 || cond == 16) && st.dropItems(2670, 1, 1, 250000) && st.hasQuestItems(2668, 2669, 2671))
                    st.set("cond", "17");
                break;
            case 20580:
                if ((cond == 15 || cond == 16) && st.dropItems(2671, 1, 1, 250000) && st.hasQuestItems(2668, 2669, 2670))
                    st.set("cond", "17");
                break;
        }
        return null;
    }
}
