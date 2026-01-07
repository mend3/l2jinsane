package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q233_TestOfTheWarSpirit extends Quest {
    private static final String qn = "Q233_TestOfTheWarSpirit";

    private static final int VENDETTA_TOTEM = 2880;

    private static final int TAMLIN_ORC_HEAD = 2881;

    private static final int WARSPIRIT_TOTEM = 2882;

    private static final int ORIM_CONTRACT = 2883;

    private static final int PORTA_EYE = 2884;

    private static final int EXCURO_SCALE = 2885;

    private static final int MORDEO_TALON = 2886;

    private static final int BRAKI_REMAINS_1 = 2887;

    private static final int PEKIRON_TOTEM = 2888;

    private static final int TONAR_SKULL = 2889;

    private static final int TONAR_RIBBONE = 2890;

    private static final int TONAR_SPINE = 2891;

    private static final int TONAR_ARMBONE = 2892;

    private static final int TONAR_THIGHBONE = 2893;

    private static final int TONAR_REMAINS_1 = 2894;

    private static final int MANAKIA_TOTEM = 2895;

    private static final int HERMODT_SKULL = 2896;

    private static final int HERMODT_RIBBONE = 2897;

    private static final int HERMODT_SPINE = 2898;

    private static final int HERMODT_ARMBONE = 2899;

    private static final int HERMODT_THIGHBONE = 2900;

    private static final int HERMODT_REMAINS_1 = 2901;

    private static final int RACOY_TOTEM = 2902;

    private static final int VIVYAN_LETTER = 2903;

    private static final int INSECT_DIAGRAM_BOOK = 2904;

    private static final int KIRUNA_SKULL = 2905;

    private static final int KIRUNA_RIBBONE = 2906;

    private static final int KIRUNA_SPINE = 2907;

    private static final int KIRUNA_ARMBONE = 2908;

    private static final int KIRUNA_THIGHBONE = 2909;

    private static final int KIRUNA_REMAINS_1 = 2910;

    private static final int BRAKI_REMAINS_2 = 2911;

    private static final int TONAR_REMAINS_2 = 2912;

    private static final int HERMODT_REMAINS_2 = 2913;

    private static final int KIRUNA_REMAINS_2 = 2914;

    private static final int MARK_OF_WARSPIRIT = 2879;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int VIVYAN = 30030;

    private static final int SARIEN = 30436;

    private static final int RACOY = 30507;

    private static final int SOMAK = 30510;

    private static final int MANAKIA = 30515;

    private static final int ORIM = 30630;

    private static final int ANCESTOR_MARTANKUS = 30649;

    private static final int PEKIRON = 30682;

    private static final int NOBLE_ANT = 20089;

    private static final int NOBLE_ANT_LEADER = 20090;

    private static final int MEDUSA = 20158;

    private static final int PORTA = 20213;

    private static final int EXCURO = 20214;

    private static final int MORDEO = 20215;

    private static final int LETO_LIZARDMAN_SHAMAN = 20581;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int TAMLIN_ORC = 20601;

    private static final int TAMLIN_ORC_ARCHER = 20602;

    private static final int STENOA_GORGON_QUEEN = 27108;

    public Q233_TestOfTheWarSpirit() {
        super(233, "Test of the War Spirit");
        setItemsIds(2880, 2881, 2882, 2883, 2884, 2885, 2886, 2887, 2888, 2889,
                2890, 2891, 2892, 2893, 2894, 2895, 2896, 2897, 2898, 2899,
                2900, 2901, 2902, 2903, 2904, 2905, 2906, 2907, 2908, 2909,
                2910, 2911, 2912, 2913, 2914);
        addStartNpc(30510);
        addTalkId(30510, 30030, 30436, 30507, 30515, 30630, 30649, 30682);
        addKillId(20089, 20090, 20158, 20213, 20214, 20215, 20581, 20582, 20601, 20602,
                27108);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q233_TestOfTheWarSpirit");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30510-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30510-05e.htm";
                st.giveItems(7562, DF_REWARD_39.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30630-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2883, 1);
        } else if (event.equalsIgnoreCase("30507-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2902, 1);
        } else if (event.equalsIgnoreCase("30030-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2903, 1);
        } else if (event.equalsIgnoreCase("30682-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2888, 1);
        } else if (event.equalsIgnoreCase("30515-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2895, 1);
        } else if (event.equalsIgnoreCase("30649-03.htm")) {
            st.takeItems(2881, -1);
            st.takeItems(2882, -1);
            st.takeItems(2911, -1);
            st.takeItems(2913, -1);
            st.takeItems(2914, -1);
            st.takeItems(2912, -1);
            st.giveItems(2879, 1);
            st.rewardExpAndSp(63483L, 17500);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q233_TestOfTheWarSpirit");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() == ClassId.ORC_SHAMAN) {
                    htmltext = (player.getLevel() < 39) ? "30510-03.htm" : "30510-04.htm";
                    break;
                }
                htmltext = (player.getRace() == ClassRace.ORC) ? "30510-02.htm" : "30510-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30510:
                        if (cond == 1) {
                            htmltext = "30510-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30510-07.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2887, 1);
                            st.takeItems(2901, 1);
                            st.takeItems(2910, 1);
                            st.takeItems(2894, 1);
                            st.giveItems(2880, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30510-08.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30510-09.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2880, 1);
                            st.giveItems(2911, 1);
                            st.giveItems(2913, 1);
                            st.giveItems(2914, 1);
                            st.giveItems(2912, 1);
                            st.giveItems(2882, 1);
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30510-10.htm";
                        break;
                    case 30630:
                        if (cond == 1 && !st.hasQuestItems(2887)) {
                            if (!st.hasQuestItems(2883)) {
                                htmltext = "30630-01.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(2884) + st.getQuestItemsCount(2885) + st.getQuestItemsCount(2886) == 30) {
                                htmltext = "30630-06.htm";
                                st.takeItems(2885, 10);
                                st.takeItems(2886, 10);
                                st.takeItems(2884, 10);
                                st.takeItems(2883, 1);
                                st.giveItems(2887, 1);
                                if (st.hasQuestItems(2901, 2910, 2894)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30630-05.htm";
                            break;
                        }
                        htmltext = "30630-07.htm";
                        break;
                    case 30507:
                        if (cond == 1 && !st.hasQuestItems(2910)) {
                            if (!st.hasQuestItems(2902)) {
                                htmltext = "30507-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(2903)) {
                                htmltext = "30507-04.htm";
                                break;
                            }
                            if (st.hasQuestItems(2904)) {
                                if (st.hasQuestItems(2908, 2906, 2905, 2907, 2909)) {
                                    htmltext = "30507-06.htm";
                                    st.takeItems(2904, 1);
                                    st.takeItems(2902, 1);
                                    st.takeItems(2908, 1);
                                    st.takeItems(2906, 1);
                                    st.takeItems(2905, 1);
                                    st.takeItems(2907, 1);
                                    st.takeItems(2909, 1);
                                    st.giveItems(2910, 1);
                                    if (st.hasQuestItems(2887, 2901, 2894)) {
                                        st.set("cond", "2");
                                        st.playSound("ItemSound.quest_middle");
                                        break;
                                    }
                                    st.playSound("ItemSound.quest_itemget");
                                    break;
                                }
                                htmltext = "30507-05.htm";
                                break;
                            }
                            htmltext = "30507-03.htm";
                            break;
                        }
                        htmltext = "30507-07.htm";
                        break;
                    case 30030:
                        if (cond == 1 && st.hasQuestItems(2902)) {
                            if (st.hasQuestItems(2903)) {
                                htmltext = "30030-05.htm";
                                break;
                            }
                            if (st.hasQuestItems(2904)) {
                                htmltext = "30030-06.htm";
                                break;
                            }
                            htmltext = "30030-01.htm";
                            break;
                        }
                        htmltext = "30030-07.htm";
                        break;
                    case 30436:
                        if (cond == 1 && st.hasQuestItems(2902)) {
                            if (st.hasQuestItems(2903)) {
                                htmltext = "30436-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(2903, 1);
                                st.giveItems(2904, 1);
                                break;
                            }
                            if (st.hasQuestItems(2904))
                                htmltext = "30436-02.htm";
                            break;
                        }
                        htmltext = "30436-03.htm";
                        break;
                    case 30682:
                        if (cond == 1 && !st.hasQuestItems(2894)) {
                            if (!st.hasQuestItems(2888)) {
                                htmltext = "30682-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(2892, 2890, 2889, 2891, 2893)) {
                                htmltext = "30682-04.htm";
                                st.takeItems(2888, 1);
                                st.takeItems(2892, 1);
                                st.takeItems(2890, 1);
                                st.takeItems(2889, 1);
                                st.takeItems(2891, 1);
                                st.takeItems(2893, 1);
                                st.giveItems(2894, 1);
                                if (st.hasQuestItems(2887, 2901, 2910)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30682-03.htm";
                            break;
                        }
                        htmltext = "30682-05.htm";
                        break;
                    case 30515:
                        if (cond == 1 && !st.hasQuestItems(2901)) {
                            if (!st.hasQuestItems(2895)) {
                                htmltext = "30515-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(2899, 2897, 2896, 2898, 2900)) {
                                htmltext = "30515-04.htm";
                                st.takeItems(2895, 1);
                                st.takeItems(2899, 1);
                                st.takeItems(2897, 1);
                                st.takeItems(2896, 1);
                                st.takeItems(2898, 1);
                                st.takeItems(2900, 1);
                                st.giveItems(2901, 1);
                                if (st.hasQuestItems(2887, 2910, 2894)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30515-03.htm";
                            break;
                        }
                        htmltext = "30515-05.htm";
                        break;
                    case 30649:
                        if (cond == 5)
                            htmltext = "30649-01.htm";
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
            case 20213:
                if (st.hasQuestItems(2883))
                    st.dropItemsAlways(2884, 1, 10);
                break;
            case 20214:
                if (st.hasQuestItems(2883))
                    st.dropItemsAlways(2885, 1, 10);
                break;
            case 20215:
                if (st.hasQuestItems(2883))
                    st.dropItemsAlways(2886, 1, 10);
                break;
            case 20089:
            case 20090:
                if (st.hasQuestItems(2904)) {
                    int rndAnt = Rnd.get(100);
                    if (rndAnt > 70) {
                        if (st.hasQuestItems(2909)) {
                            st.dropItemsAlways(2908, 1, 1);
                            break;
                        }
                        st.dropItemsAlways(2909, 1, 1);
                        break;
                    }
                    if (rndAnt > 40) {
                        if (st.hasQuestItems(2907)) {
                            st.dropItemsAlways(2906, 1, 1);
                            break;
                        }
                        st.dropItemsAlways(2907, 1, 1);
                        break;
                    }
                    if (rndAnt > 10)
                        st.dropItemsAlways(2905, 1, 1);
                }
                break;
            case 20581:
            case 20582:
                if (st.hasQuestItems(2888) && Rnd.nextBoolean()) {
                    if (!st.hasQuestItems(2889)) {
                        st.dropItemsAlways(2889, 1, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2890)) {
                        st.dropItemsAlways(2890, 1, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2891)) {
                        st.dropItemsAlways(2891, 1, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2892)) {
                        st.dropItemsAlways(2892, 1, 1);
                        break;
                    }
                    st.dropItemsAlways(2893, 1, 1);
                }
                break;
            case 20158:
                if (st.hasQuestItems(2895) && Rnd.nextBoolean()) {
                    if (!st.hasQuestItems(2897)) {
                        st.dropItemsAlways(2897, 1, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2898)) {
                        st.dropItemsAlways(2898, 1, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2899)) {
                        st.dropItemsAlways(2899, 1, 1);
                        break;
                    }
                    st.dropItemsAlways(2900, 1, 1);
                }
                break;
            case 27108:
                if (st.hasQuestItems(2895))
                    st.dropItemsAlways(2896, 1, 1);
                break;
            case 20601:
            case 20602:
                if (st.hasQuestItems(2880) && st.dropItems(2881, 1, 13, 500000))
                    st.set("cond", "4");
                break;
        }
        return null;
    }
}
