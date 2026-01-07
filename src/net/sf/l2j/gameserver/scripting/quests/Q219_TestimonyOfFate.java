package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q219_TestimonyOfFate extends Quest {
    private static final String qn = "Q219_TestimonyOfFate";

    private static final int KAIRA = 30476;

    private static final int METHEUS = 30614;

    private static final int IXIA = 30463;

    private static final int ALDER_SPIRIT = 30613;

    private static final int ROA = 30114;

    private static final int NORMAN = 30210;

    private static final int THIFIELL = 30358;

    private static final int ARKENIA = 30419;

    private static final int BLOODY_PIXY = 31845;

    private static final int BLIGHT_TREANT = 31850;

    private static final int KAIRA_LETTER = 3173;

    private static final int METHEUS_FUNERAL_JAR = 3174;

    private static final int KASANDRA_REMAINS = 3175;

    private static final int HERBALISM_TEXTBOOK = 3176;

    private static final int IXIA_LIST = 3177;

    private static final int MEDUSA_ICHOR = 3178;

    private static final int MARSH_SPIDER_FLUIDS = 3179;

    private static final int DEAD_SEEKER_DUNG = 3180;

    private static final int TYRANT_BLOOD = 3181;

    private static final int NIGHTSHADE_ROOT = 3182;

    private static final int BELLADONNA = 3183;

    private static final int ALDER_SKULL_1 = 3184;

    private static final int ALDER_SKULL_2 = 3185;

    private static final int ALDER_RECEIPT = 3186;

    private static final int REVELATIONS_MANUSCRIPT = 3187;

    private static final int KAIRA_RECOMMENDATION = 3189;

    private static final int KAIRA_INSTRUCTIONS = 3188;

    private static final int PALUS_CHARM = 3190;

    private static final int THIFIELL_LETTER = 3191;

    private static final int ARKENIA_NOTE = 3192;

    private static final int PIXY_GARNET = 3193;

    private static final int GRANDIS_SKULL = 3194;

    private static final int KARUL_BUGBEAR_SKULL = 3195;

    private static final int BREKA_OVERLORD_SKULL = 3196;

    private static final int LETO_OVERLORD_SKULL = 3197;

    private static final int RED_FAIRY_DUST = 3198;

    private static final int BLIGHT_TREANT_SEED = 3199;

    private static final int BLACK_WILLOW_LEAF = 3200;

    private static final int BLIGHT_TREANT_SAP = 3201;

    private static final int ARKENIA_LETTER = 3202;

    private static final int MARK_OF_FATE = 3172;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int HANGMAN_TREE = 20144;

    private static final int MARSH_STAKATO = 20157;

    private static final int MEDUSA = 20158;

    private static final int TYRANT = 20192;

    private static final int TYRANT_KINGPIN = 20193;

    private static final int DEAD_SEEKER = 20202;

    private static final int MARSH_STAKATO_WORKER = 20230;

    private static final int MARSH_STAKATO_SOLDIER = 20232;

    private static final int MARSH_SPIDER = 20233;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int BREKA_ORC_OVERLORD = 20270;

    private static final int GRANDIS = 20554;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int KARUL_BUGBEAR = 20600;

    private static final int BLACK_WILLOW_LURKER = 27079;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q219_TestimonyOfFate() {
        super(219, "Testimony of Fate");
        CHANCES.put(Integer.valueOf(20202), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20192), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20193), Integer.valueOf(600000));
        CHANCES.put(Integer.valueOf(20158), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20157), Integer.valueOf(400000));
        CHANCES.put(Integer.valueOf(20230), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(20232), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20234), Integer.valueOf(600000));
        CHANCES.put(Integer.valueOf(20233), Integer.valueOf(500000));
        setItemsIds(3173, 3174, 3175, 3176, 3177, 3178, 3179, 3180, 3181, 3182,
                3183, 3184, 3185, 3186, 3187, 3189, 3188, 3190, 3191, 3192,
                3193, 3194, 3195, 3196, 3197, 3198, 3199, 3200, 3201, 3202);
        addStartNpc(30476);
        addTalkId(30476, 30614, 30463, 30613, 30114, 30210, 30358, 30419, 31845, 31850);
        addKillId(20144, 20157, 20158, 20192, 20193, 20202, 20230, 20232, 20233, 20234,
                20270, 20554, 20582, 20600, 27079);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q219_TestimonyOfFate");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30476-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3173, 1);
            if (!player.getMemos().getBool("secondClassChange37", false)) {
                htmltext = "30476-05a.htm";
                st.giveItems(7562, DF_REWARD_37.get(Integer.valueOf(player.getRace().ordinal())));
                player.getMemos().set("secondClassChange37", true);
            }
        } else if (event.equalsIgnoreCase("30114-04.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3185, 1);
            st.giveItems(3186, 1);
        } else if (event.equalsIgnoreCase("30476-12.htm")) {
            st.playSound("ItemSound.quest_middle");
            if (player.getLevel() < 38) {
                htmltext = "30476-13.htm";
                st.set("cond", "14");
                st.giveItems(3188, 1);
            } else {
                st.set("cond", "15");
                st.takeItems(3187, 1);
                st.giveItems(3189, 1);
            }
        } else if (event.equalsIgnoreCase("30419-02.htm")) {
            st.set("cond", "17");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3191, 1);
            st.giveItems(3192, 1);
        } else if (event.equalsIgnoreCase("31845-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3193, 1);
        } else if (event.equalsIgnoreCase("31850-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3199, 1);
        } else if (event.equalsIgnoreCase("30419-05.htm")) {
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3192, 1);
            st.takeItems(3201, 1);
            st.takeItems(3198, 1);
            st.giveItems(3202, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q219_TestimonyOfFate");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30476-02.htm";
                    break;
                }
                if (player.getLevel() < 37 || player.getClassId().level() != 1) {
                    htmltext = "30476-01.htm";
                    break;
                }
                htmltext = "30476-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30476:
                        if (cond == 1) {
                            htmltext = "30476-06.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30476-07.htm";
                            break;
                        }
                        if (cond > 3 && cond < 9) {
                            htmltext = "30476-08.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30476-09.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3184, 1);
                            addSpawn(30613, player, false, 0L, false);
                            break;
                        }
                        if (cond > 9 && cond < 13) {
                            htmltext = "30476-10.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30476-11.htm";
                            break;
                        }
                        if (cond == 14) {
                            if (player.getLevel() < 38) {
                                htmltext = "30476-14.htm";
                                break;
                            }
                            htmltext = "30476-12.htm";
                            st.set("cond", "15");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3188, 1);
                            st.takeItems(3187, 1);
                            st.giveItems(3189, 1);
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30476-16.htm";
                            break;
                        }
                        if (cond > 15)
                            htmltext = "30476-17.htm";
                        break;
                    case 30614:
                        if (cond == 1) {
                            htmltext = "30614-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3173, 1);
                            st.giveItems(3174, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30614-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30614-03.htm";
                            st.set("cond", "4");
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3175, 1);
                            st.giveItems(3176, 1);
                            break;
                        }
                        if (cond > 3 && cond < 8) {
                            htmltext = "30614-04.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30614-05.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3183, 1);
                            st.giveItems(3184, 1);
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30614-06.htm";
                        break;
                    case 30463:
                        if (cond == 5) {
                            htmltext = "30463-01.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3176, 1);
                            st.giveItems(3177, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30463-02.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30463-03.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3177, 1);
                            st.takeItems(3180, -1);
                            st.takeItems(3179, -1);
                            st.takeItems(3178, -1);
                            st.takeItems(3182, -1);
                            st.takeItems(3181, -1);
                            st.giveItems(3183, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30463-04.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30463-05.htm";
                        break;
                    case 30613:
                        if (cond == 10) {
                            htmltext = "30613-01.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(3185, 1);
                            npc.deleteMe();
                        }
                        break;
                    case 30114:
                        if (cond == 11) {
                            htmltext = "30114-01.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30114-05.htm";
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30114-06.htm";
                        break;
                    case 30210:
                        if (cond == 12) {
                            htmltext = "30210-01.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3186, 1);
                            st.giveItems(3187, 1);
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30210-02.htm";
                        break;
                    case 30358:
                        if (cond == 15) {
                            htmltext = "30358-01.htm";
                            st.set("cond", "16");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3189, 1);
                            st.giveItems(3190, 1);
                            st.giveItems(3191, 1);
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30358-02.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30358-03.htm";
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30358-04.htm";
                            st.takeItems(3190, 1);
                            st.takeItems(3202, 1);
                            st.giveItems(3172, 1);
                            st.rewardExpAndSp(68183L, 1750);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30419:
                        if (cond == 16) {
                            htmltext = "30419-01.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = (st.hasQuestItems(3201) && st.hasQuestItems(3198)) ? "30419-04.htm" : "30419-03.htm";
                            break;
                        }
                        if (cond == 18)
                            htmltext = "30419-06.htm";
                        break;
                    case 31845:
                        if (cond == 17) {
                            if (st.hasQuestItems(3193)) {
                                if (st.getQuestItemsCount(3194) >= 10 && st.getQuestItemsCount(3195) >= 10 && st.getQuestItemsCount(3196) >= 10 && st.getQuestItemsCount(3197) >= 10) {
                                    htmltext = "31845-04.htm";
                                    st.playSound("ItemSound.quest_itemget");
                                    st.takeItems(3196, -1);
                                    st.takeItems(3194, -1);
                                    st.takeItems(3195, -1);
                                    st.takeItems(3197, -1);
                                    st.takeItems(3193, 1);
                                    st.giveItems(3198, 1);
                                    break;
                                }
                                htmltext = "31845-03.htm";
                                break;
                            }
                            if (st.hasQuestItems(3198)) {
                                htmltext = "31845-05.htm";
                                break;
                            }
                            htmltext = "31845-01.htm";
                            break;
                        }
                        if (cond == 18)
                            htmltext = "31845-05.htm";
                        break;
                    case 31850:
                        if (cond == 17) {
                            if (st.hasQuestItems(3199)) {
                                if (st.hasQuestItems(3200)) {
                                    htmltext = "31850-04.htm";
                                    st.playSound("ItemSound.quest_itemget");
                                    st.takeItems(3200, 1);
                                    st.takeItems(3199, 1);
                                    st.giveItems(3201, 1);
                                    break;
                                }
                                htmltext = "31850-03.htm";
                                break;
                            }
                            if (st.hasQuestItems(3201)) {
                                htmltext = "31850-05.htm";
                                break;
                            }
                            htmltext = "31850-01.htm";
                            break;
                        }
                        if (cond == 18)
                            htmltext = "31850-05.htm";
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
            case 20144:
                if (st.getInt("cond") == 2) {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(3174, 1);
                    st.giveItems(3175, 1);
                }
                break;
            case 20202:
                if (st.getInt("cond") == 6 && st.dropItems(3180, 1, 10, CHANCES.get(Integer.valueOf(npcId))) &&
                        st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3182) >= 10 && st.getQuestItemsCount(3179) >= 10)
                    st.set("cond", "7");
                break;
            case 20192:
            case 20193:
                if (st.getInt("cond") == 6 && st.dropItems(3181, 1, 10, CHANCES.get(Integer.valueOf(npcId))) &&
                        st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3182) >= 10 && st.getQuestItemsCount(3179) >= 10)
                    st.set("cond", "7");
                break;
            case 20158:
                if (st.getInt("cond") == 6 && st.dropItems(3178, 1, 10, CHANCES.get(Integer.valueOf(npcId))) &&
                        st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3182) >= 10 && st.getQuestItemsCount(3179) >= 10)
                    st.set("cond", "7");
                break;
            case 20157:
            case 20230:
            case 20232:
            case 20234:
                if (st.getInt("cond") == 6 && st.dropItems(3182, 1, 10, CHANCES.get(Integer.valueOf(npcId))) &&
                        st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3179) >= 10)
                    st.set("cond", "7");
                break;
            case 20233:
                if (st.getInt("cond") == 6 && st.dropItems(3179, 1, 10, CHANCES.get(Integer.valueOf(npcId))) &&
                        st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3182) >= 10)
                    st.set("cond", "7");
                break;
            case 20554:
                if (st.hasQuestItems(3193))
                    st.dropItemsAlways(3194, 1, 10);
                break;
            case 20582:
                if (st.hasQuestItems(3193))
                    st.dropItemsAlways(3197, 1, 10);
                break;
            case 20270:
                if (st.hasQuestItems(3193))
                    st.dropItemsAlways(3196, 1, 10);
                break;
            case 20600:
                if (st.hasQuestItems(3193))
                    st.dropItemsAlways(3195, 1, 10);
                break;
            case 27079:
                if (st.hasQuestItems(3199))
                    st.dropItemsAlways(3200, 1, 1);
                break;
        }
        return null;
    }
}
