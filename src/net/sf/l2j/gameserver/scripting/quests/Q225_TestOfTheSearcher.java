package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q225_TestOfTheSearcher extends Quest {
    private static final String qn = "Q225_TestOfTheSearcher";

    private static final int LUTHER_LETTER = 2784;

    private static final int ALEX_WARRANT = 2785;

    private static final int LEIRYNN_ORDER_1 = 2786;

    private static final int DELU_TOTEM = 2787;

    private static final int LEIRYNN_ORDER_2 = 2788;

    private static final int CHIEF_KALKI_FANG = 2789;

    private static final int LEIRYNN_REPORT = 2790;

    private static final int STRANGE_MAP = 2791;

    private static final int LAMBERT_MAP = 2792;

    private static final int ALEX_LETTER = 2793;

    private static final int ALEX_ORDER = 2794;

    private static final int WINE_CATALOG = 2795;

    private static final int TYRA_CONTRACT = 2796;

    private static final int RED_SPORE_DUST = 2797;

    private static final int MALRUKIAN_WINE = 2798;

    private static final int OLD_ORDER = 2799;

    private static final int JAX_DIARY = 2800;

    private static final int TORN_MAP_PIECE_1 = 2801;

    private static final int TORN_MAP_PIECE_2 = 2802;

    private static final int SOLT_MAP = 2803;

    private static final int MAKEL_MAP = 2804;

    private static final int COMBINED_MAP = 2805;

    private static final int RUSTED_KEY = 2806;

    private static final int GOLD_BAR = 2807;

    private static final int ALEX_RECOMMEND = 2808;

    private static final int MARK_OF_SEARCHER = 2809;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int ALEX = 30291;

    private static final int TYRA = 30420;

    private static final int TREE = 30627;

    private static final int STRONG_WOODEN_CHEST = 30628;

    private static final int LUTHER = 30690;

    private static final int LEIRYNN = 30728;

    private static final int BORYS = 30729;

    private static final int JAX = 30730;

    private static final int HANGMAN_TREE = 20144;

    private static final int ROAD_SCAVENGER = 20551;

    private static final int GIANT_FUNGUS = 20555;

    private static final int DELU_LIZARDMAN_SHAMAN = 20781;

    private static final int DELU_CHIEF_KALKIS = 27093;

    private static final int NEER_BODYGUARD = 27092;

    private static Npc _strongWoodenChest;

    public Q225_TestOfTheSearcher() {
        super(225, "Test of the Searcher");
        setItemsIds(2784, 2785, 2786, 2787, 2788, 2789, 2790, 2791, 2792, 2793,
                2794, 2795, 2796, 2797, 2798, 2799, 2800, 2801, 2802, 2803,
                2804, 2805, 2806, 2807, 2808);
        addStartNpc(30690);
        addTalkId(30291, 30420, 30627, 30628, 30690, 30728, 30729, 30730);
        addAttackId(20781);
        addKillId(20144, 20551, 20555, 20781, 27093, 27092);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q225_TestOfTheSearcher");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30690-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2784, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30690-05a.htm";
                st.giveItems(7562, DF_REWARD_39.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30291-07.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2790, 1);
            st.takeItems(2791, 1);
            st.giveItems(2793, 1);
            st.giveItems(2794, 1);
            st.giveItems(2792, 1);
        } else if (event.equalsIgnoreCase("30420-01a.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2795, 1);
            st.giveItems(2796, 1);
        } else if (event.equalsIgnoreCase("30730-01d.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2799, 1);
            st.giveItems(2800, 1);
        } else if (event.equalsIgnoreCase("30627-01a.htm")) {
            if (_strongWoodenChest == null) {
                if (st.getInt("cond") == 16) {
                    st.set("cond", "17");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2806, 1);
                }
                _strongWoodenChest = addSpawn(30628, 10098, 157287, -2406, 0, false, 0L, true);
                startQuestTimer("chest_despawn", 300000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30628-01a.htm")) {
            if (!st.hasQuestItems(2806)) {
                htmltext = "30628-02.htm";
            } else {
                st.set("cond", "18");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(2806, -1);
                st.giveItems(2807, 20);
                _strongWoodenChest.deleteMe();
                _strongWoodenChest = null;
                cancelQuestTimer("chest_despawn", null, player);
            }
        } else if (event.equalsIgnoreCase("chest_despawn")) {
            _strongWoodenChest.deleteMe();
            _strongWoodenChest = null;
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q225_TestOfTheSearcher");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.ROGUE && player.getClassId() != ClassId.ELVEN_SCOUT && player.getClassId() != ClassId.ASSASSIN && player.getClassId() != ClassId.SCAVENGER) {
                    htmltext = "30690-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30690-02.htm";
                    break;
                }
                htmltext = (player.getClassId() == ClassId.SCAVENGER) ? "30690-04.htm" : "30690-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30690:
                        if (cond == 1) {
                            htmltext = "30690-06.htm";
                            break;
                        }
                        if (cond > 1 && cond < 19) {
                            htmltext = "30690-07.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "30690-08.htm";
                            st.takeItems(2808, 1);
                            st.giveItems(2809, 1);
                            st.rewardExpAndSp(37831L, 18750);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30291:
                        if (cond == 1) {
                            htmltext = "30291-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2784, 1);
                            st.giveItems(2785, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30291-02.htm";
                            break;
                        }
                        if (cond > 2 && cond < 7) {
                            htmltext = "30291-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30291-04.htm";
                            break;
                        }
                        if (cond > 7 && cond < 13) {
                            htmltext = "30291-08.htm";
                            break;
                        }
                        if (cond > 12 && cond < 16) {
                            htmltext = "30291-09.htm";
                            break;
                        }
                        if (cond > 15 && cond < 18) {
                            htmltext = "30291-10.htm";
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30291-11.htm";
                            st.set("cond", "19");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2794, 1);
                            st.takeItems(2805, 1);
                            st.takeItems(2807, -1);
                            st.giveItems(2808, 1);
                            break;
                        }
                        if (cond == 19)
                            htmltext = "30291-12.htm";
                        break;
                    case 30728:
                        if (cond == 2) {
                            htmltext = "30728-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2785, 1);
                            st.giveItems(2786, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30728-02.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30728-03.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2787, -1);
                            st.takeItems(2786, 1);
                            st.giveItems(2788, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30728-04.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30728-05.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2789, 1);
                            st.takeItems(2788, 1);
                            st.giveItems(2790, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30728-06.htm";
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30728-07.htm";
                        break;
                    case 30729:
                        if (cond == 8) {
                            htmltext = "30729-01.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2793, 1);
                            st.giveItems(2795, 1);
                            break;
                        }
                        if (cond > 8 && cond < 12) {
                            htmltext = "30729-02.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30729-03.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2798, 1);
                            st.takeItems(2795, 1);
                            st.giveItems(2799, 1);
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30729-04.htm";
                            break;
                        }
                        if (cond > 13)
                            htmltext = "30729-05.htm";
                        break;
                    case 30420:
                        if (cond == 9) {
                            htmltext = "30420-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30420-02.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30420-03.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2797, -1);
                            st.takeItems(2796, 1);
                            st.giveItems(2798, 1);
                            break;
                        }
                        if (cond > 11)
                            htmltext = "30420-04.htm";
                        break;
                    case 30730:
                        if (cond == 13) {
                            htmltext = "30730-01.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30730-02.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30730-03.htm";
                            st.set("cond", "16");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2792, 1);
                            st.takeItems(2804, 1);
                            st.takeItems(2800, 1);
                            st.takeItems(2803, 1);
                            st.giveItems(2805, 1);
                            break;
                        }
                        if (cond > 15)
                            htmltext = "30730-04.htm";
                        break;
                    case 30627:
                        if (cond == 16 || cond == 17)
                            htmltext = "30627-01.htm";
                        break;
                    case 30628:
                        if (cond == 17)
                            htmltext = "30628-01.htm";
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
        if (st.hasQuestItems(2786) && !npc.isScriptValue(1)) {
            npc.setScriptValue(1);
            addSpawn(27092, npc, false, 200000L, true);
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        QuestState st;
        Player player = killer.getActingPlayer();
        switch (npc.getNpcId()) {
            case 20781:
                st = checkPlayerCondition(player, npc, "cond", "3");
                if (st == null)
                    return null;
                if (st.dropItemsAlways(2787, 1, 10))
                    st.set("cond", "4");
                break;
            case 27093:
                st = checkPlayerCondition(player, npc, "cond", "5");
                if (st == null)
                    return null;
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(2789, 1);
                st.giveItems(2791, 1);
                break;
            case 20555:
                st = checkPlayerCondition(player, npc, "cond", "10");
                if (st == null)
                    return null;
                if (st.dropItemsAlways(2797, 1, 10))
                    st.set("cond", "11");
                break;
            case 20551:
                st = checkPlayerCondition(player, npc, "cond", "14");
                if (st == null)
                    return null;
                if (!st.hasQuestItems(2803) && st.dropItems(2801, 1, 4, 500000)) {
                    st.takeItems(2801, -1);
                    st.giveItems(2803, 1);
                    if (st.hasQuestItems(2804))
                        st.set("cond", "15");
                }
                break;
            case 20144:
                st = checkPlayerCondition(player, npc, "cond", "14");
                if (st == null)
                    return null;
                if (!st.hasQuestItems(2804) && st.dropItems(2802, 1, 4, 500000)) {
                    st.takeItems(2802, -1);
                    st.giveItems(2804, 1);
                    if (st.hasQuestItems(2803))
                        st.set("cond", "15");
                }
                break;
        }
        return null;
    }
}
