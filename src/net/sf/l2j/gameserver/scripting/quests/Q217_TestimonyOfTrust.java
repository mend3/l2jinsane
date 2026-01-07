package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q217_TestimonyOfTrust extends Quest {
    private static final String qn = "Q217_TestimonyOfTrust";

    private static final int LETTER_TO_ELF = 2735;

    private static final int LETTER_TO_DARK_ELF = 2736;

    private static final int LETTER_TO_DWARF = 2737;

    private static final int LETTER_TO_ORC = 2738;

    private static final int LETTER_TO_SERESIN = 2739;

    private static final int SCROLL_OF_DARK_ELF_TRUST = 2740;

    private static final int SCROLL_OF_ELF_TRUST = 2741;

    private static final int SCROLL_OF_DWARF_TRUST = 2742;

    private static final int SCROLL_OF_ORC_TRUST = 2743;

    private static final int RECOMMENDATION_OF_HOLLINT = 2744;

    private static final int ORDER_OF_ASTERIOS = 2745;

    private static final int BREATH_OF_WINDS = 2746;

    private static final int SEED_OF_VERDURE = 2747;

    private static final int LETTER_FROM_THIFIELL = 2748;

    private static final int BLOOD_GUARDIAN_BASILIK = 2749;

    private static final int GIANT_APHID = 2750;

    private static final int STAKATO_FLUIDS = 2751;

    private static final int BASILIK_PLASMA = 2752;

    private static final int HONEY_DEW = 2753;

    private static final int STAKATO_ICHOR = 2754;

    private static final int ORDER_OF_CLAYTON = 2755;

    private static final int PARASITE_OF_LOTA = 2756;

    private static final int LETTER_TO_MANAKIA = 2757;

    private static final int LETTER_OF_MANAKIA = 2758;

    private static final int LETTER_TO_NIKOLA = 2759;

    private static final int ORDER_OF_NIKOLA = 2760;

    private static final int HEARTSTONE_OF_PORTA = 2761;

    private static final int MARK_OF_TRUST = 2734;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int HOLLINT = 30191;

    private static final int ASTERIOS = 30154;

    private static final int THIFIELL = 30358;

    private static final int CLAYTON = 30464;

    private static final int SERESIN = 30657;

    private static final int KAKAI = 30565;

    private static final int MANAKIA = 30515;

    private static final int LOCKIRIN = 30531;

    private static final int NIKOLA = 30621;

    private static final int BIOTIN = 30031;

    private static final int DRYAD = 20013;

    private static final int DRYAD_ELDER = 20019;

    private static final int LIREIN = 20036;

    private static final int LIREIN_ELDER = 20044;

    private static final int ACTEA_OF_VERDANT_WILDS = 27121;

    private static final int LUELL_OF_ZEPHYR_WINDS = 27120;

    private static final int GUARDIAN_BASILIK = 20550;

    private static final int ANT_RECRUIT = 20082;

    private static final int ANT_PATROL = 20084;

    private static final int ANT_GUARD = 20086;

    private static final int ANT_SOLDIER = 20087;

    private static final int ANT_WARRIOR_CAPTAIN = 20088;

    private static final int MARSH_STAKATO = 20157;

    private static final int MARSH_STAKATO_WORKER = 20230;

    private static final int MARSH_STAKATO_SOLDIER = 20232;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int WINDSUS = 20553;

    private static final int PORTA = 20213;

    public Q217_TestimonyOfTrust() {
        super(217, "Testimony of Trust");
        setItemsIds(2735, 2736, 2737, 2738, 2739, 2740, 2741, 2742, 2743, 2744,
                2745, 2746, 2747, 2748, 2749, 2750, 2751, 2752, 2753, 2754,
                2755, 2756, 2757, 2758, 2759, 2760, 2761);
        addStartNpc(30191);
        addTalkId(30191, 30154, 30358, 30464, 30657, 30565, 30515, 30531, 30621, 30031);
        addKillId(20013, 20019, 20036, 20044, 27121, 27120, 20550, 20082, 20084, 20086,
                20087, 20088, 20157, 20230, 20232, 20234, 20553, 20213);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q217_TestimonyOfTrust");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30191-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2735, 1);
            st.giveItems(2736, 1);
            if (!player.getMemos().getBool("secondClassChange37", false)) {
                htmltext = "30191-04a.htm";
                st.giveItems(7562, DF_REWARD_37.get(Integer.valueOf(player.getRace().ordinal())));
                player.getMemos().set("secondClassChange37", true);
            }
        } else if (event.equalsIgnoreCase("30154-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2735, 1);
            st.giveItems(2745, 1);
        } else if (event.equalsIgnoreCase("30358-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2736, 1);
            st.giveItems(2748, 1);
        } else if (event.equalsIgnoreCase("30515-02.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2757, 1);
        } else if (event.equalsIgnoreCase("30531-02.htm")) {
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2737, 1);
            st.giveItems(2759, 1);
        } else if (event.equalsIgnoreCase("30565-02.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2738, 1);
            st.giveItems(2757, 1);
        } else if (event.equalsIgnoreCase("30621-02.htm")) {
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2759, 1);
            st.giveItems(2760, 1);
        } else if (event.equalsIgnoreCase("30657-03.htm")) {
            if (player.getLevel() < 38) {
                htmltext = "30657-02.htm";
                if (st.getInt("cond") == 10) {
                    st.set("cond", "11");
                    st.playSound("ItemSound.quest_middle");
                }
            } else {
                st.set("cond", "12");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(2739, 1);
                st.giveItems(2737, 1);
                st.giveItems(2738, 1);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q217_TestimonyOfTrust");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId().level() != 1) {
                    htmltext = "30191-01a.htm";
                    break;
                }
                if (player.getRace() != ClassRace.HUMAN) {
                    htmltext = "30191-02.htm";
                    break;
                }
                if (player.getLevel() < 37) {
                    htmltext = "30191-01.htm";
                    break;
                }
                htmltext = "30191-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30191:
                        if (cond < 9) {
                            htmltext = "30191-08.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30191-05.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2740, 1);
                            st.takeItems(2741, 1);
                            st.giveItems(2739, 1);
                            break;
                        }
                        if (cond > 9 && cond < 22) {
                            htmltext = "30191-09.htm";
                            break;
                        }
                        if (cond == 22) {
                            htmltext = "30191-06.htm";
                            st.set("cond", "23");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2742, 1);
                            st.takeItems(2743, 1);
                            st.giveItems(2744, 1);
                            break;
                        }
                        if (cond == 23)
                            htmltext = "30191-07.htm";
                        break;
                    case 30154:
                        if (cond == 1) {
                            htmltext = "30154-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30154-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30154-05.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2746, 1);
                            st.takeItems(2747, 1);
                            st.takeItems(2745, 1);
                            st.giveItems(2741, 1);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30154-06.htm";
                        break;
                    case 30358:
                        if (cond == 4) {
                            htmltext = "30358-01.htm";
                            break;
                        }
                        if (cond > 4 && cond < 8) {
                            htmltext = "30358-05.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30358-03.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2752, 1);
                            st.takeItems(2753, 1);
                            st.takeItems(2754, 1);
                            st.giveItems(2740, 1);
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30358-04.htm";
                        break;
                    case 30464:
                        if (cond == 5) {
                            htmltext = "30464-01.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2748, 1);
                            st.giveItems(2755, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30464-02.htm";
                            break;
                        }
                        if (cond > 6) {
                            htmltext = "30464-03.htm";
                            if (cond == 7) {
                                st.set("cond", "8");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(2755, 1);
                            }
                        }
                        break;
                    case 30657:
                        if (cond == 10 || cond == 11) {
                            htmltext = "30657-01.htm";
                            break;
                        }
                        if (cond > 11 && cond < 22) {
                            htmltext = "30657-04.htm";
                            break;
                        }
                        if (cond == 22)
                            htmltext = "30657-05.htm";
                        break;
                    case 30565:
                        if (cond == 12) {
                            htmltext = "30565-01.htm";
                            break;
                        }
                        if (cond > 12 && cond < 16) {
                            htmltext = "30565-03.htm";
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30565-04.htm";
                            st.set("cond", "17");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2758, 1);
                            st.giveItems(2743, 1);
                            break;
                        }
                        if (cond > 16)
                            htmltext = "30565-05.htm";
                        break;
                    case 30515:
                        if (cond == 13) {
                            htmltext = "30515-01.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30515-03.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30515-04.htm";
                            st.set("cond", "16");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2756, -1);
                            st.giveItems(2758, 1);
                            break;
                        }
                        if (cond > 15)
                            htmltext = "30515-05.htm";
                        break;
                    case 30531:
                        if (cond == 17) {
                            htmltext = "30531-01.htm";
                            break;
                        }
                        if (cond > 17 && cond < 21) {
                            htmltext = "30531-03.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "30531-04.htm";
                            st.set("cond", "22");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2742, 1);
                            break;
                        }
                        if (cond == 22)
                            htmltext = "30531-05.htm";
                        break;
                    case 30621:
                        if (cond == 18) {
                            htmltext = "30621-01.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "30621-03.htm";
                            break;
                        }
                        if (cond == 20) {
                            htmltext = "30621-04.htm";
                            st.set("cond", "21");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2761, -1);
                            st.takeItems(2760, 1);
                            break;
                        }
                        if (cond > 20)
                            htmltext = "30621-05.htm";
                        break;
                    case 30031:
                        if (cond == 23) {
                            htmltext = "30031-01.htm";
                            st.takeItems(2744, 1);
                            st.giveItems(2734, 1);
                            st.rewardExpAndSp(39571L, 2500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
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
            case 20013:
            case 20019:
                if (st.getInt("cond") == 2 && !st.hasQuestItems(2747) && Rnd.get(100) < 33) {
                    addSpawn(27121, npc, true, 200000L, true);
                    st.playSound("Itemsound.quest_before_battle");
                }
                break;
            case 20036:
            case 20044:
                if (st.getInt("cond") == 2 && !st.hasQuestItems(2746) && Rnd.get(100) < 33) {
                    addSpawn(27120, npc, true, 200000L, true);
                    st.playSound("Itemsound.quest_before_battle");
                }
                break;
            case 27121:
                if (st.getInt("cond") == 2 && !st.hasQuestItems(2747)) {
                    st.giveItems(2747, 1);
                    if (st.hasQuestItems(2746)) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 27120:
                if (st.getInt("cond") == 2 && !st.hasQuestItems(2746)) {
                    st.giveItems(2746, 1);
                    if (st.hasQuestItems(2747)) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20157:
            case 20230:
            case 20232:
            case 20234:
                if (st.getInt("cond") == 6 && !st.hasQuestItems(2754) && st.dropItemsAlways(2751, 1, 10)) {
                    st.takeItems(2751, -1);
                    st.giveItems(2754, 1);
                    if (st.hasQuestItems(2752, 2753))
                        st.set("cond", "7");
                }
                break;
            case 20082:
            case 20084:
            case 20086:
            case 20087:
            case 20088:
                if (st.getInt("cond") == 6 && !st.hasQuestItems(2753) && st.dropItemsAlways(2750, 1, 10)) {
                    st.takeItems(2750, -1);
                    st.giveItems(2753, 1);
                    if (st.hasQuestItems(2752, 2754))
                        st.set("cond", "7");
                }
                break;
            case 20550:
                if (st.getInt("cond") == 6 && !st.hasQuestItems(2752) && st.dropItemsAlways(2749, 1, 10)) {
                    st.takeItems(2749, -1);
                    st.giveItems(2752, 1);
                    if (st.hasQuestItems(2753, 2754))
                        st.set("cond", "7");
                }
                break;
            case 20553:
                if (st.getInt("cond") == 14 && st.dropItems(2756, 1, 10, 500000))
                    st.set("cond", "15");
                break;
            case 20213:
                if (st.getInt("cond") == 19 && st.dropItemsAlways(2761, 1, 10))
                    st.set("cond", "20");
                break;
        }
        return null;
    }
}
