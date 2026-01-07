package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q222_TestOfTheDuelist extends Quest {
    private static final String qn = "Q222_TestOfTheDuelist";

    private static final int KAIEN = 30623;

    private static final int ORDER_GLUDIO = 2763;

    private static final int ORDER_DION = 2764;

    private static final int ORDER_GIRAN = 2765;

    private static final int ORDER_OREN = 2766;

    private static final int ORDER_ADEN = 2767;

    private static final int PUNCHER_SHARD = 2768;

    private static final int NOBLE_ANT_FEELER = 2769;

    private static final int DRONE_CHITIN = 2770;

    private static final int DEAD_SEEKER_FANG = 2771;

    private static final int OVERLORD_NECKLACE = 2772;

    private static final int FETTERED_SOUL_CHAIN = 2773;

    private static final int CHIEF_AMULET = 2774;

    private static final int ENCHANTED_EYE_MEAT = 2775;

    private static final int TAMRIN_ORC_RING = 2776;

    private static final int TAMRIN_ORC_ARROW = 2777;

    private static final int FINAL_ORDER = 2778;

    private static final int EXCURO_SKIN = 2779;

    private static final int KRATOR_SHARD = 2780;

    private static final int GRANDIS_SKIN = 2781;

    private static final int TIMAK_ORC_BELT = 2782;

    private static final int LAKIN_MACE = 2783;

    private static final int MARK_OF_DUELIST = 2762;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int PUNCHER = 20085;

    private static final int NOBLE_ANT_LEADER = 20090;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int DEAD_SEEKER = 20202;

    private static final int BREKA_ORC_OVERLORD = 20270;

    private static final int FETTERED_SOUL = 20552;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int ENCHANTED_MONSTEREYE = 20564;

    private static final int TAMLIN_ORC = 20601;

    private static final int TAMLIN_ORC_ARCHER = 20602;

    private static final int EXCURO = 20214;

    private static final int KRATOR = 20217;

    private static final int GRANDIS = 20554;

    private static final int TIMAK_ORC_OVERLORD = 20588;

    private static final int LAKIN = 20604;

    public Q222_TestOfTheDuelist() {
        super(222, "Test of the Duelist");
        setItemsIds(2763, 2764, 2765, 2766, 2767, 2778, 2768, 2769, 2770, 2771,
                2772, 2773, 2774, 2775, 2776, 2777, 2779, 2780, 2781, 2782,
                2783);
        addStartNpc(30623);
        addTalkId(30623);
        addKillId(20085, 20090, 20234, 20202, 20270, 20552, 20582, 20564, 20601, 20602,
                20214, 20217, 20554, 20588, 20604);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q222_TestOfTheDuelist");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30623-04.htm")) {
            if (player.getRace() == ClassRace.ORC)
                htmltext = "30623-05.htm";
        } else if (event.equalsIgnoreCase("30623-07.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("cond", "2");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2763, 1);
            st.giveItems(2764, 1);
            st.giveItems(2765, 1);
            st.giveItems(2766, 1);
            st.giveItems(2767, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30623-07a.htm";
                st.giveItems(7562, DF_REWARD_39.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30623-16.htm")) {
            if (st.getInt("cond") == 3) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(2763, 1);
                st.takeItems(2764, 1);
                st.takeItems(2765, 1);
                st.takeItems(2766, 1);
                st.takeItems(2767, 1);
                st.takeItems(2768, -1);
                st.takeItems(2769, -1);
                st.takeItems(2770, -1);
                st.takeItems(2771, -1);
                st.takeItems(2772, -1);
                st.takeItems(2773, -1);
                st.takeItems(2774, -1);
                st.takeItems(2775, -1);
                st.takeItems(2776, -1);
                st.takeItems(2777, -1);
                st.giveItems(2778, 1);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int classId, cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q222_TestOfTheDuelist");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                classId = player.getClassId().getId();
                if (classId != 1 && classId != 47 && classId != 19 && classId != 32) {
                    htmltext = "30623-02.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30623-01.htm";
                    break;
                }
                htmltext = "30623-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 2) {
                    htmltext = "30623-07a.htm";
                    break;
                }
                if (cond == 3) {
                    htmltext = "30623-13.htm";
                    break;
                }
                if (cond == 4) {
                    htmltext = "30623-17.htm";
                    break;
                }
                if (cond == 5) {
                    htmltext = "30623-18.htm";
                    st.takeItems(2778, 1);
                    st.takeItems(2779, -1);
                    st.takeItems(2780, -1);
                    st.takeItems(2781, -1);
                    st.takeItems(2782, -1);
                    st.takeItems(2783, -1);
                    st.giveItems(2762, 1);
                    st.rewardExpAndSp(47015L, 20000);
                    player.broadcastPacket(new SocialAction(player, 3));
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
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
        if (st.getInt("cond") == 2) {
            switch (npc.getNpcId()) {
                case 20085:
                    if (st.dropItemsAlways(2768, 1, 10) &&
                            st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20090:
                    if (st.dropItemsAlways(2769, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20234:
                    if (st.dropItemsAlways(2770, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20202:
                    if (st.dropItemsAlways(2771, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20270:
                    if (st.dropItemsAlways(2772, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20552:
                    if (st.dropItemsAlways(2773, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20582:
                    if (st.dropItemsAlways(2774, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20564:
                    if (st.dropItemsAlways(2775, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2776) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20601:
                    if (st.dropItemsAlways(2776, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2777) >= 10)
                        st.set("cond", "3");
                    break;
                case 20602:
                    if (st.dropItemsAlways(2777, 1, 10) &&
                            st.getQuestItemsCount(2768) >= 10 && st.getQuestItemsCount(2769) >= 10 && st.getQuestItemsCount(2770) >= 10 && st.getQuestItemsCount(2771) >= 10 && st.getQuestItemsCount(2772) >= 10 && st.getQuestItemsCount(2773) >= 10 && st.getQuestItemsCount(2774) >= 10 && st.getQuestItemsCount(2775) >= 10 && st.getQuestItemsCount(2776) >= 10)
                        st.set("cond", "3");
                    break;
            }
        } else if (st.getInt("cond") == 4) {
            switch (npc.getNpcId()) {
                case 20214:
                    if (st.dropItemsAlways(2779, 1, 3) &&
                            st.getQuestItemsCount(2780) >= 3 && st.getQuestItemsCount(2783) >= 3 && st.getQuestItemsCount(2781) >= 3 && st.getQuestItemsCount(2782) >= 3)
                        st.set("cond", "5");
                    break;
                case 20217:
                    if (st.dropItemsAlways(2780, 1, 3) &&
                            st.getQuestItemsCount(2779) >= 3 && st.getQuestItemsCount(2783) >= 3 && st.getQuestItemsCount(2781) >= 3 && st.getQuestItemsCount(2782) >= 3)
                        st.set("cond", "5");
                    break;
                case 20604:
                    if (st.dropItemsAlways(2783, 1, 3) &&
                            st.getQuestItemsCount(2779) >= 3 && st.getQuestItemsCount(2780) >= 3 && st.getQuestItemsCount(2781) >= 3 && st.getQuestItemsCount(2782) >= 3)
                        st.set("cond", "5");
                    break;
                case 20554:
                    if (st.dropItemsAlways(2781, 1, 3) &&
                            st.getQuestItemsCount(2779) >= 3 && st.getQuestItemsCount(2780) >= 3 && st.getQuestItemsCount(2783) >= 3 && st.getQuestItemsCount(2782) >= 3)
                        st.set("cond", "5");
                    break;
                case 20588:
                    if (st.dropItemsAlways(2782, 1, 3) &&
                            st.getQuestItemsCount(2779) >= 3 && st.getQuestItemsCount(2780) >= 3 && st.getQuestItemsCount(2783) >= 3 && st.getQuestItemsCount(2781) >= 3)
                        st.set("cond", "5");
                    break;
            }
        }
        return null;
    }
}
