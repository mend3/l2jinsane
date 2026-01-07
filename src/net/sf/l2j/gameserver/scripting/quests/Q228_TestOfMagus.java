package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q228_TestOfMagus extends Quest {
    private static final String qn = "Q228_TestOfMagus";

    private static final int RUKAL_LETTER = 2841;

    private static final int PARINA_LETTER = 2842;

    private static final int LILAC_CHARM = 2843;

    private static final int GOLDEN_SEED_1 = 2844;

    private static final int GOLDEN_SEED_2 = 2845;

    private static final int GOLDEN_SEED_3 = 2846;

    private static final int SCORE_OF_ELEMENTS = 2847;

    private static final int DAZZLING_DROP = 2848;

    private static final int FLAME_CRYSTAL = 2849;

    private static final int HARPY_FEATHER = 2850;

    private static final int WYRM_WINGBONE = 2851;

    private static final int WINDSUS_MANE = 2852;

    private static final int EN_MONSTEREYE_SHELL = 2853;

    private static final int EN_STONEGOLEM_POWDER = 2854;

    private static final int EN_IRONGOLEM_SCRAP = 2855;

    private static final int TONE_OF_WATER = 2856;

    private static final int TONE_OF_FIRE = 2857;

    private static final int TONE_OF_WIND = 2858;

    private static final int TONE_OF_EARTH = 2859;

    private static final int SALAMANDER_CHARM = 2860;

    private static final int SYLPH_CHARM = 2861;

    private static final int UNDINE_CHARM = 2862;

    private static final int SERPENT_CHARM = 2863;

    private static final int MARK_OF_MAGUS = 2840;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int PARINA = 30391;

    private static final int EARTH_SNAKE = 30409;

    private static final int FLAME_SALAMANDER = 30411;

    private static final int WIND_SYLPH = 30412;

    private static final int WATER_UNDINE = 30413;

    private static final int CASIAN = 30612;

    private static final int RUKAL = 30629;

    private static final int HARPY = 20145;

    private static final int MARSH_STAKATO = 20157;

    private static final int WYRM = 20176;

    private static final int MARSH_STAKATO_WORKER = 20230;

    private static final int TOAD_LORD = 20231;

    private static final int MARSH_STAKATO_SOLDIER = 20232;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int WINDSUS = 20553;

    private static final int ENCHANTED_MONSTEREYE = 20564;

    private static final int ENCHANTED_STONE_GOLEM = 20565;

    private static final int ENCHANTED_IRON_GOLEM = 20566;

    private static final int SINGING_FLOWER_PHANTASM = 27095;

    private static final int SINGING_FLOWER_NIGHTMARE = 27096;

    private static final int SINGING_FLOWER_DARKLING = 27097;

    private static final int GHOST_FIRE = 27098;

    public Q228_TestOfMagus() {
        super(228, "Test Of Magus");
        setItemsIds(2841, 2842, 2843, 2844, 2845, 2846, 2847, 2848, 2849, 2850,
                2851, 2852, 2853, 2854, 2855, 2856, 2857, 2858, 2859, 2860,
                2861, 2862, 2863);
        addStartNpc(30629);
        addTalkId(30391, 30409, 30411, 30412, 30413, 30612, 30629);
        addKillId(20145, 20157, 20176, 20230, 20231, 20232, 20234, 20553, 20564, 20565,
                20566, 27095, 27096, 27097, 27098);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q228_TestOfMagus");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30629-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2841, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30629-04a.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30629-10.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2844, 1);
            st.takeItems(2845, 1);
            st.takeItems(2846, 1);
            st.takeItems(2843, 1);
            st.giveItems(2847, 1);
        } else if (event.equalsIgnoreCase("30391-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2841, 1);
            st.giveItems(2842, 1);
        } else if (event.equalsIgnoreCase("30612-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2842, 1);
            st.giveItems(2843, 1);
        } else if (event.equalsIgnoreCase("30412-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2861, 1);
        } else if (event.equalsIgnoreCase("30409-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2863, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q228_TestOfMagus");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.ELVEN_WIZARD && player.getClassId() != ClassId.DARK_WIZARD) {
                    htmltext = "30629-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30629-02.htm";
                    break;
                }
                htmltext = "30629-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30629:
                        if (cond == 1) {
                            htmltext = "30629-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30629-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30629-07.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30629-08.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30629-11.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30629-12.htm";
                            st.takeItems(2847, 1);
                            st.takeItems(2859, 1);
                            st.takeItems(2857, 1);
                            st.takeItems(2856, 1);
                            st.takeItems(2858, 1);
                            st.giveItems(2840, 1);
                            st.rewardExpAndSp(139039L, 40000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30391:
                        if (cond == 1) {
                            htmltext = "30391-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30391-03.htm";
                            break;
                        }
                        if (cond == 3 || cond == 4) {
                            htmltext = "30391-04.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30391-05.htm";
                        break;
                    case 30612:
                        if (cond == 2) {
                            htmltext = "30612-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30612-03.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30612-04.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30612-05.htm";
                        break;
                    case 30413:
                        if (cond == 5) {
                            if (st.hasQuestItems(2862)) {
                                if (st.getQuestItemsCount(2848) < 20) {
                                    htmltext = "30413-02.htm";
                                    break;
                                }
                                htmltext = "30413-03.htm";
                                st.takeItems(2848, 20);
                                st.takeItems(2862, 1);
                                st.giveItems(2856, 1);
                                if (st.hasQuestItems(2857, 2858, 2859)) {
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (!st.hasQuestItems(2856)) {
                                htmltext = "30413-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(2862, 1);
                                break;
                            }
                            htmltext = "30413-04.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30413-04.htm";
                        break;
                    case 30411:
                        if (cond == 5) {
                            if (st.hasQuestItems(2860)) {
                                if (st.getQuestItemsCount(2849) < 5) {
                                    htmltext = "30411-02.htm";
                                    break;
                                }
                                htmltext = "30411-03.htm";
                                st.takeItems(2849, 5);
                                st.takeItems(2860, 1);
                                st.giveItems(2857, 1);
                                if (st.hasQuestItems(2856, 2858, 2859)) {
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (!st.hasQuestItems(2857)) {
                                htmltext = "30411-01.htm";
                                st.giveItems(2860, 1);
                                break;
                            }
                            htmltext = "30411-04.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30411-04.htm";
                        break;
                    case 30412:
                        if (cond == 5) {
                            if (st.hasQuestItems(2861)) {
                                if (st.getQuestItemsCount(2850) + st.getQuestItemsCount(2851) + st.getQuestItemsCount(2852) < 40) {
                                    htmltext = "30412-03.htm";
                                    break;
                                }
                                htmltext = "30412-04.htm";
                                st.takeItems(2850, 20);
                                st.takeItems(2861, 1);
                                st.takeItems(2852, 10);
                                st.takeItems(2851, 10);
                                st.giveItems(2858, 1);
                                if (st.hasQuestItems(2856, 2857, 2859)) {
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (!st.hasQuestItems(2858)) {
                                htmltext = "30412-01.htm";
                                break;
                            }
                            htmltext = "30412-05.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30412-05.htm";
                        break;
                    case 30409:
                        if (cond == 5) {
                            if (st.hasQuestItems(2863)) {
                                if (st.getQuestItemsCount(2853) + st.getQuestItemsCount(2854) + st.getQuestItemsCount(2855) < 30) {
                                    htmltext = "30409-04.htm";
                                    break;
                                }
                                htmltext = "30409-05.htm";
                                st.takeItems(2855, 10);
                                st.takeItems(2853, 10);
                                st.takeItems(2854, 10);
                                st.takeItems(2863, 1);
                                st.giveItems(2859, 1);
                                if (st.hasQuestItems(2856, 2857, 2858)) {
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (!st.hasQuestItems(2859)) {
                                htmltext = "30409-01.htm";
                                break;
                            }
                            htmltext = "30409-06.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30409-06.htm";
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
        if (cond == 3) {
            switch (npc.getNpcId()) {
                case 27095:
                    if (!st.hasQuestItems(2844)) {
                        npc.broadcastNpcSay("I am a tree of nothing... a tree... that knows where to return...");
                        st.dropItemsAlways(2844, 1, 1);
                        if (st.hasQuestItems(2845, 2846))
                            st.set("cond", "4");
                    }
                    break;
                case 27096:
                    if (!st.hasQuestItems(2845)) {
                        npc.broadcastNpcSay("I am a creature that shows the truth of the place deep in my heart...");
                        st.dropItemsAlways(2845, 1, 1);
                        if (st.hasQuestItems(2844, 2846))
                            st.set("cond", "4");
                    }
                    break;
                case 27097:
                    if (!st.hasQuestItems(2846)) {
                        npc.broadcastNpcSay("I am a mirror of darkness... a virtual image of darkness...");
                        st.dropItemsAlways(2846, 1, 1);
                        if (st.hasQuestItems(2844, 2845))
                            st.set("cond", "4");
                    }
                    break;
            }
        } else if (cond == 5) {
            switch (npc.getNpcId()) {
                case 27098:
                    if (st.hasQuestItems(2860))
                        st.dropItems(2849, 1, 5, 500000);
                    break;
                case 20157:
                case 20230:
                case 20231:
                    if (st.hasQuestItems(2862))
                        st.dropItems(2848, 1, 20, 300000);
                    break;
                case 20232:
                    if (st.hasQuestItems(2862))
                        st.dropItems(2848, 1, 20, 400000);
                    break;
                case 20234:
                    if (st.hasQuestItems(2862))
                        st.dropItems(2848, 1, 20, 500000);
                    break;
                case 20145:
                    if (st.hasQuestItems(2861))
                        st.dropItemsAlways(2850, 1, 20);
                    break;
                case 20176:
                    if (st.hasQuestItems(2861))
                        st.dropItems(2851, 1, 10, 500000);
                    break;
                case 20553:
                    if (st.hasQuestItems(2861))
                        st.dropItems(2852, 1, 10, 500000);
                    break;
                case 20564:
                    if (st.hasQuestItems(2863))
                        st.dropItemsAlways(2853, 1, 10);
                    break;
                case 20565:
                    if (st.hasQuestItems(2863))
                        st.dropItemsAlways(2854, 1, 10);
                    break;
                case 20566:
                    if (st.hasQuestItems(2863))
                        st.dropItemsAlways(2855, 1, 10);
                    break;
            }
        }
        return null;
    }
}
