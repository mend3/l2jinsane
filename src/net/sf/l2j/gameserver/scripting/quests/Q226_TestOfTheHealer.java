package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q226_TestOfTheHealer extends Quest {
    private static final String qn = "Q226_TestOfTheHealer";

    private static final int REPORT_OF_PERRIN = 2810;

    private static final int KRISTINA_LETTER = 2811;

    private static final int PICTURE_OF_WINDY = 2812;

    private static final int GOLDEN_STATUE = 2813;

    private static final int WINDY_PEBBLES = 2814;

    private static final int ORDER_OF_SORIUS = 2815;

    private static final int SECRET_LETTER_1 = 2816;

    private static final int SECRET_LETTER_2 = 2817;

    private static final int SECRET_LETTER_3 = 2818;

    private static final int SECRET_LETTER_4 = 2819;

    private static final int MARK_OF_HEALER = 2820;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int BANDELLOS = 30473;

    private static final int SORIUS = 30327;

    private static final int ALLANA = 30424;

    private static final int PERRIN = 30428;

    private static final int GUPU = 30658;

    private static final int ORPHAN_GIRL = 30659;

    private static final int WINDY_SHAORING = 30660;

    private static final int MYSTERIOUS_DARKELF = 30661;

    private static final int PIPER_LONGBOW = 30662;

    private static final int SLEIN_SHINING_BLADE = 30663;

    private static final int KAIN_FLYING_KNIFE = 30664;

    private static final int KRISTINA = 30665;

    private static final int DAURIN_HAMMERCRUSH = 30674;

    private static final int LETO_LIZARDMAN_LEADER = 27123;

    private static final int LETO_LIZARDMAN_ASSASSIN = 27124;

    private static final int LETO_LIZARDMAN_SNIPER = 27125;

    private static final int LETO_LIZARDMAN_WIZARD = 27126;

    private static final int LETO_LIZARDMAN_LORD = 27127;

    private static final int TATOMA = 27134;

    private Npc _tatoma;

    private Npc _letoLeader;

    public Q226_TestOfTheHealer() {
        super(226, "Test of the Healer");
        setItemsIds(2810, 2811, 2812, 2813, 2814, 2815, 2816, 2817, 2818, 2819);
        addStartNpc(30473);
        addTalkId(30473, 30327, 30424, 30428, 30658, 30659, 30660, 30661, 30662, 30663,
                30664, 30665, 30674);
        addKillId(27123, 27124, 27125, 27126, 27127, 27134);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q226_TestOfTheHealer");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30473-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2810, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30473-04a.htm";
                st.giveItems(7562, DF_REWARD_39.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30473-09.htm")) {
            st.takeItems(2813, 1);
            st.giveItems(2820, 1);
            st.rewardExpAndSp(134839L, 50000);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30428-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            if (this._tatoma == null) {
                this._tatoma = addSpawn(27134, -93254, 147559, -2679, 0, false, 0L, false);
                startQuestTimer("tatoma_despawn", 200000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30658-02.htm")) {
            if (st.getQuestItemsCount(57) >= 100000) {
                st.set("cond", "7");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(57, 100000);
                st.giveItems(2812, 1);
            } else {
                htmltext = "30658-05.htm";
            }
        } else if (event.equalsIgnoreCase("30658-03.htm")) {
            st.set("gupu", "1");
        } else if (event.equalsIgnoreCase("30658-07.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30660-03.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2812, 1);
            st.giveItems(2814, 1);
        } else if (event.equalsIgnoreCase("30674-02.htm")) {
            st.set("cond", "11");
            st.playSound("Itemsound.quest_before_battle");
            st.takeItems(2815, 1);
            if (this._letoLeader == null) {
                this._letoLeader = addSpawn(27123, -97441, 106585, -3405, 0, false, 0L, false);
                startQuestTimer("leto_leader_despawn", 200000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30665-02.htm")) {
            st.set("cond", "22");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2816, 1);
            st.takeItems(2817, 1);
            st.takeItems(2818, 1);
            st.takeItems(2819, 1);
            st.giveItems(2811, 1);
        } else {
            if (event.equalsIgnoreCase("tatoma_despawn")) {
                this._tatoma.deleteMe();
                this._tatoma = null;
                return null;
            }
            if (event.equalsIgnoreCase("leto_leader_despawn")) {
                this._letoLeader.deleteMe();
                this._letoLeader = null;
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q226_TestOfTheHealer");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.ELVEN_KNIGHT && player.getClassId() != ClassId.CLERIC && player.getClassId() != ClassId.ELVEN_ORACLE) {
                    htmltext = "30473-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30473-02.htm";
                    break;
                }
                htmltext = "30473-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30473:
                        if (cond < 23) {
                            htmltext = "30473-05.htm";
                            break;
                        }
                        if (!st.hasQuestItems(2813)) {
                            htmltext = "30473-06.htm";
                            st.giveItems(2820, 1);
                            st.rewardExpAndSp(118304L, 26250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                            break;
                        }
                        htmltext = "30473-07.htm";
                        break;
                    case 30428:
                        if (cond < 3) {
                            htmltext = "30428-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30428-03.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2810, 1);
                            break;
                        }
                        htmltext = "30428-04.htm";
                        break;
                    case 30659:
                        htmltext = "30659-0" + Rnd.get(1, 5) + ".htm";
                        break;
                    case 30424:
                        if (cond == 4) {
                            htmltext = "30424-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30424-02.htm";
                        break;
                    case 30658:
                        if (st.getInt("gupu") == 1 && cond != 9) {
                            htmltext = "30658-07.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30658-01.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30658-01.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30658-04.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30658-06.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(2814, 1);
                            st.giveItems(2813, 1);
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30658-07.htm";
                        break;
                    case 30660:
                        if (cond == 7) {
                            htmltext = "30660-01.htm";
                            break;
                        }
                        if (st.hasQuestItems(2814))
                            htmltext = "30660-04.htm";
                        break;
                    case 30327:
                        if (cond == 9) {
                            htmltext = "30327-01.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2815, 1);
                            break;
                        }
                        if (cond > 9 && cond < 22) {
                            htmltext = "30327-02.htm";
                            break;
                        }
                        if (cond == 22) {
                            htmltext = "30327-03.htm";
                            st.set("cond", "23");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2811, 1);
                            break;
                        }
                        if (cond == 23)
                            htmltext = "30327-04.htm";
                        break;
                    case 30674:
                        if (cond == 10) {
                            htmltext = "30674-01.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30674-02a.htm";
                            if (this._letoLeader == null) {
                                this._letoLeader = addSpawn(27123, -97441, 106585, -3405, 0, false, 0L, false);
                                startQuestTimer("leto_leader_despawn", 200000L, null, player, false);
                            }
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30674-03.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30674-04.htm";
                        break;
                    case 30662:
                    case 30663:
                    case 30664:
                        if (cond == 13 || cond == 14) {
                            htmltext = npc.getNpcId() + "-01.htm";
                            break;
                        }
                        if (cond > 14 && cond < 19) {
                            htmltext = npc.getNpcId() + "-02.htm";
                            break;
                        }
                        if (cond > 18 && cond < 22) {
                            htmltext = npc.getNpcId() + "-03.htm";
                            st.set("cond", "21");
                            st.playSound("ItemSound.quest_middle");
                        }
                        break;
                    case 30661:
                        if (cond == 13) {
                            htmltext = "30661-01.htm";
                            st.set("cond", "14");
                            st.playSound("Itemsound.quest_before_battle");
                            addSpawn(27124, player, true, 0L, false);
                            addSpawn(27124, player, true, 0L, false);
                            addSpawn(27124, player, true, 0L, false);
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30661-01.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30661-02.htm";
                            st.set("cond", "16");
                            st.playSound("Itemsound.quest_before_battle");
                            addSpawn(27125, player, true, 0L, false);
                            addSpawn(27125, player, true, 0L, false);
                            addSpawn(27125, player, true, 0L, false);
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30661-02.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30661-03.htm";
                            st.set("cond", "18");
                            st.playSound("Itemsound.quest_before_battle");
                            addSpawn(27126, player, true, 0L, false);
                            addSpawn(27126, player, true, 0L, false);
                            addSpawn(27127, player, true, 0L, false);
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30661-03.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "30661-04.htm";
                            st.set("cond", "20");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 20 || cond == 21)
                            htmltext = "30661-04.htm";
                        break;
                    case 30665:
                        if (cond > 18 && cond < 22) {
                            htmltext = "30665-01.htm";
                            break;
                        }
                        if (cond > 21) {
                            htmltext = "30665-04.htm";
                            break;
                        }
                        htmltext = "30665-03.htm";
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
            case 27134:
                if (cond == 1 || cond == 2) {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                }
                this._tatoma = null;
                cancelQuestTimer("tatoma_despawn", null, player);
                break;
            case 27123:
                if (cond == 10 || cond == 11) {
                    st.set("cond", "12");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2816, 1);
                }
                this._letoLeader = null;
                cancelQuestTimer("leto_leader_despawn", null, player);
                break;
            case 27124:
                if (cond == 13 || cond == 14) {
                    st.set("cond", "15");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2817, 1);
                }
                break;
            case 27125:
                if (cond == 15 || cond == 16) {
                    st.set("cond", "17");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2818, 1);
                }
                break;
            case 27127:
                if (cond == 17 || cond == 18) {
                    st.set("cond", "19");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2819, 1);
                }
                break;
        }
        return null;
    }
}
