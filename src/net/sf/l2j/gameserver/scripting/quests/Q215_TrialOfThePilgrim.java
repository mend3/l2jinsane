package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q215_TrialOfThePilgrim extends Quest {
    private static final String qn = "Q215_TrialOfThePilgrim";

    private static final int BOOK_OF_SAGE = 2722;

    private static final int VOUCHER_OF_TRIAL = 2723;

    private static final int SPIRIT_OF_FLAME = 2724;

    private static final int ESSENCE_OF_FLAME = 2725;

    private static final int BOOK_OF_GERALD = 2726;

    private static final int GRAY_BADGE = 2727;

    private static final int PICTURE_OF_NAHIR = 2728;

    private static final int HAIR_OF_NAHIR = 2729;

    private static final int STATUE_OF_EINHASAD = 2730;

    private static final int BOOK_OF_DARKNESS = 2731;

    private static final int DEBRIS_OF_WILLOW = 2732;

    private static final int TAG_OF_RUMOR = 2733;

    private static final int MARK_OF_PILGRIM = 2721;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int SANTIAGO = 30648;

    private static final int TANAPI = 30571;

    private static final int ANCESTOR_MARTANKUS = 30649;

    private static final int GAURI_TWINKLEROCK = 30550;

    private static final int DORF = 30651;

    private static final int GERALD = 30650;

    private static final int PRIMOS = 30117;

    private static final int PETRON = 30036;

    private static final int ANDELLIA = 30362;

    private static final int URUHA = 30652;

    private static final int CASIAN = 30612;

    private static final int LAVA_SALAMANDER = 27116;

    private static final int NAHIR = 27117;

    private static final int BLACK_WILLOW = 27118;

    public Q215_TrialOfThePilgrim() {
        super(215, "Trial of the Pilgrim");
        setItemsIds(2722, 2723, 2724, 2725, 2726, 2727, 2728, 2729, 2730, 2731,
                2732, 2733);
        addStartNpc(30648);
        addTalkId(30648, 30571, 30649, 30550, 30651, 30650, 30117, 30036, 30362, 30652,
                30612);
        addKillId(27116, 27117, 27118);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q215_TrialOfThePilgrim");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30648-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2723, 1);
            if (!player.getMemos().getBool("secondClassChange35", false)) {
                htmltext = "30648-04a.htm";
                st.giveItems(7562, DF_REWARD_35.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange35", true);
            }
        } else if (event.equalsIgnoreCase("30649-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2725, 1);
            st.giveItems(2724, 1);
        } else if (event.equalsIgnoreCase("30650-02.htm")) {
            if (st.getQuestItemsCount(57) >= 100000) {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(57, 100000);
                st.giveItems(2726, 1);
            } else {
                htmltext = "30650-03.htm";
            }
        } else if (event.equalsIgnoreCase("30652-02.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2732, 1);
            st.giveItems(2731, 1);
        } else if (event.equalsIgnoreCase("30362-04.htm")) {
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30362-05.htm")) {
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2731, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q215_TrialOfThePilgrim");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.CLERIC && player.getClassId() != ClassId.ELVEN_ORACLE && player.getClassId() != ClassId.SHILLIEN_ORACLE && player.getClassId() != ClassId.ORC_SHAMAN) {
                    htmltext = "30648-02.htm";
                    break;
                }
                if (player.getLevel() < 35) {
                    htmltext = "30648-01.htm";
                    break;
                }
                htmltext = "30648-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30648:
                        if (cond < 17) {
                            htmltext = "30648-09.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30648-10.htm";
                            st.takeItems(2722, 1);
                            st.giveItems(2721, 1);
                            st.rewardExpAndSp(77382L, 16000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30571:
                        if (cond == 1) {
                            htmltext = "30571-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2723, 1);
                            break;
                        }
                        if (cond < 5) {
                            htmltext = "30571-02.htm";
                            break;
                        }
                        if (cond >= 5) {
                            htmltext = "30571-03.htm";
                            if (cond == 5) {
                                st.set("cond", "6");
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                        break;
                    case 30649:
                        if (cond == 2) {
                            htmltext = "30649-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30649-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30649-03.htm";
                        break;
                    case 30550:
                        if (cond == 6) {
                            htmltext = "30550-01.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2733, 1);
                            break;
                        }
                        if (cond > 6)
                            htmltext = "30550-02.htm";
                        break;
                    case 30651:
                        if (cond == 7) {
                            htmltext = !st.hasQuestItems(2726) ? "30651-01.htm" : "30651-02.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2733, 1);
                            st.giveItems(2727, 1);
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30651-03.htm";
                        break;
                    case 30650:
                        if (cond == 7 && !st.hasQuestItems(2726)) {
                            htmltext = "30650-01.htm";
                            break;
                        }
                        if (cond == 8 && st.hasQuestItems(2726)) {
                            htmltext = "30650-04.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(2726, 1);
                            st.giveItems(57, 100000);
                        }
                        break;
                    case 30117:
                        if (cond == 8) {
                            htmltext = "30117-01.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30117-02.htm";
                        break;
                    case 30036:
                        if (cond == 9) {
                            htmltext = "30036-01.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2728, 1);
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30036-02.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30036-03.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2729, 1);
                            st.takeItems(2728, 1);
                            st.giveItems(2730, 1);
                            break;
                        }
                        if (cond > 11)
                            htmltext = "30036-04.htm";
                        break;
                    case 30362:
                        if (cond == 12) {
                            if (player.getLevel() < 36) {
                                htmltext = "30362-01a.htm";
                                break;
                            }
                            htmltext = "30362-01.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 13) {
                            htmltext = Rnd.nextBoolean() ? "30362-02.htm" : "30362-02a.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30362-07.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30362-03.htm";
                            break;
                        }
                        if (cond == 16)
                            htmltext = "30362-06.htm";
                        break;
                    case 30652:
                        if (cond == 14) {
                            htmltext = "30652-01.htm";
                            break;
                        }
                        if (cond == 15)
                            htmltext = "30652-03.htm";
                        break;
                    case 30612:
                        if (cond == 16) {
                            htmltext = "30612-01.htm";
                            st.set("cond", "17");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2731, 1);
                            st.takeItems(2727, 1);
                            st.takeItems(2724, 1);
                            st.takeItems(2730, 1);
                            st.giveItems(2722, 1);
                            break;
                        }
                        if (cond == 17)
                            htmltext = "30612-02.htm";
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
            case 27116:
                if (st.getInt("cond") == 3 && st.dropItems(2725, 1, 1, 200000))
                    st.set("cond", "4");
                break;
            case 27117:
                if (st.getInt("cond") == 10 && st.dropItems(2729, 1, 1, 200000))
                    st.set("cond", "11");
                break;
            case 27118:
                if (st.getInt("cond") == 13 && st.dropItems(2732, 1, 1, 200000))
                    st.set("cond", "14");
                break;
        }
        return null;
    }
}
