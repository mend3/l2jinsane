package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q241_PossessorOfAPreciousSoul extends Quest {
    private static final String qn = "Q241_PossessorOfAPreciousSoul";

    private static final int TALIEN = 31739;

    private static final int GABRIELLE = 30753;

    private static final int GILMORE = 30754;

    private static final int KANTABILON = 31042;

    private static final int STEDMIEL = 30692;

    private static final int VIRGIL = 31742;

    private static final int OGMAR = 31744;

    private static final int RAHORAKTI = 31336;

    private static final int KASSANDRA = 31743;

    private static final int CARADINE = 31740;

    private static final int NOEL = 31272;

    private static final int BARAHAM = 27113;

    private static final int MALRUK_SUCCUBUS_1 = 20244;

    private static final int MALRUK_SUCCUBUS_TUREN_1 = 20245;

    private static final int MALRUK_SUCCUBUS_2 = 20283;

    private static final int MALRUK_SUCCUBUS_TUREN_2 = 20284;

    private static final int SPLINTER_STAKATO = 21508;

    private static final int SPLINTER_STAKATO_WALKER = 21509;

    private static final int SPLINTER_STAKATO_SOLDIER = 21510;

    private static final int SPLINTER_STAKATO_DRONE_1 = 21511;

    private static final int SPLINTER_STAKATO_DRONE_2 = 21512;

    private static final int LEGEND_OF_SEVENTEEN = 7587;

    private static final int MALRUK_SUCCUBUS_CLAW = 7597;

    private static final int ECHO_CRYSTAL = 7589;

    private static final int POETRY_BOOK = 7588;

    private static final int CRIMSON_MOSS = 7598;

    private static final int RAHORAKTI_MEDICINE = 7599;

    private static final int LUNARGENT = 6029;

    private static final int HELLFIRE_OIL = 6033;

    private static final int VIRGIL_LETTER = 7677;

    public Q241_PossessorOfAPreciousSoul() {
        super(241, "Possessor of a Precious Soul - 1");
        setItemsIds(7587, 7597, 7589, 7588, 7598, 7599);
        addStartNpc(31739);
        addTalkId(31739, 30753, 30754, 31042, 30692, 31742, 31744, 31336, 31743, 31740,
                31272);
        addKillId(27113, 20244, 20283, 20245, 20284, 21508, 21509, 21510, 21511, 21512);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q241_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31739-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31739-07.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7587, 1);
        } else if (event.equalsIgnoreCase("31739-10.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7589, 1);
        } else if (event.equalsIgnoreCase("31739-13.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7588, 1);
        } else if (event.equalsIgnoreCase("30753-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30754-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31042-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31042-05.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7597, -1);
            st.giveItems(7589, 1);
        } else if (event.equalsIgnoreCase("30692-02.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7588, 1);
        } else if (event.equalsIgnoreCase("31742-02.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31742-05.htm")) {
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31744-02.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31336-02.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31336-05.htm")) {
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7598, -1);
            st.giveItems(7599, 1);
        } else if (event.equalsIgnoreCase("31743-02.htm")) {
            st.set("cond", "17");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7599, 1);
        } else if (event.equalsIgnoreCase("31740-02.htm")) {
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31740-05.htm")) {
            st.giveItems(7677, 1);
            st.rewardExpAndSp(263043L, 0);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("31272-02.htm")) {
            st.set("cond", "20");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31272-05.htm")) {
            if (st.hasQuestItems(6033) && st.getQuestItemsCount(6029) >= 5) {
                st.set("cond", "21");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(6029, 5);
                st.takeItems(6033, 1);
            } else {
                htmltext = "31272-07.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q241_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (!player.isSubClassActive() || player.getLevel() < 50) ? "31739-02.htm" : "31739-01.htm";
                break;
            case 1:
                if (!player.isSubClassActive())
                    break;
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31739:
                        if (cond == 1) {
                            htmltext = "31739-04.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "31739-05.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31739-06.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "31739-08.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "31739-09.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "31739-11.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "31739-12.htm";
                            break;
                        }
                        if (cond == 11)
                            htmltext = "31739-14.htm";
                        break;
                    case 30753:
                        if (cond == 1) {
                            htmltext = "30753-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30753-03.htm";
                        break;
                    case 30754:
                        if (cond == 2) {
                            htmltext = "30754-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30754-03.htm";
                        break;
                    case 31042:
                        if (cond == 5) {
                            htmltext = "31042-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "31042-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "31042-04.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "31042-06.htm";
                        break;
                    case 30692:
                        if (cond == 9) {
                            htmltext = "30692-01.htm";
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30692-03.htm";
                        break;
                    case 31742:
                        if (cond == 11) {
                            htmltext = "31742-01.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "31742-03.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "31742-04.htm";
                            break;
                        }
                        if (cond == 18)
                            htmltext = "31742-06.htm";
                        break;
                    case 31744:
                        if (cond == 12) {
                            htmltext = "31744-01.htm";
                            break;
                        }
                        if (cond == 13)
                            htmltext = "31744-03.htm";
                        break;
                    case 31336:
                        if (cond == 13) {
                            htmltext = "31336-01.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "31336-03.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "31336-04.htm";
                            break;
                        }
                        if (cond == 16)
                            htmltext = "31336-06.htm";
                        break;
                    case 31743:
                        if (cond == 16) {
                            htmltext = "31743-01.htm";
                            break;
                        }
                        if (cond == 17)
                            htmltext = "31743-03.htm";
                        break;
                    case 31740:
                        if (cond == 18) {
                            htmltext = "31740-01.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "31740-03.htm";
                            break;
                        }
                        if (cond == 21)
                            htmltext = "31740-04.htm";
                        break;
                    case 31272:
                        if (cond == 19) {
                            htmltext = "31272-01.htm";
                            break;
                        }
                        if (cond == 20) {
                            if (st.hasQuestItems(6033) && st.getQuestItemsCount(6029) >= 5) {
                                htmltext = "31272-04.htm";
                                break;
                            }
                            htmltext = "31272-03.htm";
                            break;
                        }
                        if (cond == 21)
                            htmltext = "31272-06.htm";
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
        if (st == null || !player.isSubClassActive())
            return null;
        switch (npc.getNpcId()) {
            case 27113:
                if (st.getInt("cond") == 3) {
                    st.set("cond", "4");
                    st.giveItems(7587, 1);
                    st.playSound("ItemSound.quest_middle");
                }
                break;
            case 20244:
            case 20283:
                if (st.getInt("cond") == 6 && st.dropItems(7597, 1, 10, 100000))
                    st.set("cond", "7");
                break;
            case 20245:
            case 20284:
                if (st.getInt("cond") == 6 && st.dropItems(7597, 1, 10, 120000))
                    st.set("cond", "7");
                break;
            case 21508:
            case 21509:
            case 21510:
            case 21511:
            case 21512:
                if (st.getInt("cond") == 14 && st.dropItems(7598, 1, 5, 100000))
                    st.set("cond", "15");
                break;
        }
        return null;
    }
}
