package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q212_TrialOfDuty extends Quest {
    private static final String qn = "Q212_TrialOfDuty";

    private static final int LETTER_OF_DUSTIN = 2634;

    private static final int KNIGHTS_TEAR = 2635;

    private static final int MIRROR_OF_ORPIC = 2636;

    private static final int TEAR_OF_CONFESSION = 2637;

    private static final int REPORT_PIECE_1 = 2638;

    private static final int REPORT_PIECE_2 = 2639;

    private static final int TEAR_OF_LOYALTY = 2640;

    private static final int MILITAS_ARTICLE = 2641;

    private static final int SAINTS_ASHES_URN = 2642;

    private static final int ATHEBALDT_SKULL = 2643;

    private static final int ATHEBALDT_RIBS = 2644;

    private static final int ATHEBALDT_SHIN = 2645;

    private static final int LETTER_OF_WINDAWOOD = 2646;

    private static final int OLD_KNIGHT_SWORD = 3027;

    private static final int MARK_OF_DUTY = 2633;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int HANNAVALT = 30109;

    private static final int DUSTIN = 30116;

    private static final int SIR_COLLIN = 30311;

    private static final int SIR_ARON = 30653;

    private static final int SIR_KIEL = 30654;

    private static final int SILVERSHADOW = 30655;

    private static final int SPIRIT_TALIANUS = 30656;

    public Q212_TrialOfDuty() {
        super(212, "Trial of Duty");
        setItemsIds(2634, 2635, 2636, 2637, 2638, 2639, 2640, 2641, 2642, 2643,
                2644, 2645, 2646, 3027);
        addStartNpc(30109);
        addTalkId(30109, 30116, 30311, 30653, 30654, 30655, 30656);
        addKillId(20144, 20190, 20191, 20200, 20201, 20270, 27119, 20577, 20578, 20579,
                20580, 20581, 20582);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q212_TrialOfDuty");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30109-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            if (!player.getMemos().getBool("secondClassChange35", false)) {
                htmltext = "30109-04a.htm";
                st.giveItems(7562, DF_REWARD_35.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange35", true);
            }
        } else if (event.equalsIgnoreCase("30116-05.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2640, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = Quest.getNoQuestMsg();
        QuestState st = player.getQuestState("Q212_TrialOfDuty");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.ELVEN_KNIGHT && player.getClassId() != ClassId.PALUS_KNIGHT) {
                    htmltext = "30109-02.htm";
                    break;
                }
                if (player.getLevel() < 35) {
                    htmltext = "30109-01.htm";
                    break;
                }
                htmltext = "30109-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30109:
                        if (cond == 18) {
                            htmltext = "30109-05.htm";
                            st.takeItems(2634, 1);
                            st.giveItems(2633, 1);
                            st.rewardExpAndSp(79832L, 3750);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                            break;
                        }
                        htmltext = "30109-04a.htm";
                        break;
                    case 30653:
                        if (cond == 1) {
                            htmltext = "30653-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(3027, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30653-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30653-03.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2635, 1);
                            st.takeItems(3027, 1);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30653-04.htm";
                        break;
                    case 30654:
                        if (cond == 4) {
                            htmltext = "30654-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30654-02.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30654-03.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2636, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30654-04.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30654-05.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2637, 1);
                            break;
                        }
                        if (cond > 9)
                            htmltext = "30654-06.htm";
                        break;
                    case 30656:
                        if (cond == 8) {
                            htmltext = "30656-01.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2636, 1);
                            st.takeItems(2639, 1);
                            st.giveItems(2637, 1);
                            npc.deleteMe();
                        }
                        break;
                    case 30655:
                        if (cond == 10) {
                            if (player.getLevel() < 35) {
                                htmltext = "30655-01.htm";
                                break;
                            }
                            htmltext = "30655-02.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30655-03.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30655-04.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2641, -1);
                            st.giveItems(2640, 1);
                            break;
                        }
                        if (cond == 13)
                            htmltext = "30655-05.htm";
                        break;
                    case 30116:
                        if (cond == 13) {
                            htmltext = "30116-01.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30116-06.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30116-07.htm";
                            st.set("cond", "16");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2643, 1);
                            st.takeItems(2644, 1);
                            st.takeItems(2645, 1);
                            st.giveItems(2642, 1);
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30116-09.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30116-08.htm";
                            st.set("cond", "18");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2646, 1);
                            st.giveItems(2634, 1);
                            break;
                        }
                        if (cond == 18)
                            htmltext = "30116-10.htm";
                        break;
                    case 30311:
                        if (cond == 16) {
                            htmltext = "30311-01.htm";
                            st.set("cond", "17");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2642, 1);
                            st.giveItems(2646, 1);
                            break;
                        }
                        if (cond > 16)
                            htmltext = "30311-02.htm";
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
            case 20190:
            case 20191:
                if (cond == 2 && Rnd.get(10) < 1) {
                    st.playSound("Itemsound.quest_before_battle");
                    addSpawn(27119, npc, false, 120000L, true);
                }
                break;
            case 27119:
                if (cond == 2 && st.getItemEquipped(7) == 3027) {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2635, 1);
                }
                break;
            case 20200:
            case 20201:
                if (cond == 5 && st.dropItemsAlways(2638, 1, 10)) {
                    st.set("cond", "6");
                    st.takeItems(2638, -1);
                    st.giveItems(2639, 1);
                }
                break;
            case 20144:
                if ((cond == 7 || cond == 8) && Rnd.get(100) < 33) {
                    if (cond == 7) {
                        st.set("cond", "8");
                        st.playSound("ItemSound.quest_middle");
                    }
                    addSpawn(30656, npc, false, 300000L, true);
                }
                break;
            case 20577:
            case 20578:
            case 20579:
            case 20580:
            case 20581:
            case 20582:
                if (cond == 11 && st.dropItemsAlways(2641, 1, 20))
                    st.set("cond", "12");
                break;
            case 20270:
                if (cond == 14 && Rnd.nextBoolean()) {
                    if (!st.hasQuestItems(2643)) {
                        st.playSound("ItemSound.quest_itemget");
                        st.giveItems(2643, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2644)) {
                        st.playSound("ItemSound.quest_itemget");
                        st.giveItems(2644, 1);
                        break;
                    }
                    if (!st.hasQuestItems(2645)) {
                        st.set("cond", "15");
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(2645, 1);
                    }
                }
                break;
        }
        return null;
    }
}
