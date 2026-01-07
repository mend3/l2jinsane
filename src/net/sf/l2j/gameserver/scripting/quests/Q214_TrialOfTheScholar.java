package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q214_TrialOfTheScholar extends Quest {
    private static final String qn = "Q214_TrialOfTheScholar";

    private static final int MIRIEN_SIGIL_1 = 2675;

    private static final int MIRIEN_SIGIL_2 = 2676;

    private static final int MIRIEN_SIGIL_3 = 2677;

    private static final int MIRIEN_INSTRUCTION = 2678;

    private static final int MARIA_LETTER_1 = 2679;

    private static final int MARIA_LETTER_2 = 2680;

    private static final int LUCAS_LETTER = 2681;

    private static final int LUCILLA_HANDBAG = 2682;

    private static final int CRETA_LETTER_1 = 2683;

    private static final int CRETA_PAINTING_1 = 2684;

    private static final int CRETA_PAINTING_2 = 2685;

    private static final int CRETA_PAINTING_3 = 2686;

    private static final int BROWN_SCROLL_SCRAP = 2687;

    private static final int CRYSTAL_OF_PURITY_1 = 2688;

    private static final int HIGH_PRIEST_SIGIL = 2689;

    private static final int GRAND_MAGISTER_SIGIL = 2690;

    private static final int CRONOS_SIGIL = 2691;

    private static final int SYLVAIN_LETTER = 2692;

    private static final int SYMBOL_OF_SYLVAIN = 2693;

    private static final int JUREK_LIST = 2694;

    private static final int MONSTER_EYE_DESTROYER_SKIN = 2695;

    private static final int SHAMAN_NECKLACE = 2696;

    private static final int SHACKLE_SCALP = 2697;

    private static final int SYMBOL_OF_JUREK = 2698;

    private static final int CRONOS_LETTER = 2699;

    private static final int DIETER_KEY = 2700;

    private static final int CRETA_LETTER_2 = 2701;

    private static final int DIETER_LETTER = 2702;

    private static final int DIETER_DIARY = 2703;

    private static final int RAUT_LETTER_ENVELOPE = 2704;

    private static final int TRIFF_RING = 2705;

    private static final int SCRIPTURE_CHAPTER_1 = 2706;

    private static final int SCRIPTURE_CHAPTER_2 = 2707;

    private static final int SCRIPTURE_CHAPTER_3 = 2708;

    private static final int SCRIPTURE_CHAPTER_4 = 2709;

    private static final int VALKON_REQUEST = 2710;

    private static final int POITAN_NOTES = 2711;

    private static final int STRONG_LIQUOR = 2713;

    private static final int CRYSTAL_OF_PURITY_2 = 2714;

    private static final int CASIAN_LIST = 2715;

    private static final int GHOUL_SKIN = 2716;

    private static final int MEDUSA_BLOOD = 2717;

    private static final int FETTERED_SOUL_ICHOR = 2718;

    private static final int ENCHANTED_GARGOYLE_NAIL = 2719;

    private static final int SYMBOL_OF_CRONOS = 2720;

    private static final int MARK_OF_SCHOLAR = 2674;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int SYLVAIN = 30070;

    private static final int LUCAS = 30071;

    private static final int VALKON = 30103;

    private static final int DIETER = 30111;

    private static final int JUREK = 30115;

    private static final int EDROC = 30230;

    private static final int RAUT = 30316;

    private static final int POITAN = 30458;

    private static final int MIRIEN = 30461;

    private static final int MARIA = 30608;

    private static final int CRETA = 30609;

    private static final int CRONOS = 30610;

    private static final int TRIFF = 30611;

    private static final int CASIAN = 30612;

    private static final int MONSTER_EYE_DESTROYER = 20068;

    private static final int MEDUSA = 20158;

    private static final int GHOUL = 20201;

    private static final int SHACKLE_1 = 20235;

    private static final int SHACKLE_2 = 20279;

    private static final int BREKA_ORC_SHAMAN = 20269;

    private static final int FETTERED_SOUL = 20552;

    private static final int GRANDIS = 20554;

    private static final int ENCHANTED_GARGOYLE = 20567;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    public Q214_TrialOfTheScholar() {
        super(214, "Trial Of The Scholar");
        setItemsIds(2675, 2676, 2677, 2678, 2679, 2680, 2681, 2682, 2683, 2684,
                2685, 2686, 2687, 2688, 2689, 2690, 2691, 2692, 2693, 2694,
                2695, 2696, 2697, 2698, 2699, 2700, 2701, 2702, 2703, 2704,
                2705, 2706, 2707, 2708, 2709, 2710, 2711, 2713, 2714, 2715,
                2716, 2717, 2718, 2719, 2720);
        addStartNpc(30461);
        addTalkId(30461, 30070, 30071, 30103, 30111, 30115, 30230, 30316, 30458, 30608,
                30609, 30610, 30611, 30612);
        addKillId(20068, 20158, 20201, 20235, 20279, 20269, 20552, 20554, 20567, 20580);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q214_TrialOfTheScholar");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30461-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2675, 1);
            if (!player.getMemos().getBool("secondClassChange35", false)) {
                htmltext = "30461-04a.htm";
                st.giveItems(7562, DF_REWARD_35.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange35", true);
            }
        } else if (event.equalsIgnoreCase("30461-09.htm")) {
            if (player.getLevel() < 36) {
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(2678, 1);
            } else {
                htmltext = "30461-10.htm";
                st.set("cond", "19");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(2676, 1);
                st.takeItems(2698, 1);
                st.giveItems(2677, 1);
            }
        } else if (event.equalsIgnoreCase("30070-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(2689, 1);
            st.giveItems(2692, 1);
        } else if (event.equalsIgnoreCase("30608-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2692, 1);
            st.giveItems(2679, 1);
        } else if (event.equalsIgnoreCase("30608-08.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2683, 1);
            st.giveItems(2682, 1);
        } else if (event.equalsIgnoreCase("30608-14.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2687, -1);
            st.takeItems(2686, 1);
            st.giveItems(2688, 1);
        } else if (event.equalsIgnoreCase("30115-03.htm")) {
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(2690, 1);
            st.giveItems(2694, 1);
        } else if (event.equalsIgnoreCase("30071-04.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2685, 1);
            st.giveItems(2686, 1);
        } else if (event.equalsIgnoreCase("30609-05.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2680, 1);
            st.giveItems(2683, 1);
        } else if (event.equalsIgnoreCase("30609-09.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2682, 1);
            st.giveItems(2684, 1);
        } else if (event.equalsIgnoreCase("30609-14.htm")) {
            st.set("cond", "22");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2700, 1);
            st.giveItems(2701, 1);
        } else if (event.equalsIgnoreCase("30610-10.htm")) {
            st.set("cond", "20");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(2699, 1);
            st.giveItems(2691, 1);
        } else if (event.equalsIgnoreCase("30610-14.htm")) {
            st.set("cond", "31");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2691, 1);
            st.takeItems(2703, 1);
            st.takeItems(2706, 1);
            st.takeItems(2707, 1);
            st.takeItems(2708, 1);
            st.takeItems(2709, 1);
            st.takeItems(2705, 1);
            st.giveItems(2720, 1);
        } else if (event.equalsIgnoreCase("30111-05.htm")) {
            st.set("cond", "21");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2699, 1);
            st.giveItems(2700, 1);
        } else if (event.equalsIgnoreCase("30111-09.htm")) {
            st.set("cond", "23");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2701, 1);
            st.giveItems(2703, 1);
            st.giveItems(2702, 1);
        } else if (event.equalsIgnoreCase("30230-02.htm")) {
            st.set("cond", "24");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2702, 1);
            st.giveItems(2704, 1);
        } else if (event.equalsIgnoreCase("30316-02.htm")) {
            st.set("cond", "25");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2704, 1);
            st.giveItems(2706, 1);
            st.giveItems(2713, 1);
        } else if (event.equalsIgnoreCase("30611-04.htm")) {
            st.set("cond", "26");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2713, 1);
            st.giveItems(2705, 1);
        } else if (event.equalsIgnoreCase("30103-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2710, 1);
        } else if (event.equalsIgnoreCase("30612-04.htm")) {
            st.set("cond", "28");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(2715, 1);
        } else if (event.equalsIgnoreCase("30612-07.htm")) {
            st.set("cond", "30");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2715, 1);
            st.takeItems(2719, -1);
            st.takeItems(2718, -1);
            st.takeItems(2716, -1);
            st.takeItems(2717, -1);
            st.takeItems(2711, 1);
            st.giveItems(2709, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q214_TrialOfTheScholar");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.ELVEN_WIZARD && player.getClassId() != ClassId.DARK_WIZARD) {
                    htmltext = "30461-01.htm";
                    break;
                }
                if (player.getLevel() < 35) {
                    htmltext = "30461-02.htm";
                    break;
                }
                htmltext = "30461-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30461:
                        if (cond < 14) {
                            htmltext = "30461-05.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30461-06.htm";
                            st.set("cond", "15");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2675, 1);
                            st.takeItems(2693, 1);
                            st.giveItems(2676, 1);
                            break;
                        }
                        if (cond > 14 && cond < 18) {
                            htmltext = "30461-07.htm";
                            break;
                        }
                        if (cond == 18) {
                            if (!st.hasQuestItems(2678)) {
                                htmltext = "30461-08.htm";
                                break;
                            }
                            if (player.getLevel() < 36) {
                                htmltext = "30461-11.htm";
                                break;
                            }
                            htmltext = "30461-12.htm";
                            st.set("cond", "19");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2678, 1);
                            st.takeItems(2676, 1);
                            st.takeItems(2698, 1);
                            st.giveItems(2677, 1);
                            break;
                        }
                        if (cond > 18 && cond < 31) {
                            htmltext = "30461-13.htm";
                            break;
                        }
                        if (cond == 31) {
                            htmltext = "30461-14.htm";
                            st.takeItems(2677, 1);
                            st.takeItems(2720, 1);
                            st.giveItems(2674, 1);
                            st.rewardExpAndSp(80265L, 30000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30070:
                        if (cond == 1) {
                            htmltext = "30070-01.htm";
                            break;
                        }
                        if (cond < 13) {
                            htmltext = "30070-03.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30070-04.htm";
                            st.set("cond", "14");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2688, 1);
                            st.takeItems(2689, 1);
                            st.giveItems(2693, 1);
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30070-05.htm";
                            break;
                        }
                        if (cond > 14)
                            htmltext = "30070-06.htm";
                        break;
                    case 30608:
                        if (cond == 2) {
                            htmltext = "30608-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30608-03.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30608-04.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2681, 1);
                            st.giveItems(2680, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30608-05.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30608-06.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30608-09.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30608-10.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2684, 1);
                            st.giveItems(2685, 1);
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30608-11.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30608-12.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30608-12.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30608-13.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30608-15.htm";
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(2693, 2676)) {
                            htmltext = "30608-16.htm";
                            break;
                        }
                        if (cond > 18) {
                            if (!st.hasQuestItems(2710)) {
                                htmltext = "30608-17.htm";
                                break;
                            }
                            htmltext = "30608-18.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(2710, 1);
                            st.giveItems(2714, 1);
                        }
                        break;
                    case 30115:
                        if (cond == 15) {
                            htmltext = "30115-01.htm";
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30115-04.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30115-05.htm";
                            st.set("cond", "18");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2690, 1);
                            st.takeItems(2694, 1);
                            st.takeItems(2695, -1);
                            st.takeItems(2697, -1);
                            st.takeItems(2696, -1);
                            st.giveItems(2698, 1);
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30115-06.htm";
                            break;
                        }
                        if (cond > 18)
                            htmltext = "30115-07.htm";
                        break;
                    case 30071:
                        if (cond == 3) {
                            htmltext = "30071-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2679, 1);
                            st.giveItems(2681, 1);
                            break;
                        }
                        if (cond > 3 && cond < 9) {
                            htmltext = "30071-02.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30071-03.htm";
                            break;
                        }
                        if (cond == 10 || cond == 11) {
                            htmltext = "30071-05.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30071-06.htm";
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30071-07.htm";
                        break;
                    case 30609:
                        if (cond == 5) {
                            htmltext = "30609-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30609-06.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30609-07.htm";
                            break;
                        }
                        if (cond > 7 && cond < 13) {
                            htmltext = "30609-10.htm";
                            break;
                        }
                        if (cond >= 13 && cond < 19) {
                            htmltext = "30609-11.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "30609-12.htm";
                            break;
                        }
                        if (cond > 21)
                            htmltext = "30609-15.htm";
                        break;
                    case 30610:
                        if (cond == 19) {
                            htmltext = "30610-01.htm";
                            break;
                        }
                        if (cond > 19 && cond < 30) {
                            htmltext = "30610-11.htm";
                            break;
                        }
                        if (cond == 30) {
                            htmltext = "30610-12.htm";
                            break;
                        }
                        if (cond == 31)
                            htmltext = "30610-15.htm";
                        break;
                    case 30111:
                        if (cond == 20) {
                            htmltext = "30111-01.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "30111-06.htm";
                            break;
                        }
                        if (cond == 22) {
                            htmltext = "30111-07.htm";
                            break;
                        }
                        if (cond == 23) {
                            htmltext = "30111-10.htm";
                            break;
                        }
                        if (cond == 24) {
                            htmltext = "30111-11.htm";
                            break;
                        }
                        if (cond > 24 && cond < 31) {
                            htmltext = !st.hasQuestItems(2706, 2707, 2708, 2709) ? "30111-12.htm" : "30111-13.htm";
                            break;
                        }
                        if (cond == 31)
                            htmltext = "30111-15.htm";
                        break;
                    case 30230:
                        if (cond == 23) {
                            htmltext = "30230-01.htm";
                            break;
                        }
                        if (cond == 24) {
                            htmltext = "30230-03.htm";
                            break;
                        }
                        if (cond > 24)
                            htmltext = "30230-04.htm";
                        break;
                    case 30316:
                        if (cond == 24) {
                            htmltext = "30316-01.htm";
                            break;
                        }
                        if (cond == 25) {
                            htmltext = "30316-04.htm";
                            break;
                        }
                        if (cond > 25)
                            htmltext = "30316-05.htm";
                        break;
                    case 30611:
                        if (cond == 25) {
                            htmltext = "30611-01.htm";
                            break;
                        }
                        if (cond > 25)
                            htmltext = "30611-05.htm";
                        break;
                    case 30103:
                        if (st.hasQuestItems(2705)) {
                            if (!st.hasQuestItems(2707)) {
                                if (!st.hasQuestItems(2710)) {
                                    if (!st.hasQuestItems(2714)) {
                                        htmltext = "30103-01.htm";
                                        break;
                                    }
                                    htmltext = "30103-06.htm";
                                    st.playSound("ItemSound.quest_itemget");
                                    st.takeItems(2714, 1);
                                    st.giveItems(2707, 1);
                                    break;
                                }
                                htmltext = "30103-05.htm";
                                break;
                            }
                            htmltext = "30103-07.htm";
                        }
                        break;
                    case 30458:
                        if (cond == 26 || cond == 27) {
                            if (!st.hasQuestItems(2711)) {
                                htmltext = "30458-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(2711, 1);
                                break;
                            }
                            htmltext = "30458-02.htm";
                            break;
                        }
                        if (cond == 28 || cond == 29) {
                            htmltext = "30458-03.htm";
                            break;
                        }
                        if (cond == 30)
                            htmltext = "30458-04.htm";
                        break;
                    case 30612:
                        if ((cond == 26 || cond == 27) && st.hasQuestItems(2711)) {
                            if (st.hasQuestItems(2706, 2707, 2708)) {
                                htmltext = "30612-02.htm";
                                break;
                            }
                            htmltext = "30612-01.htm";
                            if (cond == 26) {
                                st.set("cond", "27");
                                st.playSound("ItemSound.quest_middle");
                            }
                            break;
                        }
                        if (cond == 28) {
                            htmltext = "30612-05.htm";
                            break;
                        }
                        if (cond == 29) {
                            htmltext = "30612-06.htm";
                            break;
                        }
                        if (cond == 30)
                            htmltext = "30612-08.htm";
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
        switch (npc.getNpcId()) {
            case 20580:
                if (st.getInt("cond") == 11 && st.dropItems(2687, 1, 5, 500000))
                    st.set("cond", "12");
                break;
            case 20235:
            case 20279:
                if (st.getInt("cond") == 16 && st.dropItems(2697, 1, 2, 500000) &&
                        st.getQuestItemsCount(2695) == 5 && st.getQuestItemsCount(2696) == 5)
                    st.set("cond", "17");
                break;
            case 20068:
                if (st.getInt("cond") == 16 && st.dropItems(2695, 1, 5, 500000) &&
                        st.getQuestItemsCount(2697) == 2 && st.getQuestItemsCount(2696) == 5)
                    st.set("cond", "17");
                break;
            case 20269:
                if (st.getInt("cond") == 16 && st.dropItems(2696, 1, 5, 500000) &&
                        st.getQuestItemsCount(2697) == 2 && st.getQuestItemsCount(2695) == 5)
                    st.set("cond", "17");
                break;
            case 20554:
                if (st.hasQuestItems(2705))
                    st.dropItems(2708, 1, 1, 300000);
                break;
            case 20158:
                if (st.getInt("cond") == 28 && st.dropItemsAlways(2717, 1, 12) &&
                        st.getQuestItemsCount(2716) == 10 && st.getQuestItemsCount(2718) == 5 && st.getQuestItemsCount(2719) == 5)
                    st.set("cond", "29");
                break;
            case 20201:
                if (st.getInt("cond") == 28 && st.dropItemsAlways(2716, 1, 10) &&
                        st.getQuestItemsCount(2717) == 12 && st.getQuestItemsCount(2718) == 5 && st.getQuestItemsCount(2719) == 5)
                    st.set("cond", "29");
                break;
            case 20552:
                if (st.getInt("cond") == 28 && st.dropItemsAlways(2718, 1, 5) &&
                        st.getQuestItemsCount(2717) == 12 && st.getQuestItemsCount(2716) == 10 && st.getQuestItemsCount(2719) == 5)
                    st.set("cond", "29");
                break;
            case 20567:
                if (st.getInt("cond") == 28 && st.dropItemsAlways(2719, 1, 5) &&
                        st.getQuestItemsCount(2717) == 12 && st.getQuestItemsCount(2716) == 10 && st.getQuestItemsCount(2718) == 5)
                    st.set("cond", "29");
                break;
        }
        return null;
    }
}
