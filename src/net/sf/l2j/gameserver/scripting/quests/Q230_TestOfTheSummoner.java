/**/
package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Q230_TestOfTheSummoner extends Quest {
    public static final String qn = "Q230_TestOfTheSummoner";
    private static final int LETO_LIZARDMAN_AMULET = 3337;
    private static final int SAC_OF_REDSPORES = 3338;
    private static final int KARUL_BUGBEAR_TOTEM = 3339;
    private static final int SHARDS_OF_MANASHEN = 3340;
    private static final int BREKA_ORC_TOTEM = 3341;
    private static final int CRIMSON_BLOODSTONE = 3342;
    private static final int TALONS_OF_TYRANT = 3343;
    private static final int WINGS_OF_DRONEANT = 3344;
    private static final int TUSK_OF_WINDSUS = 3345;
    private static final int FANGS_OF_WYRM = 3346;
    private static final int LARA_LIST_1 = 3347;
    private static final int LARA_LIST_2 = 3348;
    private static final int LARA_LIST_3 = 3349;
    private static final int LARA_LIST_4 = 3350;
    private static final int LARA_LIST_5 = 3351;
    private static final int GALATEA_LETTER = 3352;
    private static final int BEGINNER_ARCANA = 3353;
    private static final int ALMORS_ARCANA = 3354;
    private static final int CAMONIELL_ARCANA = 3355;
    private static final int BELTHUS_ARCANA = 3356;
    private static final int BASILLIA_ARCANA = 3357;
    private static final int CELESTIEL_ARCANA = 3358;
    private static final int BRYNTHEA_ARCANA = 3359;
    private static final int CRYSTAL_OF_PROGRESS_1 = 3360;
    private static final int CRYSTAL_OF_INPROGRESS_1 = 3361;
    private static final int CRYSTAL_OF_FOUL_1 = 3362;
    private static final int CRYSTAL_OF_DEFEAT_1 = 3363;
    private static final int CRYSTAL_OF_VICTORY_1 = 3364;
    private static final int CRYSTAL_OF_PROGRESS_2 = 3365;
    private static final int CRYSTAL_OF_INPROGRESS_2 = 3366;
    private static final int CRYSTAL_OF_FOUL_2 = 3367;
    private static final int CRYSTAL_OF_DEFEAT_2 = 3368;
    private static final int CRYSTAL_OF_VICTORY_2 = 3369;
    private static final int CRYSTAL_OF_PROGRESS_3 = 3370;
    private static final int CRYSTAL_OF_INPROGRESS_3 = 3371;
    private static final int CRYSTAL_OF_FOUL_3 = 3372;
    private static final int CRYSTAL_OF_DEFEAT_3 = 3373;
    private static final int CRYSTAL_OF_VICTORY_3 = 3374;
    private static final int CRYSTAL_OF_PROGRESS_4 = 3375;
    private static final int CRYSTAL_OF_INPROGRESS_4 = 3376;
    private static final int CRYSTAL_OF_FOUL_4 = 3377;
    private static final int CRYSTAL_OF_DEFEAT_4 = 3378;
    private static final int CRYSTAL_OF_VICTORY_4 = 3379;
    private static final int CRYSTAL_OF_PROGRESS_5 = 3380;
    private static final int CRYSTAL_OF_INPROGRESS_5 = 3381;
    private static final int CRYSTAL_OF_FOUL_5 = 3382;
    private static final int CRYSTAL_OF_DEFEAT_5 = 3383;
    private static final int CRYSTAL_OF_VICTORY_5 = 3384;
    private static final int CRYSTAL_OF_PROGRESS_6 = 3385;
    private static final int CRYSTAL_OF_INPROGRESS_6 = 3386;
    private static final int CRYSTAL_OF_FOUL_6 = 3387;
    private static final int CRYSTAL_OF_DEFEAT_6 = 3388;
    private static final int CRYSTAL_OF_VICTORY_6 = 3389;
    private static final int MARK_OF_SUMMONER = 3336;
    private static final int DIMENSIONAL_DIAMOND = 7562;
    private static final int LARA = 30063;
    private static final int GALATEA = 30634;
    private static final int ALMORS = 30635;
    private static final int CAMONIELL = 30636;
    private static final int BELTHUS = 30637;
    private static final int BASILLA = 30638;
    private static final int CELESTIEL = 30639;
    private static final int BRYNTHEA = 30640;
    private static final int NOBLE_ANT = 20089;
    private static final int NOBLE_ANT_LEADER = 20090;
    private static final int WYRM = 20176;
    private static final int TYRANT = 20192;
    private static final int TYRANT_KINGPIN = 20193;
    private static final int BREKA_ORC = 20267;
    private static final int BREKA_ORC_ARCHER = 20268;
    private static final int BREKA_ORC_SHAMAN = 20269;
    private static final int BREKA_ORC_OVERLORD = 20270;
    private static final int BREKA_ORC_WARRIOR = 20271;
    private static final int FETTERED_SOUL = 20552;
    private static final int WINDSUS = 20553;
    private static final int GIANT_FUNGUS = 20555;
    private static final int MANASHEN_GARGOYLE = 20563;
    private static final int LETO_LIZARDMAN = 20577;
    private static final int LETO_LIZARDMAN_ARCHER = 20578;
    private static final int LETO_LIZARDMAN_SOLDIER = 20579;
    private static final int LETO_LIZARDMAN_WARRIOR = 20580;
    private static final int LETO_LIZARDMAN_SHAMAN = 20581;
    private static final int LETO_LIZARDMAN_OVERLORD = 20582;
    private static final int KARUL_BUGBEAR = 20600;
    private static final int PAKO_THE_CAT = 27102;
    private static final int UNICORN_RACER = 27103;
    private static final int SHADOW_TUREN = 27104;
    private static final int MIMI_THE_CAT = 27105;
    private static final int UNICORN_PHANTASM = 27106;
    private static final int SILHOUETTE_TILFO = 27107;
    private static final int[][] LARA_LISTS = new int[][]{{3347, 3338, 3337}, {3348, 3339, 3340}, {3349, 3342, 3341}, {3350, 3345, 3343}, {3351, 3344, 3346}};
    private static final Map<Integer, Q230_TestOfTheSummoner.ProgressDuelMob> _duelsInProgress = new ConcurrentHashMap<>();

    public Q230_TestOfTheSummoner() {
        super(230, "Test of the Summoner");
        this.setItemsIds(3337, 3338, 3339, 3340, 3341, 3342, 3343, 3344, 3345, 3346, 3347, 3348, 3349, 3350, 3351, 3352, 3353, 3354, 3355, 3356, 3357, 3358, 3359, 3360, 3361, 3362, 3363, 3364, 3365, 3366, 3367, 3368, 3369, 3370, 3371, 3372, 3373, 3374, 3375, 3376, 3377, 3378, 3379, 3380, 3381, 3382, 3383, 3384, 3385, 3386, 3387, 3388, 3389);
        this.addStartNpc(30634);
        this.addTalkId(30634, 30635, 30636, 30637, 30638, 30639, 30640, 30063);
        this.addKillId(20089, 20090, 20176, 20192, 20193, 20267, 20268, 20269, 20270, 20271, 20552, 20553, 20555, 20563, 20577, 20578, 20579, 20580, 20581, 20582, 20600, 27102, 27103, 27104, 27105, 27106, 27107);
        this.addAttackId(27102, 27103, 27104, 27105, 27106, 27107);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q230_TestOfTheSummoner");
        if (st == null) {
            return null;
        } else {
            if (event.equals("30634-08.htm")) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.set("Belthus", "1");
                st.set("Brynthea", "1");
                st.set("Celestiel", "1");
                st.set("Camoniell", "1");
                st.set("Basilla", "1");
                st.set("Almors", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(3352, 1);
                if (!player.getMemos().getBool("secondClassChange39", false)) {
                    htmltext = "30634-08a.htm";
                    st.giveItems(7562, DF_REWARD_39.get(player.getClassId().getId()));
                    player.getMemos().set("secondClassChange39", true);
                }
            } else {
                int random;
                if (event.equals("30063-02.htm")) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(3352, 1);
                    random = Rnd.get(5);
                    st.giveItems(LARA_LISTS[random][0], 1);
                    st.set("Lara", String.valueOf(random + 1));
                } else if (event.equals("30063-04.htm")) {
                    random = Rnd.get(5);
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(LARA_LISTS[random][0], 1);
                    st.set("Lara", String.valueOf(random + 1));
                } else if (event.equals("30635-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30635-03.htm";
                    }
                } else if (event.equals("30635-04.htm")) {
                    st.set("Almors", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3362, -1);
                    st.takeItems(3363, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3360, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                } else if (event.equals("30636-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30636-03.htm";
                    }
                } else if (event.equals("30636-04.htm")) {
                    st.set("Camoniell", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3367, -1);
                    st.takeItems(3368, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3365, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                } else if (event.equals("30637-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30637-03.htm";
                    }
                } else if (event.equals("30637-04.htm")) {
                    st.set("Belthus", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3372, -1);
                    st.takeItems(3373, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3370, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                } else if (event.equals("30638-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30638-03.htm";
                    }
                } else if (event.equals("30638-04.htm")) {
                    st.set("Basilla", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3377, -1);
                    st.takeItems(3378, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3375, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                } else if (event.equals("30639-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30639-03.htm";
                    }
                } else if (event.equals("30639-04.htm")) {
                    st.set("Celestiel", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3382, -1);
                    st.takeItems(3383, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3380, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                } else if (event.equals("30640-02.htm")) {
                    if (st.hasQuestItems(3353)) {
                        htmltext = "30640-03.htm";
                    }
                } else if (event.equals("30640-04.htm")) {
                    st.set("Brynthea", "2");
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(3387, -1);
                    st.takeItems(3388, -1);
                    st.takeItems(3353, 1);
                    st.giveItems(3385, 1);
                    npc.setTarget(player);
                    npc.doCast(SkillTable.getInstance().getInfo(4126, 1));
                }
            }

            return htmltext;
        }
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q230_TestOfTheSummoner");
        if (st == null) {
            return htmltext;
        } else {
            int cond = st.getInt("cond");
            int npcId = npc.getNpcId();
            switch (st.getState()) {
                case 0:
                    if (player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.ELVEN_WIZARD && player.getClassId() != ClassId.DARK_WIZARD) {
                        htmltext = "30634-01.htm";
                    } else if (player.getLevel() < 39) {
                        htmltext = "30634-02.htm";
                    } else {
                        htmltext = "30634-03.htm";
                    }
                    break;
                case 1:
                    switch (npcId) {
                        case 30063:
                            if (cond == 1) {
                                htmltext = "30063-01.htm";
                                return htmltext;
                            } else if (st.getInt("Lara") == 0) {
                                htmltext = "30063-03.htm";
                                return htmltext;
                            } else {
                                int[] laraPart = LARA_LISTS[st.getInt("Lara") - 1];
                                if (st.getQuestItemsCount(laraPart[1]) >= 30 && st.getQuestItemsCount(laraPart[2]) >= 30) {
                                    htmltext = "30063-06.htm";
                                    st.set("cond", "3");
                                    st.unset("Lara");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(laraPart[0], 1);
                                    st.takeItems(laraPart[1], -1);
                                    st.takeItems(laraPart[2], -1);
                                    st.giveItems(3353, 2);
                                } else {
                                    htmltext = "30063-05.htm";
                                }

                                return htmltext;
                            }
                        case 30634:
                            if (cond == 1) {
                                htmltext = "30634-09.htm";
                                return htmltext;
                            } else {
                                if (cond != 2 && cond != 3) {
                                    if (cond == 4) {
                                        htmltext = "30634-12.htm";
                                        st.takeItems(3353, -1);
                                        st.takeItems(3354, -1);
                                        st.takeItems(3357, -1);
                                        st.takeItems(3356, -1);
                                        st.takeItems(3359, -1);
                                        st.takeItems(3355, -1);
                                        st.takeItems(3358, -1);
                                        st.takeItems(3347, -1);
                                        st.takeItems(3348, -1);
                                        st.takeItems(3349, -1);
                                        st.takeItems(3350, -1);
                                        st.takeItems(3351, -1);
                                        st.giveItems(3336, 1);
                                        st.rewardExpAndSp(148409L, 30000);
                                        player.broadcastPacket(new SocialAction(player, 3));
                                        st.playSound("ItemSound.quest_finish");
                                        st.exitQuest(false);
                                        return htmltext;
                                    }
                                } else {
                                    htmltext = !st.hasQuestItems(3353) ? "30634-10.htm" : "30634-11.htm";
                                }

                                return htmltext;
                            }
                        case 30635:
                            int almorsStat = st.getInt("Almors");
                            if (almorsStat == 1) {
                                htmltext = "30635-01.htm";
                                return htmltext;
                            } else if (almorsStat == 2) {
                                htmltext = "30635-08.htm";
                                return htmltext;
                            } else if (almorsStat == 3) {
                                htmltext = "30635-09.htm";
                                return htmltext;
                            } else if (almorsStat == 4) {
                                htmltext = "30635-05.htm";
                                return htmltext;
                            } else if (almorsStat == 5) {
                                htmltext = "30635-06.htm";
                                return htmltext;
                            } else if (almorsStat == 6) {
                                htmltext = "30635-07.htm";
                                st.set("Almors", "7");
                                st.takeItems(3364, -1);
                                st.giveItems(3354, 1);
                                if (st.hasQuestItems(3355, 3356, 3357, 3358, 3359)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (almorsStat == 7) {
                                    htmltext = "30635-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        case 30636:
                            int camoniellStat = st.getInt("Camoniell");
                            if (camoniellStat == 1) {
                                htmltext = "30636-01.htm";
                                return htmltext;
                            } else if (camoniellStat == 2) {
                                htmltext = "30636-08.htm";
                                return htmltext;
                            } else if (camoniellStat == 3) {
                                htmltext = "30636-09.htm";
                                return htmltext;
                            } else if (camoniellStat == 4) {
                                htmltext = "30636-05.htm";
                                return htmltext;
                            } else if (camoniellStat == 5) {
                                htmltext = "30636-06.htm";
                                return htmltext;
                            } else if (camoniellStat == 6) {
                                htmltext = "30636-07.htm";
                                st.set("Camoniell", "7");
                                st.takeItems(3369, -1);
                                st.giveItems(3355, 1);
                                if (st.hasQuestItems(3354, 3356, 3357, 3358, 3359)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (camoniellStat == 7) {
                                    htmltext = "30636-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        case 30637:
                            int belthusStat = st.getInt("Belthus");
                            if (belthusStat == 1) {
                                htmltext = "30637-01.htm";
                                return htmltext;
                            } else if (belthusStat == 2) {
                                htmltext = "30637-08.htm";
                                return htmltext;
                            } else if (belthusStat == 3) {
                                htmltext = "30637-09.htm";
                                return htmltext;
                            } else if (belthusStat == 4) {
                                htmltext = "30637-05.htm";
                                return htmltext;
                            } else if (belthusStat == 5) {
                                htmltext = "30637-06.htm";
                                return htmltext;
                            } else if (belthusStat == 6) {
                                htmltext = "30637-07.htm";
                                st.set("Belthus", "7");
                                st.takeItems(3374, -1);
                                st.giveItems(3356, 1);
                                if (st.hasQuestItems(3354, 3355, 3357, 3358, 3359)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (belthusStat == 7) {
                                    htmltext = "30637-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        case 30638:
                            int basillaStat = st.getInt("Basilla");
                            if (basillaStat == 1) {
                                htmltext = "30638-01.htm";
                                return htmltext;
                            } else if (basillaStat == 2) {
                                htmltext = "30638-08.htm";
                                return htmltext;
                            } else if (basillaStat == 3) {
                                htmltext = "30638-09.htm";
                                return htmltext;
                            } else if (basillaStat == 4) {
                                htmltext = "30638-05.htm";
                                return htmltext;
                            } else if (basillaStat == 5) {
                                htmltext = "30638-06.htm";
                                return htmltext;
                            } else if (basillaStat == 6) {
                                htmltext = "30638-07.htm";
                                st.set("Basilla", "7");
                                st.takeItems(3379, -1);
                                st.giveItems(3357, 1);
                                if (st.hasQuestItems(3354, 3355, 3356, 3358, 3359)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (basillaStat == 7) {
                                    htmltext = "30638-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        case 30639:
                            int celestielStat = st.getInt("Celestiel");
                            if (celestielStat == 1) {
                                htmltext = "30639-01.htm";
                                return htmltext;
                            } else if (celestielStat == 2) {
                                htmltext = "30639-08.htm";
                                return htmltext;
                            } else if (celestielStat == 3) {
                                htmltext = "30639-09.htm";
                                return htmltext;
                            } else if (celestielStat == 4) {
                                htmltext = "30639-05.htm";
                                return htmltext;
                            } else if (celestielStat == 5) {
                                htmltext = "30639-06.htm";
                                return htmltext;
                            } else if (celestielStat == 6) {
                                htmltext = "30639-07.htm";
                                st.set("Celestiel", "7");
                                st.takeItems(3384, -1);
                                st.giveItems(3358, 1);
                                if (st.hasQuestItems(3354, 3355, 3356, 3357, 3359)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (celestielStat == 7) {
                                    htmltext = "30639-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        case 30640:
                            int bryntheaStat = st.getInt("Brynthea");
                            if (bryntheaStat == 1) {
                                htmltext = "30640-01.htm";
                                return htmltext;
                            } else if (bryntheaStat == 2) {
                                htmltext = "30640-08.htm";
                                return htmltext;
                            } else if (bryntheaStat == 3) {
                                htmltext = "30640-09.htm";
                                return htmltext;
                            } else if (bryntheaStat == 4) {
                                htmltext = "30640-05.htm";
                                return htmltext;
                            } else if (bryntheaStat == 5) {
                                htmltext = "30640-06.htm";
                                return htmltext;
                            } else if (bryntheaStat == 6) {
                                htmltext = "30640-07.htm";
                                st.set("Brynthea", "7");
                                st.takeItems(3389, -1);
                                st.giveItems(3359, 1);
                                if (st.hasQuestItems(3354, 3355, 3356, 3357, 3358)) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.playSound("ItemSound.quest_itemget");
                                }

                                return htmltext;
                            } else {
                                if (bryntheaStat == 7) {
                                    htmltext = "30640-10.htm";
                                    return htmltext;
                                }

                                return htmltext;
                            }
                        default:
                            return htmltext;
                    }
                case 2:
                    htmltext = getAlreadyCompletedMsg();
            }

            return htmltext;
        }
    }

    public String onDeath(Creature killer, Player player) {
        if (!(killer instanceof Attackable)) {
            return null;
        } else {
            QuestState st = this.checkPlayerState(player, (Npc) killer, (byte) 1);
            if (st == null) {
                return null;
            } else {
                switch (((Npc) killer).getNpcId()) {
                    case 27102:
                        if (st.getInt("Almors") == 3) {
                            st.set("Almors", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3363, 1);
                        }
                        break;
                    case 27103:
                        if (st.getInt("Camoniell") == 3) {
                            st.set("Camoniell", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3368, 1);
                        }
                        break;
                    case 27104:
                        if (st.getInt("Belthus") == 3) {
                            st.set("Belthus", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3373, 1);
                        }
                        break;
                    case 27105:
                        if (st.getInt("Basilla") == 3) {
                            st.set("Basilla", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3378, 1);
                        }
                        break;
                    case 27106:
                        if (st.getInt("Celestiel") == 3) {
                            st.set("Celestiel", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3383, 1);
                        }
                        break;
                    case 27107:
                        if (st.getInt("Brynthea") == 3) {
                            st.set("Brynthea", "4");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(3388, 1);
                        }
                }

                return null;
            }
        }
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = this.checkPlayerState(player, npc, (byte) 1);
        if (st == null) {
            return null;
        } else {
            int npcId = npc.getNpcId();
            switch (npcId) {
                case 20089:
                case 20090:
                    if (st.getInt("Lara") == 5) {
                        st.dropItems(3344, 1, 30, 600000);
                    }
                    break;
                case 20176:
                    if (st.getInt("Lara") == 5) {
                        st.dropItems(3346, 1, 30, 500000);
                    }
                    break;
                case 20192:
                case 20193:
                    if (st.getInt("Lara") == 4) {
                        st.dropItems(3343, 1, 30, 500000);
                    }
                    break;
                case 20267:
                case 20268:
                case 20271:
                    if (st.getInt("Lara") == 3) {
                        st.dropItems(3341, 1, 30, 250000);
                    }
                    break;
                case 20269:
                case 20270:
                    if (st.getInt("Lara") == 3) {
                        st.dropItems(3341, 1, 30, 500000);
                    }
                    break;
                case 20552:
                    if (st.getInt("Lara") == 3) {
                        st.dropItems(3342, 1, 30, 600000);
                    }
                    break;
                case 20553:
                    if (st.getInt("Lara") == 4) {
                        st.dropItems(3345, 1, 30, 700000);
                    }
                    break;
                case 20555:
                    if (st.getInt("Lara") == 1) {
                        st.dropItems(3338, 1, 30, 800000);
                    }
                    break;
                case 20563:
                    if (st.getInt("Lara") == 2) {
                        st.dropItems(3340, 1, 30, 800000);
                    }
                    break;
                case 20577:
                case 20578:
                    if (st.getInt("Lara") == 1) {
                        st.dropItems(3337, 1, 30, 250000);
                    }
                    break;
                case 20579:
                case 20580:
                    if (st.getInt("Lara") == 1) {
                        st.dropItems(3337, 1, 30, 500000);
                    }
                    break;
                case 20581:
                case 20582:
                    if (st.getInt("Lara") == 1) {
                        st.dropItems(3337, 1, 30, 750000);
                    }
                    break;
                case 20600:
                    if (st.getInt("Lara") == 2) {
                        st.dropItems(3339, 1, 30, 800000);
                    }
                    break;
                case 27102:
                    if (st.getInt("Almors") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Almors", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3361, -1);
                        st.giveItems(3364, 1);
                        npc.broadcastNpcSay("I'm sorry, Lord!");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
                    break;
                case 27103:
                    if (st.getInt("Camoniell") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Camoniell", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3366, -1);
                        st.giveItems(3369, 1);
                        npc.broadcastNpcSay("I LOSE");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
                    break;
                case 27104:
                    if (st.getInt("Belthus") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Belthus", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3371, -1);
                        st.giveItems(3374, 1);
                        npc.broadcastNpcSay("Ugh! I lost...!");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
                    break;
                case 27105:
                    if (st.getInt("Basilla") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Basilla", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3376, -1);
                        st.giveItems(3379, 1);
                        npc.broadcastNpcSay("Lost! Sorry, Lord!");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
                    break;
                case 27106:
                    if (st.getInt("Celestiel") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Celestiel", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3381, -1);
                        st.giveItems(3384, 1);
                        npc.broadcastNpcSay("I LOSE");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
                    break;
                case 27107:
                    if (st.getInt("Brynthea") == 3 && _duelsInProgress.containsKey(npcId)) {
                        st.set("Brynthea", "6");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(3386, -1);
                        st.giveItems(3389, 1);
                        npc.broadcastNpcSay("Ugh! Can this be happening?!");
                        st.getPlayer().removeNotifyQuestOfDeath(st);
                        _duelsInProgress.remove(npcId);
                    }
            }

            return null;
        }
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        QuestState st = this.checkPlayerState(player, npc, (byte) 1);
        if (st == null) {
            return null;
        } else {
            st.addNotifyOfDeath();
            int npcId = npc.getNpcId();
            boolean isPet = attacker instanceof Summon;
            Q230_TestOfTheSummoner.ProgressDuelMob duel;
            Player foulPlayer;
            switch (npcId) {
                case 27102:
                    if (st.getInt("Almors") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Almors", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3360, -1);
                        st.giveItems(3361, 1);
                        npc.broadcastNpcSay("Whhiisshh!");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Almors") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Almors", "5");
                                    st.takeItems(3360, -1);
                                    st.takeItems(3361, -1);
                                    st.giveItems(3362, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("Rule violation!");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
                    break;
                case 27103:
                    if (st.getInt("Camoniell") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Camoniell", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3365, -1);
                        st.giveItems(3366, 1);
                        npc.broadcastNpcSay("START DUEL");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Camoniell") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Camoniell", "5");
                                    st.takeItems(3365, -1);
                                    st.takeItems(3366, -1);
                                    st.giveItems(3367, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("RULE VIOLATION");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
                    break;
                case 27104:
                    if (st.getInt("Belthus") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Belthus", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3370, -1);
                        st.giveItems(3371, 1);
                        npc.broadcastNpcSay("So shall we start?!");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Belthus") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Belthus", "5");
                                    st.takeItems(3370, -1);
                                    st.takeItems(3371, -1);
                                    st.giveItems(3372, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("Rule violation!!!");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
                    break;
                case 27105:
                    if (st.getInt("Basilla") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Basilla", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3375, -1);
                        st.giveItems(3376, 1);
                        npc.broadcastNpcSay("Whish! Fight!");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Basilla") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Basilla", "5");
                                    st.takeItems(3375, -1);
                                    st.takeItems(3376, -1);
                                    st.giveItems(3377, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("Rule violation!");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
                    break;
                case 27106:
                    if (st.getInt("Celestiel") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Celestiel", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3380, -1);
                        st.giveItems(3381, 1);
                        npc.broadcastNpcSay("START DUEL");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Celestiel") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Celestiel", "5");
                                    st.takeItems(3380, -1);
                                    st.takeItems(3381, -1);
                                    st.giveItems(3382, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("RULE VIOLATION");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
                    break;
                case 27107:
                    if (st.getInt("Brynthea") == 2 && isPet && npc.getCurrentHp() == (double) npc.getMaxHp()) {
                        st.set("Brynthea", "3");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3385, -1);
                        st.giveItems(3386, 1);
                        npc.broadcastNpcSay("I'll walk all over you!");
                        _duelsInProgress.put(npcId, new ProgressDuelMob(this, player, attacker.getSummon()));
                    } else if (st.getInt("Brynthea") == 3 && _duelsInProgress.containsKey(npcId)) {
                        duel = _duelsInProgress.get(npcId);
                        if (!isPet || attacker.getSummon() != duel.getPet()) {
                            foulPlayer = duel.getAttacker();
                            if (foulPlayer != null) {
                                st = foulPlayer.getQuestState("Q230_TestOfTheSummoner");
                                if (st != null) {
                                    st.set("Brynthea", "5");
                                    st.takeItems(3385, -1);
                                    st.takeItems(3386, -1);
                                    st.giveItems(3387, 1);
                                    st.getPlayer().removeNotifyQuestOfDeath(st);
                                    npc.broadcastNpcSay("Rule violation!!!");
                                    npc.doDie(npc);
                                }
                            }
                        }
                    }
            }

            return null;
        }
    }

    private static final class ProgressDuelMob {
        private final Player _attacker;
        private final Summon _pet;

        public ProgressDuelMob(final Q230_TestOfTheSummoner param1, Player param2, Summon param3) {
            this._attacker = param2;
            this._pet = param3;
        }

        public Player getAttacker() {
            return this._attacker;
        }

        public Summon getPet() {
            return this._pet;
        }
    }
}