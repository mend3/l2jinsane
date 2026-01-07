package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q221_TestimonyOfProsperity extends Quest {
    private static final String qn = "Q221_TestimonyOfProsperity";

    private static final int ADENA = 57;

    private static final int ANIMAL_SKIN = 1867;

    private static final int RECIPE_TITAN_KEY = 3023;

    private static final int KEY_OF_TITAN = 3030;

    private static final int RING_OF_TESTIMONY_1 = 3239;

    private static final int RING_OF_TESTIMONY_2 = 3240;

    private static final int OLD_ACCOUNT_BOOK = 3241;

    private static final int BLESSED_SEED = 3242;

    private static final int EMILY_RECIPE = 3243;

    private static final int LILITH_ELVEN_WAFER = 3244;

    private static final int MAPHR_TABLET_FRAGMENT = 3245;

    private static final int COLLECTION_LICENSE = 3246;

    private static final int LOCKIRIN_NOTICE_1 = 3247;

    private static final int LOCKIRIN_NOTICE_2 = 3248;

    private static final int LOCKIRIN_NOTICE_3 = 3249;

    private static final int LOCKIRIN_NOTICE_4 = 3250;

    private static final int LOCKIRIN_NOTICE_5 = 3251;

    private static final int CONTRIBUTION_OF_SHARI = 3252;

    private static final int CONTRIBUTION_OF_MION = 3253;

    private static final int CONTRIBUTION_OF_MARYSE = 3254;

    private static final int MARYSE_REQUEST = 3255;

    private static final int CONTRIBUTION_OF_TOMA = 3256;

    private static final int RECEIPT_OF_BOLTER = 3257;

    private static final int RECEIPT_OF_CONTRIBUTION_1 = 3258;

    private static final int RECEIPT_OF_CONTRIBUTION_2 = 3259;

    private static final int RECEIPT_OF_CONTRIBUTION_3 = 3260;

    private static final int RECEIPT_OF_CONTRIBUTION_4 = 3261;

    private static final int RECEIPT_OF_CONTRIBUTION_5 = 3262;

    private static final int PROCURATION_OF_TOROCCO = 3263;

    private static final int BRIGHT_LIST = 3264;

    private static final int MANDRAGORA_PETAL = 3265;

    private static final int CRIMSON_MOSS = 3266;

    private static final int MANDRAGORA_BOUQUET = 3267;

    private static final int PARMAN_INSTRUCTIONS = 3268;

    private static final int PARMAN_LETTER = 3269;

    private static final int CLAY_DOUGH = 3270;

    private static final int PATTERN_OF_KEYHOLE = 3271;

    private static final int NIKOLAS_LIST = 3272;

    private static final int STAKATO_SHELL = 3273;

    private static final int TOAD_LORD_SAC = 3274;

    private static final int SPIDER_THORN = 3275;

    private static final int CRYSTAL_BROOCH = 3428;

    private static final int MARK_OF_PROSPERITY = 3238;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int WILFORD = 30005;

    private static final int PARMAN = 30104;

    private static final int LILITH = 30368;

    private static final int BRIGHT = 30466;

    private static final int SHARI = 30517;

    private static final int MION = 30519;

    private static final int LOCKIRIN = 30531;

    private static final int SPIRON = 30532;

    private static final int BALANKI = 30533;

    private static final int KEEF = 30534;

    private static final int FILAUR = 30535;

    private static final int ARIN = 30536;

    private static final int MARYSE_REDBONNET = 30553;

    private static final int BOLTER = 30554;

    private static final int TOROCCO = 30555;

    private static final int TOMA = 30556;

    private static final int PIOTUR = 30597;

    private static final int EMILY = 30620;

    private static final int NIKOLA = 30621;

    private static final int BOX_OF_TITAN = 30622;

    private static final int MANDRAGORA_SPROUT_1 = 20223;

    private static final int MANDRAGORA_SPROUT_2 = 20154;

    private static final int MANDRAGORA_SAPLING = 20155;

    private static final int MANDRAGORA_BLOSSOM = 20156;

    private static final int MARSH_STAKATO = 20157;

    private static final int GIANT_CRIMSON_ANT = 20228;

    private static final int MARSH_STAKATO_WORKER = 20230;

    private static final int TOAD_LORD = 20231;

    private static final int MARSH_STAKATO_SOLDIER = 20232;

    private static final int MARSH_SPIDER = 20233;

    private static final int MARSH_STAKATO_DRONE = 20234;

    public Q221_TestimonyOfProsperity() {
        super(221, "Testimony Of Prosperity");
        setItemsIds(3239, 3240, 3241, 3242, 3243, 3244, 3245, 3246, 3247, 3248,
                3249, 3250, 3251, 3252, 3253, 3254, 3255, 3256, 3257, 3258,
                3259, 3260, 3261, 3262, 3263, 3264, 3265, 3266, 3267, 3268,
                3269, 3270, 3271, 3272, 3273, 3274, 3275, 3428);
        addStartNpc(30104);
        addTalkId(30005, 30104, 30368, 30466, 30517, 30519, 30531, 30532, 30533, 30534,
                30535, 30536, 30553, 30554, 30555, 30556, 30597, 30620, 30621, 30622);
        addKillId(20223, 20155, 20156, 20157, 20154, 20228, 20230, 20231, 20232, 20233,
                20234);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q221_TestimonyOfProsperity");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30104-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3239, 1);
            if (!player.getMemos().getBool("secondClassChange37", false)) {
                htmltext = "30104-04e.htm";
                st.giveItems(7562, DF_REWARD_37.get(player.getRace().ordinal()));
                player.getMemos().set("secondClassChange37", true);
            }
        } else if (event.equalsIgnoreCase("30104-07.htm")) {
            st.takeItems(3242, 1);
            st.takeItems(3243, 1);
            st.takeItems(3244, 1);
            st.takeItems(3241, 1);
            st.takeItems(3239, 1);
            st.playSound("ItemSound.quest_middle");
            if (player.getLevel() < 38) {
                st.set("cond", "3");
                st.giveItems(3268, 1);
            } else {
                htmltext = "30104-08.htm";
                st.set("cond", "4");
                st.giveItems(3269, 1);
                st.giveItems(3240, 1);
            }
        } else if (event.equalsIgnoreCase("30531-02.htm") && st.hasQuestItems(3246)) {
            htmltext = "30531-04.htm";
        } else if (event.equalsIgnoreCase("30531-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3246, 1);
            st.giveItems(3247, 1);
            st.giveItems(3248, 1);
            st.giveItems(3249, 1);
            st.giveItems(3250, 1);
            st.giveItems(3251, 1);
        } else if (event.equalsIgnoreCase("30534-03a.htm") && st.getQuestItemsCount(57) >= 5000) {
            htmltext = "30534-03b.htm";
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(57, 5000);
            st.takeItems(3263, 1);
            st.giveItems(3260, 1);
        } else if (event.equalsIgnoreCase("30005-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3428, 1);
        } else if (event.equalsIgnoreCase("30466-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3264, 1);
        } else if (event.equalsIgnoreCase("30555-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3263, 1);
        } else if (event.equalsIgnoreCase("30368-03.htm")) {
            st.takeItems(3428, 1);
            st.giveItems(3244, 1);
            if (st.hasQuestItems(3242, 3241, 3243)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30597-02.htm")) {
            st.giveItems(3242, 1);
            if (st.hasQuestItems(3241, 3243, 3244)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30620-03.htm")) {
            st.takeItems(3267, 1);
            st.giveItems(3243, 1);
            if (st.hasQuestItems(3242, 3241, 3244)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30621-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3270, 1);
        } else if (event.equalsIgnoreCase("30622-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3270, 1);
            st.giveItems(3271, 1);
        } else if (event.equalsIgnoreCase("30622-04.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3030, 1);
            st.takeItems(3272, 1);
            st.takeItems(3023, 1);
            st.takeItems(3273, 20);
            st.takeItems(3275, 10);
            st.takeItems(3274, 10);
            st.giveItems(3245, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q221_TestimonyOfProsperity");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DWARF) {
                    htmltext = "30104-01.htm";
                    break;
                }
                if (player.getLevel() < 37) {
                    htmltext = "30104-02.htm";
                    break;
                }
                if (player.getClassId().level() != 1) {
                    htmltext = "30104-01a.htm";
                    break;
                }
                htmltext = "30104-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30104:
                        if (cond == 1) {
                            htmltext = "30104-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30104-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            if (player.getLevel() < 38) {
                                htmltext = "30104-09.htm";
                                break;
                            }
                            htmltext = "30104-10.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3268, 1);
                            st.giveItems(3269, 1);
                            st.giveItems(3240, 1);
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30104-11.htm";
                            break;
                        }
                        if (cond == 7 || cond == 8) {
                            htmltext = "30104-12.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30104-13.htm";
                            st.takeItems(3245, 1);
                            st.takeItems(3240, 1);
                            st.giveItems(3238, 1);
                            st.rewardExpAndSp(12969L, 1000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30531:
                        if (cond == 1 || cond == 2) {
                            if (st.hasQuestItems(3246)) {
                                if (st.hasQuestItems(3258, 3259, 3260, 3261, 3262)) {
                                    htmltext = "30531-05.htm";
                                    st.takeItems(3246, 1);
                                    st.takeItems(3258, 1);
                                    st.takeItems(3259, 1);
                                    st.takeItems(3260, 1);
                                    st.takeItems(3261, 1);
                                    st.takeItems(3262, 1);
                                    st.giveItems(3241, 1);
                                    if (st.hasQuestItems(3242, 3243, 3244)) {
                                        st.set("cond", "2");
                                        st.playSound("ItemSound.quest_middle");
                                        break;
                                    }
                                    st.playSound("ItemSound.quest_itemget");
                                    break;
                                }
                                htmltext = "30531-04.htm";
                                break;
                            }
                            htmltext = st.hasQuestItems(3241) ? "30531-06.htm" : "30531-01.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30531-07.htm";
                        break;
                    case 30532:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3247)) {
                                htmltext = "30532-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3247, 1);
                                break;
                            }
                            if (st.hasQuestItems(3252)) {
                                htmltext = "30532-03.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3252, 1);
                                st.giveItems(3258, 1);
                                break;
                            }
                            htmltext = st.hasQuestItems(3258) ? "30532-04.htm" : "30532-02.htm";
                        }
                        break;
                    case 30533:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3248)) {
                                htmltext = "30533-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3248, 1);
                                break;
                            }
                            if (st.hasQuestItems(3254, 3253)) {
                                htmltext = "30533-03.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3254, 1);
                                st.takeItems(3253, 1);
                                st.giveItems(3259, 1);
                                break;
                            }
                            htmltext = st.hasQuestItems(3259) ? "30533-04.htm" : "30533-02.htm";
                        }
                        break;
                    case 30534:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3249)) {
                                htmltext = "30534-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3249, 1);
                                break;
                            }
                            if (st.hasQuestItems(3263)) {
                                htmltext = "30534-03.htm";
                                break;
                            }
                            htmltext = st.hasQuestItems(3260) ? "30534-04.htm" : "30534-02.htm";
                        }
                        break;
                    case 30535:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3250)) {
                                htmltext = "30535-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3250, 1);
                                break;
                            }
                            if (st.hasQuestItems(3257)) {
                                htmltext = "30535-03.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3257, 1);
                                st.giveItems(3261, 1);
                                break;
                            }
                            htmltext = st.hasQuestItems(3261) ? "30535-04.htm" : "30535-02.htm";
                        }
                        break;
                    case 30536:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3251)) {
                                htmltext = "30536-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3251, 1);
                                break;
                            }
                            if (st.hasQuestItems(3256)) {
                                htmltext = "30536-03.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3256, 1);
                                st.giveItems(3262, 1);
                                break;
                            }
                            htmltext = st.hasQuestItems(3262) ? "30536-04.htm" : "30536-02.htm";
                        }
                        break;
                    case 30517:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3252)) {
                                htmltext = "30517-02.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3247, 3258)) {
                                htmltext = "30517-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(3252, 1);
                            }
                        }
                        break;
                    case 30519:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3253)) {
                                htmltext = "30519-02.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3248, 3259)) {
                                htmltext = "30519-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(3253, 1);
                            }
                        }
                        break;
                    case 30553:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3255)) {
                                if (st.getQuestItemsCount(1867) < 100) {
                                    htmltext = "30553-02.htm";
                                    break;
                                }
                                htmltext = "30553-03.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(1867, 100);
                                st.takeItems(3255, 1);
                                st.giveItems(3254, 1);
                                break;
                            }
                            if (st.hasQuestItems(3254)) {
                                htmltext = "30553-04.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3248, 3259)) {
                                htmltext = "30553-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(3255, 1);
                            }
                        }
                        break;
                    case 30555:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3263)) {
                                htmltext = "30555-03.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3249, 3260))
                                htmltext = "30555-01.htm";
                        }
                        break;
                    case 30554:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3257)) {
                                htmltext = "30554-02.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3250, 3261)) {
                                htmltext = "30554-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(3257, 1);
                            }
                        }
                        break;
                    case 30556:
                        if (cond == 1 && st.hasQuestItems(3246)) {
                            if (st.hasQuestItems(3256)) {
                                htmltext = "30556-02.htm";
                                break;
                            }
                            if (!st.hasAtLeastOneQuestItem(3251, 3262)) {
                                htmltext = "30556-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(3256, 1);
                            }
                        }
                        break;
                    case 30597:
                        if (cond == 1 || cond == 2) {
                            htmltext = st.hasQuestItems(3242) ? "30597-03.htm" : "30597-01.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30597-04.htm";
                        break;
                    case 30005:
                        if (cond == 1 || cond == 2) {
                            if (st.hasQuestItems(3244)) {
                                htmltext = "30005-06.htm";
                                break;
                            }
                            htmltext = st.hasQuestItems(3428) ? "30005-05.htm" : "30005-01.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30005-07.htm";
                        break;
                    case 30368:
                        if (cond == 1 || cond == 2) {
                            if (st.hasQuestItems(3428)) {
                                htmltext = "30368-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(3244))
                                htmltext = "30368-04.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30368-05.htm";
                        break;
                    case 30466:
                        if (cond == 1 || cond == 2) {
                            if (st.hasQuestItems(3243)) {
                                htmltext = "30466-07.htm";
                                break;
                            }
                            if (st.hasQuestItems(3267)) {
                                htmltext = "30466-06.htm";
                                break;
                            }
                            if (st.hasQuestItems(3264)) {
                                if (st.getQuestItemsCount(3266) + st.getQuestItemsCount(3265) < 30) {
                                    htmltext = "30466-04.htm";
                                    break;
                                }
                                htmltext = "30466-05.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3264, 1);
                                st.takeItems(3266, 10);
                                st.takeItems(3265, 20);
                                st.giveItems(3267, 1);
                                break;
                            }
                            htmltext = "30466-01.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30466-08.htm";
                        break;
                    case 30620:
                        if (cond == 1 || cond == 2) {
                            if (st.hasQuestItems(3243)) {
                                htmltext = "30620-04.htm";
                                break;
                            }
                            if (st.hasQuestItems(3267))
                                htmltext = "30620-01.htm";
                            break;
                        }
                        if (cond >= 4)
                            htmltext = "30620-05.htm";
                        break;
                    case 30621:
                        if (cond == 4) {
                            htmltext = "30621-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(3269, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30621-05.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30621-06.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3271, 1);
                            st.giveItems(3272, 1);
                            st.giveItems(3023, 1);
                            break;
                        }
                        if (cond == 7 || cond == 8) {
                            htmltext = st.hasQuestItems(3030) ? "30621-08.htm" : "30621-07.htm";
                            break;
                        }
                        if (cond == 9)
                            htmltext = "30621-09.htm";
                        break;
                    case 30622:
                        if (cond == 5) {
                            htmltext = "30622-01.htm";
                            break;
                        }
                        if (cond == 8 && st.hasQuestItems(3030)) {
                            htmltext = "30622-03.htm";
                            break;
                        }
                        htmltext = "30622-05.htm";
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
            case 20223:
                if (st.hasQuestItems(3264))
                    st.dropItems(3265, 1, 20, 300000);
                break;
            case 20154:
                if (st.hasQuestItems(3264))
                    st.dropItems(3265, 1, 20, 600000);
                break;
            case 20155:
                if (st.hasQuestItems(3264))
                    st.dropItems(3265, 1, 20, 800000);
                break;
            case 20156:
                if (st.hasQuestItems(3264))
                    st.dropItemsAlways(3265, 1, 20);
                break;
            case 20228:
                if (st.hasQuestItems(3264))
                    st.dropItemsAlways(3266, 1, 10);
                break;
            case 20157:
                if (cond == 7 && st.dropItems(3273, 1, 20, 200000) && st.getQuestItemsCount(3274) + st.getQuestItemsCount(3275) == 20)
                    st.set("cond", "8");
                break;
            case 20230:
                if (cond == 7 && st.dropItems(3273, 1, 20, 300000) && st.getQuestItemsCount(3274) + st.getQuestItemsCount(3275) == 20)
                    st.set("cond", "8");
                break;
            case 20232:
                if (cond == 7 && st.dropItems(3273, 1, 20, 500000) && st.getQuestItemsCount(3274) + st.getQuestItemsCount(3275) == 20)
                    st.set("cond", "8");
                break;
            case 20234:
                if (cond == 7 && st.dropItems(3273, 1, 20, 600000) && st.getQuestItemsCount(3274) + st.getQuestItemsCount(3275) == 20)
                    st.set("cond", "8");
                break;
            case 20231:
                if (cond == 7 && st.dropItems(3274, 1, 10, 200000) && st.getQuestItemsCount(3273) + st.getQuestItemsCount(3275) == 30)
                    st.set("cond", "8");
                break;
            case 20233:
                if (cond == 7 && st.dropItems(3275, 1, 10, 200000) && st.getQuestItemsCount(3273) + st.getQuestItemsCount(3274) == 30)
                    st.set("cond", "8");
                break;
        }
        return null;
    }
}
