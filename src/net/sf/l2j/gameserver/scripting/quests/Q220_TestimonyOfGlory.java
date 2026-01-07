package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q220_TestimonyOfGlory extends Quest {
    private static final String qn = "Q220_TestimonyOfGlory";

    private static final int VOKIAN_ORDER_1 = 3204;

    private static final int MANASHEN_SHARD = 3205;

    private static final int TYRANT_TALON = 3206;

    private static final int GUARDIAN_BASILISK_FANG = 3207;

    private static final int VOKIAN_ORDER_2 = 3208;

    private static final int NECKLACE_OF_AUTHORITY = 3209;

    private static final int CHIANTA_ORDER_1 = 3210;

    private static final int SCEPTER_OF_BREKA = 3211;

    private static final int SCEPTER_OF_ENKU = 3212;

    private static final int SCEPTER_OF_VUKU = 3213;

    private static final int SCEPTER_OF_TUREK = 3214;

    private static final int SCEPTER_OF_TUNATH = 3215;

    private static final int CHIANTA_ORDER_2 = 3216;

    private static final int CHIANTA_ORDER_3 = 3217;

    private static final int TAMLIN_ORC_SKULL = 3218;

    private static final int TIMAK_ORC_HEAD = 3219;

    private static final int SCEPTER_BOX = 3220;

    private static final int PASHIKA_HEAD = 3221;

    private static final int VULTUS_HEAD = 3222;

    private static final int GLOVE_OF_VOLTAR = 3223;

    private static final int ENKU_OVERLORD_HEAD = 3224;

    private static final int GLOVE_OF_KEPRA = 3225;

    private static final int MAKUM_BUGBEAR_HEAD = 3226;

    private static final int GLOVE_OF_BURAI = 3227;

    private static final int MANAKIA_LETTER_1 = 3228;

    private static final int MANAKIA_LETTER_2 = 3229;

    private static final int KASMAN_LETTER_1 = 3230;

    private static final int KASMAN_LETTER_2 = 3231;

    private static final int KASMAN_LETTER_3 = 3232;

    private static final int DRIKO_CONTRACT = 3233;

    private static final int STAKATO_DRONE_HUSK = 3234;

    private static final int TANAPI_ORDER = 3235;

    private static final int SCEPTER_OF_TANTOS = 3236;

    private static final int RITUAL_BOX = 3237;

    private static final int MARK_OF_GLORY = 3203;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int KASMAN = 30501;

    private static final int VOKIAN = 30514;

    private static final int MANAKIA = 30515;

    private static final int KAKAI = 30565;

    private static final int TANAPI = 30571;

    private static final int VOLTAR = 30615;

    private static final int KEPRA = 30616;

    private static final int BURAI = 30617;

    private static final int HARAK = 30618;

    private static final int DRIKO = 30619;

    private static final int CHIANTA = 30642;

    private static final int TYRANT = 20192;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int GUARDIAN_BASILISK = 20550;

    private static final int MANASHEN_GARGOYLE = 20563;

    private static final int TIMAK_ORC = 20583;

    private static final int TIMAK_ORC_ARCHER = 20584;

    private static final int TIMAK_ORC_SOLDIER = 20585;

    private static final int TIMAK_ORC_WARRIOR = 20586;

    private static final int TIMAK_ORC_SHAMAN = 20587;

    private static final int TIMAK_ORC_OVERLORD = 20588;

    private static final int TAMLIN_ORC = 20601;

    private static final int TAMLIN_ORC_ARCHER = 20602;

    private static final int RAGNA_ORC_OVERLORD = 20778;

    private static final int RAGNA_ORC_SEER = 20779;

    private static final int PASHIKA_SON_OF_VOLTAR = 27080;

    private static final int VULTUS_SON_OF_VOLTAR = 27081;

    private static final int ENKU_ORC_OVERLORD = 27082;

    private static final int MAKUM_BUGBEAR_THUG = 27083;

    private static final int REVENANT_OF_TANTOS_CHIEF = 27086;

    private static boolean _sonsOfVoltar = false;

    private static boolean _enkuOrcOverlords = false;

    private static boolean _makumBugbearThugs = false;

    public Q220_TestimonyOfGlory() {
        super(220, "Testimony Of Glory");
        setItemsIds(3204, 3205, 3206, 3207, 3208, 3209, 3210, 3211, 3212, 3213,
                3214, 3215, 3216, 3217, 3218, 3219, 3220, 3221, 3222, 3223,
                3224, 3225, 3226, 3227, 3228, 3229, 3230, 3231, 3232, 3233,
                3234, 3235, 3236, 3237);
        addStartNpc(30514);
        addTalkId(30501, 30514, 30515, 30565, 30571, 30615, 30616, 30617, 30618, 30619,
                30642);
        addAttackId(20778, 20779, 27086);
        addKillId(20192, 20234, 20550, 20563, 20583, 20584, 20585, 20586, 20587, 20588,
                20601, 20602, 20778, 20779, 27080, 27081, 27082, 27083, 27086);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q220_TestimonyOfGlory");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30514-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3204, 1);
            if (!player.getMemos().getBool("secondClassChange37", false)) {
                htmltext = "30514-05a.htm";
                st.giveItems(7562, DF_REWARD_37.get(Integer.valueOf(player.getRace().ordinal())));
                player.getMemos().set("secondClassChange37", true);
            }
        } else if (event.equalsIgnoreCase("30642-03.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3208, 1);
            st.giveItems(3210, 1);
        } else if (event.equalsIgnoreCase("30642-07.htm")) {
            st.takeItems(3210, 1);
            st.takeItems(3230, 1);
            st.takeItems(3228, 1);
            st.takeItems(3229, 1);
            st.takeItems(3211, 1);
            st.takeItems(3212, 1);
            st.takeItems(3215, 1);
            st.takeItems(3214, 1);
            st.takeItems(3213, 1);
            if (player.getLevel() >= 37) {
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(3217, 1);
            } else {
                htmltext = "30642-06.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3216, 1);
            }
        } else if (event.equalsIgnoreCase("30501-02.htm") && !st.hasQuestItems(3213)) {
            if (st.hasQuestItems(3230)) {
                htmltext = "30501-04.htm";
            } else {
                htmltext = "30501-03.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3230, 1);
            }
            st.addRadar(-2150, 124443, -3724);
        } else if (event.equalsIgnoreCase("30501-05.htm") && !st.hasQuestItems(3214)) {
            if (st.hasQuestItems(3231)) {
                htmltext = "30501-07.htm";
            } else {
                htmltext = "30501-06.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3231, 1);
            }
            st.addRadar(-94294, 110818, -3563);
        } else if (event.equalsIgnoreCase("30501-08.htm") && !st.hasQuestItems(3215)) {
            if (st.hasQuestItems(3232)) {
                htmltext = "30501-10.htm";
            } else {
                htmltext = "30501-09.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3232, 1);
            }
            st.addRadar(-55217, 200628, -3724);
        } else if (event.equalsIgnoreCase("30515-02.htm") && !st.hasQuestItems(3211)) {
            if (st.hasQuestItems(3228)) {
                htmltext = "30515-04.htm";
            } else {
                htmltext = "30515-03.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3228, 1);
            }
            st.addRadar(80100, 119991, -2264);
        } else if (event.equalsIgnoreCase("30515-05.htm") && !st.hasQuestItems(3212)) {
            if (st.hasQuestItems(3229)) {
                htmltext = "30515-07.htm";
            } else {
                htmltext = "30515-06.htm";
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(3229, 1);
            }
            st.addRadar(19815, 189703, -3032);
        } else if (event.equalsIgnoreCase("30615-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3228, 1);
            st.giveItems(3223, 1);
            if (!_sonsOfVoltar) {
                addSpawn(27080, 80117, 120039, -2259, 0, false, 200000L, true);
                addSpawn(27081, 80058, 120038, -2259, 0, false, 200000L, true);
                _sonsOfVoltar = true;
                startQuestTimer("voltar_sons_cleanup", 201000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30616-05.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3229, 1);
            st.giveItems(3225, 1);
            if (!_enkuOrcOverlords) {
                addSpawn(27082, 19894, 189743, -3074, 0, false, 200000L, true);
                addSpawn(27082, 19869, 189800, -3059, 0, false, 200000L, true);
                addSpawn(27082, 19818, 189818, -3047, 0, false, 200000L, true);
                addSpawn(27082, 19753, 189837, -3027, 0, false, 200000L, true);
                _enkuOrcOverlords = true;
                startQuestTimer("enku_orcs_cleanup", 201000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30617-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3231, 1);
            st.giveItems(3227, 1);
            if (!_makumBugbearThugs) {
                addSpawn(27083, -94292, 110781, -3701, 0, false, 200000L, true);
                addSpawn(27083, -94293, 110861, -3701, 0, false, 200000L, true);
                _makumBugbearThugs = true;
                startQuestTimer("makum_bugbears_cleanup", 201000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30618-03.htm")) {
            st.takeItems(3232, 1);
            st.giveItems(3215, 1);
            if (st.hasQuestItems(3211, 3212, 3213, 3214)) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30619-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3230, 1);
            st.giveItems(3233, 1);
        } else if (event.equalsIgnoreCase("30571-03.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3220, 1);
            st.giveItems(3235, 1);
        } else {
            if (event.equalsIgnoreCase("voltar_sons_cleanup")) {
                _sonsOfVoltar = false;
                return null;
            }
            if (event.equalsIgnoreCase("enku_orcs_cleanup")) {
                _enkuOrcOverlords = false;
                return null;
            }
            if (event.equalsIgnoreCase("makum_bugbears_cleanup")) {
                _makumBugbearThugs = false;
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q220_TestimonyOfGlory");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30514-01.htm";
                    break;
                }
                if (player.getLevel() < 37) {
                    htmltext = "30514-02.htm";
                    break;
                }
                if (player.getClassId().level() != 1) {
                    htmltext = "30514-01a.htm";
                    break;
                }
                htmltext = "30514-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30514:
                        if (cond == 1) {
                            htmltext = "30514-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30514-08.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3207, 10);
                            st.takeItems(3205, 10);
                            st.takeItems(3206, 10);
                            st.takeItems(3204, 1);
                            st.giveItems(3209, 1);
                            st.giveItems(3208, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30514-09.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30514-10.htm";
                        break;
                    case 30642:
                        if (cond == 3) {
                            htmltext = "30642-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30642-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            if (st.hasQuestItems(3216)) {
                                if (player.getLevel() >= 37) {
                                    htmltext = "30642-09.htm";
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(3216, 1);
                                    st.giveItems(3217, 1);
                                    break;
                                }
                                htmltext = "30642-08.htm";
                                break;
                            }
                            htmltext = "30642-05.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30642-10.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30642-11.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3217, 1);
                            st.takeItems(3209, 1);
                            st.takeItems(3218, 20);
                            st.takeItems(3219, 20);
                            st.giveItems(3220, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30642-12.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30642-13.htm";
                        break;
                    case 30501:
                        if (st.hasQuestItems(3210)) {
                            htmltext = "30501-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30501-11.htm";
                        break;
                    case 30515:
                        if (st.hasQuestItems(3210)) {
                            htmltext = "30515-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30515-08.htm";
                        break;
                    case 30615:
                        if (cond > 3) {
                            if (st.hasQuestItems(3228)) {
                                htmltext = "30615-02.htm";
                                st.removeRadar(80100, 119991, -2264);
                                break;
                            }
                            if (st.hasQuestItems(3223)) {
                                htmltext = "30615-05.htm";
                                if (!_sonsOfVoltar) {
                                    addSpawn(27080, 80117, 120039, -2259, 0, false, 200000L, true);
                                    addSpawn(27081, 80058, 120038, -2259, 0, false, 200000L, true);
                                    _sonsOfVoltar = true;
                                    startQuestTimer("voltar_sons_cleanup", 201000L, null, player, false);
                                }
                                break;
                            }
                            if (st.hasQuestItems(3221, 3222)) {
                                htmltext = "30615-06.htm";
                                st.takeItems(3221, 1);
                                st.takeItems(3222, 1);
                                st.giveItems(3211, 1);
                                if (st.hasQuestItems(3212, 3213, 3214, 3215)) {
                                    st.set("cond", "5");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.hasQuestItems(3211)) {
                                htmltext = "30615-07.htm";
                                break;
                            }
                            if (st.hasQuestItems(3210)) {
                                htmltext = "30615-01.htm";
                                break;
                            }
                            if (cond < 9)
                                htmltext = "30615-08.htm";
                        }
                        break;
                    case 30616:
                        if (cond > 3) {
                            if (st.hasQuestItems(3229)) {
                                htmltext = "30616-02.htm";
                                st.removeRadar(19815, 189703, -3032);
                                break;
                            }
                            if (st.hasQuestItems(3225)) {
                                htmltext = "30616-05.htm";
                                if (!_enkuOrcOverlords) {
                                    addSpawn(27082, 19894, 189743, -3074, 0, false, 200000L, true);
                                    addSpawn(27082, 19869, 189800, -3059, 0, false, 200000L, true);
                                    addSpawn(27082, 19818, 189818, -3047, 0, false, 200000L, true);
                                    addSpawn(27082, 19753, 189837, -3027, 0, false, 200000L, true);
                                    _enkuOrcOverlords = true;
                                    startQuestTimer("enku_orcs_cleanup", 201000L, null, player, false);
                                }
                                break;
                            }
                            if (st.getQuestItemsCount(3224) == 4) {
                                htmltext = "30616-06.htm";
                                st.takeItems(3224, 4);
                                st.giveItems(3212, 1);
                                if (st.hasQuestItems(3211, 3213, 3214, 3215)) {
                                    st.set("cond", "5");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.hasQuestItems(3212)) {
                                htmltext = "30616-07.htm";
                                break;
                            }
                            if (st.hasQuestItems(3210)) {
                                htmltext = "30616-01.htm";
                                break;
                            }
                            if (cond < 9)
                                htmltext = "30616-08.htm";
                        }
                        break;
                    case 30617:
                        if (cond > 3) {
                            if (st.hasQuestItems(3231)) {
                                htmltext = "30617-02.htm";
                                st.removeRadar(-94294, 110818, -3563);
                                break;
                            }
                            if (st.hasQuestItems(3227)) {
                                htmltext = "30617-04.htm";
                                if (!_makumBugbearThugs) {
                                    addSpawn(27083, -94292, 110781, -3701, 0, false, 200000L, true);
                                    addSpawn(27083, -94293, 110861, -3701, 0, false, 200000L, true);
                                    _makumBugbearThugs = true;
                                    startQuestTimer("makum_bugbears_cleanup", 201000L, null, player, false);
                                }
                                break;
                            }
                            if (st.getQuestItemsCount(3226) == 2) {
                                htmltext = "30617-05.htm";
                                st.takeItems(3226, 2);
                                st.giveItems(3214, 1);
                                if (st.hasQuestItems(3211, 3213, 3212, 3215)) {
                                    st.set("cond", "5");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.hasQuestItems(3214)) {
                                htmltext = "30617-06.htm";
                                break;
                            }
                            if (st.hasQuestItems(3210)) {
                                htmltext = "30617-01.htm";
                                break;
                            }
                            if (cond < 8)
                                htmltext = "30617-07.htm";
                        }
                        break;
                    case 30618:
                        if (cond > 3) {
                            if (st.hasQuestItems(3232)) {
                                htmltext = "30618-02.htm";
                                st.removeRadar(-55217, 200628, -3724);
                                break;
                            }
                            if (st.hasQuestItems(3215)) {
                                htmltext = "30618-04.htm";
                                break;
                            }
                            if (st.hasQuestItems(3210)) {
                                htmltext = "30618-01.htm";
                                break;
                            }
                            if (cond < 9)
                                htmltext = "30618-05.htm";
                        }
                        break;
                    case 30619:
                        if (cond > 3) {
                            if (st.hasQuestItems(3230)) {
                                htmltext = "30619-02.htm";
                                st.removeRadar(-2150, 124443, -3724);
                                break;
                            }
                            if (st.hasQuestItems(3233)) {
                                if (st.getQuestItemsCount(3234) == 30) {
                                    htmltext = "30619-05.htm";
                                    st.takeItems(3233, 1);
                                    st.takeItems(3234, 30);
                                    st.giveItems(3213, 1);
                                    if (st.hasQuestItems(3211, 3214, 3212, 3215)) {
                                        st.set("cond", "5");
                                        st.playSound("ItemSound.quest_middle");
                                        break;
                                    }
                                    st.playSound("ItemSound.quest_itemget");
                                    break;
                                }
                                htmltext = "30619-04.htm";
                                break;
                            }
                            if (st.hasQuestItems(3213)) {
                                htmltext = "30619-06.htm";
                                break;
                            }
                            if (st.hasQuestItems(3210)) {
                                htmltext = "30619-01.htm";
                                break;
                            }
                            if (cond < 8)
                                htmltext = "30619-07.htm";
                        }
                        break;
                    case 30571:
                        if (cond == 8) {
                            htmltext = "30571-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30571-04.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30571-05.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3236, 1);
                            st.takeItems(3235, 1);
                            st.giveItems(3237, 1);
                            break;
                        }
                        if (cond == 11)
                            htmltext = "30571-06.htm";
                        break;
                    case 30565:
                        if (cond > 7 && cond < 11) {
                            htmltext = "30565-01.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30565-02.htm";
                            st.takeItems(3237, 1);
                            st.giveItems(3203, 1);
                            st.rewardExpAndSp(91457L, 2500);
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

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20778:
            case 20779:
                if (cond == 9 && npc.isScriptValue(0)) {
                    npc.broadcastNpcSay("Is it a lackey of Kakai?!");
                    npc.setScriptValue(1);
                }
                break;
            case 27086:
                if (cond == 9) {
                    if (npc.isScriptValue(0)) {
                        npc.broadcastNpcSay("How regretful! Unjust dishonor!");
                        npc.setScriptValue(1);
                        break;
                    }
                    if (npc.isScriptValue(1) && npc.getCurrentHp() / npc.getMaxHp() < 0.33D) {
                        npc.broadcastNpcSay("Indignant and unfair death!");
                        npc.setScriptValue(2);
                    }
                }
                break;
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20192:
                if (cond == 1 && st.dropItems(3206, 1, 10, 500000) && st.getQuestItemsCount(3207) + st.getQuestItemsCount(3205) == 20)
                    st.set("cond", "2");
                break;
            case 20550:
                if (cond == 1 && st.dropItems(3207, 1, 10, 500000) && st.getQuestItemsCount(3206) + st.getQuestItemsCount(3205) == 20)
                    st.set("cond", "2");
                break;
            case 20563:
                if (cond == 1 && st.dropItems(3205, 1, 10, 750000) && st.getQuestItemsCount(3206) + st.getQuestItemsCount(3207) == 20)
                    st.set("cond", "2");
                break;
            case 20234:
                if (st.hasQuestItems(3233))
                    st.dropItems(3234, 1, 30, 750000);
                break;
            case 27080:
                if (st.hasQuestItems(3223) && !st.hasQuestItems(3221)) {
                    st.giveItems(3221, 1);
                    if (st.hasQuestItems(3222)) {
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3223, 1);
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 27081:
                if (st.hasQuestItems(3223) && !st.hasQuestItems(3222)) {
                    st.giveItems(3222, 1);
                    if (st.hasQuestItems(3221)) {
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3223, 1);
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 27082:
                if (st.hasQuestItems(3225) && st.dropItemsAlways(3224, 1, 4))
                    st.takeItems(3225, 1);
                break;
            case 27083:
                if (st.hasQuestItems(3227) && st.dropItemsAlways(3226, 1, 2))
                    st.takeItems(3227, 1);
                break;
            case 20583:
            case 20584:
            case 20585:
            case 20586:
            case 20587:
            case 20588:
                if (cond == 6 && st.dropItems(3219, 1, 20, 500000 + (npc.getNpcId() - 20583) * 100000) && st.getQuestItemsCount(3218) == 20)
                    st.set("cond", "7");
                break;
            case 20601:
                if (cond == 6 && st.dropItems(3218, 1, 20, 500000) && st.getQuestItemsCount(3219) == 20)
                    st.set("cond", "7");
                break;
            case 20602:
                if (cond == 6 && st.dropItems(3218, 1, 20, 600000) && st.getQuestItemsCount(3219) == 20)
                    st.set("cond", "7");
                break;
            case 20778:
            case 20779:
                if (cond == 9) {
                    npc.broadcastNpcSay("Too late!");
                    addSpawn(27086, npc, true, 200000L, true);
                }
                break;
            case 27086:
                if (cond == 9 && st.dropItemsAlways(3236, 1, 1)) {
                    st.set("cond", "10");
                    npc.broadcastNpcSay("I'll get revenge someday!!");
                }
                break;
        }
        return null;
    }
}
