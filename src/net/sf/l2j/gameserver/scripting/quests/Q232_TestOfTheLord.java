package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q232_TestOfTheLord extends Quest {
    private static final String qn = "Q232_TestOfTheLord";

    private static final int SOMAK = 30510;

    private static final int MANAKIA = 30515;

    private static final int JAKAL = 30558;

    private static final int SUMARI = 30564;

    private static final int KAKAI = 30565;

    private static final int VARKEES = 30566;

    private static final int TANTUS = 30567;

    private static final int HATOS = 30568;

    private static final int TAKUNA = 30641;

    private static final int CHIANTA = 30642;

    private static final int FIRST_ORC = 30643;

    private static final int ANCESTOR_MARTANKUS = 30649;

    private static final int ORDEAL_NECKLACE = 3391;

    private static final int VARKEES_CHARM = 3392;

    private static final int TANTUS_CHARM = 3393;

    private static final int HATOS_CHARM = 3394;

    private static final int TAKUNA_CHARM = 3395;

    private static final int CHIANTA_CHARM = 3396;

    private static final int MANAKIAS_ORDERS = 3397;

    private static final int BREKA_ORC_FANG = 3398;

    private static final int MANAKIAS_AMULET = 3399;

    private static final int HUGE_ORC_FANG = 3400;

    private static final int SUMARIS_LETTER = 3401;

    private static final int URUTU_BLADE = 3402;

    private static final int TIMAK_ORC_SKULL = 3403;

    private static final int SWORD_INTO_SKULL = 3404;

    private static final int NERUGA_AXE_BLADE = 3405;

    private static final int AXE_OF_CEREMONY = 3406;

    private static final int MARSH_SPIDER_FEELER = 3407;

    private static final int MARSH_SPIDER_FEET = 3408;

    private static final int HANDIWORK_SPIDER_BROOCH = 3409;

    private static final int MONSTEREYE_CORNEA = 3410;

    private static final int MONSTEREYE_WOODCARVING = 3411;

    private static final int BEAR_FANG_NECKLACE = 3412;

    private static final int MARTANKUS_CHARM = 3413;

    private static final int RAGNA_ORC_HEAD = 3414;

    private static final int RAGNA_CHIEF_NOTICE = 3415;

    private static final int BONE_ARROW = 1341;

    private static final int IMMORTAL_FLAME = 3416;

    private static final int MARK_LORD = 3390;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static Npc _firstOrc;

    public Q232_TestOfTheLord() {
        super(232, "Test of the Lord");
        setItemsIds(3392, 3393, 3394, 3395, 3396, 3397, 3398, 3399, 3400, 3401,
                3402, 3403, 3404, 3405, 3406, 3407, 3408, 3409, 3410, 3411,
                3412, 3413, 3414, 3415, 3416);
        addStartNpc(30565);
        addTalkId(30565, 30642, 30568, 30510, 30564, 30641, 30567, 30558, 30566, 30515,
                30649, 30643);
        addKillId(20233, 20269, 20270, 20564, 20583, 20584, 20585, 20586, 20587, 20588,
                20778, 20779);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q232_TestOfTheLord");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30565-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3391, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30565-05b.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30565-08.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3404, 1);
            st.takeItems(3406, 1);
            st.takeItems(3411, 1);
            st.takeItems(3409, 1);
            st.takeItems(3391, 1);
            st.takeItems(3400, 1);
            st.giveItems(3412, 1);
        } else if (event.equalsIgnoreCase("30566-02.htm")) {
            st.giveItems(3392, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30567-02.htm")) {
            st.giveItems(3393, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30558-02.htm")) {
            st.takeItems(57, 1000);
            st.giveItems(3405, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30568-02.htm")) {
            st.giveItems(3394, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30641-02.htm")) {
            st.giveItems(3395, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30642-02.htm")) {
            st.giveItems(3396, 1);
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30643-02.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            startQuestTimer("f_orc_despawn", 10000L, null, player, false);
        } else if (event.equalsIgnoreCase("30649-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3412, 1);
            st.giveItems(3413, 1);
        } else if (event.equalsIgnoreCase("30649-07.htm")) {
            if (_firstOrc == null)
                _firstOrc = addSpawn(30643, 21036, -107690, -3038, 200000, false, 0L, true);
        } else if (event.equalsIgnoreCase("f_orc_despawn")) {
            if (_firstOrc != null) {
                _firstOrc.deleteMe();
                _firstOrc = null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q232_TestOfTheLord");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30565-01.htm";
                    break;
                }
                if (player.getClassId() != ClassId.ORC_SHAMAN) {
                    htmltext = "30565-02.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30565-03.htm";
                    break;
                }
                htmltext = "30565-04.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30566:
                        if (st.hasQuestItems(3400)) {
                            htmltext = "30566-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3392)) {
                            if (st.hasQuestItems(3399)) {
                                htmltext = "30566-04.htm";
                                st.takeItems(3392, -1);
                                st.takeItems(3399, -1);
                                st.giveItems(3400, 1);
                                if (st.hasQuestItems(3404, 3406, 3411, 3409, 3391)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30566-03.htm";
                            break;
                        }
                        htmltext = "30566-01.htm";
                        break;
                    case 30515:
                        if (st.hasQuestItems(3400)) {
                            htmltext = "30515-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3399)) {
                            htmltext = "30515-04.htm";
                            break;
                        }
                        if (st.hasQuestItems(3397)) {
                            if (st.getQuestItemsCount(3398) >= 20) {
                                htmltext = "30515-03.htm";
                                st.takeItems(3397, -1);
                                st.takeItems(3398, -1);
                                st.giveItems(3399, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30515-02.htm";
                            break;
                        }
                        htmltext = "30515-01.htm";
                        st.giveItems(3397, 1);
                        st.playSound("ItemSound.quest_itemget");
                        break;
                    case 30567:
                        if (st.hasQuestItems(3406)) {
                            htmltext = "30567-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3393)) {
                            if (st.getQuestItemsCount(1341) >= 1000) {
                                htmltext = "30567-04.htm";
                                st.takeItems(1341, 1000);
                                st.takeItems(3405, 1);
                                st.takeItems(3393, 1);
                                st.giveItems(3406, 1);
                                if (st.hasQuestItems(3404, 3411, 3409, 3391, 3400)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30567-03.htm";
                            break;
                        }
                        htmltext = "30567-01.htm";
                        break;
                    case 30558:
                        if (st.hasQuestItems(3406)) {
                            htmltext = "30558-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3405)) {
                            htmltext = "30558-04.htm";
                            break;
                        }
                        if (st.hasQuestItems(3393)) {
                            if (st.getQuestItemsCount(57) >= 1000) {
                                htmltext = "30558-01.htm";
                                break;
                            }
                            htmltext = "30558-03.htm";
                        }
                        break;
                    case 30568:
                        if (st.hasQuestItems(3404)) {
                            htmltext = "30568-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3394)) {
                            if (st.hasQuestItems(3402) && st.getQuestItemsCount(3403) >= 10) {
                                htmltext = "30568-04.htm";
                                st.takeItems(3394, 1);
                                st.takeItems(3402, 1);
                                st.takeItems(3403, -1);
                                st.giveItems(3404, 1);
                                if (st.hasQuestItems(3406, 3411, 3409, 3391, 3400)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30568-03.htm";
                            break;
                        }
                        htmltext = "30568-01.htm";
                        break;
                    case 30564:
                        if (st.hasQuestItems(3402)) {
                            htmltext = "30564-03.htm";
                            break;
                        }
                        if (st.hasQuestItems(3401)) {
                            htmltext = "30564-02.htm";
                            break;
                        }
                        if (st.hasQuestItems(3394)) {
                            htmltext = "30564-01.htm";
                            st.giveItems(3401, 1);
                            st.playSound("ItemSound.quest_itemget");
                        }
                        break;
                    case 30510:
                        if (st.hasQuestItems(3404)) {
                            htmltext = "30510-03.htm";
                            break;
                        }
                        if (st.hasQuestItems(3402)) {
                            htmltext = "30510-02.htm";
                            break;
                        }
                        if (st.hasQuestItems(3401)) {
                            htmltext = "30510-01.htm";
                            st.takeItems(3401, 1);
                            st.giveItems(3402, 1);
                            st.playSound("ItemSound.quest_itemget");
                        }
                        break;
                    case 30641:
                        if (st.hasQuestItems(3409)) {
                            htmltext = "30641-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3395)) {
                            if (st.getQuestItemsCount(3407) >= 10 && st.getQuestItemsCount(3408) >= 10) {
                                htmltext = "30641-04.htm";
                                st.takeItems(3407, -1);
                                st.takeItems(3408, -1);
                                st.takeItems(3395, 1);
                                st.giveItems(3409, 1);
                                if (st.hasQuestItems(3404, 3406, 3411, 3391, 3400)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30641-03.htm";
                            break;
                        }
                        htmltext = "30641-01.htm";
                        break;
                    case 30642:
                        if (st.hasQuestItems(3411)) {
                            htmltext = "30642-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(3396)) {
                            if (st.getQuestItemsCount(3410) >= 20) {
                                htmltext = "30642-04.htm";
                                st.takeItems(3410, -1);
                                st.takeItems(3396, 1);
                                st.giveItems(3411, 1);
                                if (st.hasQuestItems(3404, 3406, 3409, 3391, 3400)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30642-03.htm";
                            break;
                        }
                        htmltext = "30642-01.htm";
                        break;
                    case 30565:
                        if (cond == 1) {
                            htmltext = "30565-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30565-07.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30565-09.htm";
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30565-10.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30565-11.htm";
                            st.takeItems(3416, 1);
                            st.giveItems(3390, 1);
                            st.rewardExpAndSp(92955L, 16250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30649:
                        if (cond == 3) {
                            htmltext = "30649-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30649-05.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30649-06.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3413, 1);
                            st.takeItems(3414, 1);
                            st.takeItems(3415, 1);
                            st.giveItems(3416, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30649-07.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30649-08.htm";
                        break;
                    case 30643:
                        if (cond == 6) {
                            htmltext = "30643-01.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30643-03.htm";
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
            case 20564:
                if (st.hasQuestItems(3396))
                    st.dropItemsAlways(3410, 1, 20);
                break;
            case 20583:
            case 20584:
            case 20585:
                if (st.hasQuestItems(3394))
                    st.dropItems(3403, 1, 10, 710000);
                break;
            case 20586:
                if (st.hasQuestItems(3394))
                    st.dropItems(3403, 1, 10, 810000);
                break;
            case 20587:
            case 20588:
                if (st.hasQuestItems(3394))
                    st.dropItemsAlways(3403, 1, 10);
                break;
            case 20233:
                if (st.hasQuestItems(3395))
                    st.dropItemsAlways((st.getQuestItemsCount(3407) >= 10) ? 3408 : 3407, 1, 10);
                break;
            case 20269:
                if (st.hasQuestItems(3397))
                    st.dropItems(3398, 1, 20, 410000);
                break;
            case 20270:
                if (st.hasQuestItems(3397))
                    st.dropItems(3398, 1, 20, 510000);
                break;
            case 20778:
            case 20779:
                if (st.hasQuestItems(3413)) {
                    if (!st.hasQuestItems(3415)) {
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(3415, 1);
                        break;
                    }
                    if (!st.hasQuestItems(3414)) {
                        st.set("cond", "5");
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(3414, 1);
                    }
                }
                break;
        }
        return null;
    }
}
