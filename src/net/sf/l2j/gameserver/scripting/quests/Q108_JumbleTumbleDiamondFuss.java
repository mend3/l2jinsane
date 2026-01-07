package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q108_JumbleTumbleDiamondFuss extends Quest {
    private static final String qn = "Q108_JumbleTumbleDiamondFuss";

    private static final int GOUPH = 30523;

    private static final int REEP = 30516;

    private static final int MURDOC = 30521;

    private static final int AIRY = 30522;

    private static final int BRUNON = 30526;

    private static final int MARON = 30529;

    private static final int TOROCCO = 30555;

    private static final int GOUPH_CONTRACT = 1559;

    private static final int REEP_CONTRACT = 1560;

    private static final int ELVEN_WINE = 1561;

    private static final int BRUNON_DICE = 1562;

    private static final int BRUNON_CONTRACT = 1563;

    private static final int AQUAMARINE = 1564;

    private static final int CHRYSOBERYL = 1565;

    private static final int GEM_BOX = 1566;

    private static final int COAL_PIECE = 1567;

    private static final int BRUNON_LETTER = 1568;

    private static final int BERRY_TART = 1569;

    private static final int BAT_DIAGRAM = 1570;

    private static final int STAR_DIAMOND = 1571;

    private static final int GOBLIN_BRIGAND_LEADER = 20323;

    private static final int GOBLIN_BRIGAND_LIEUTENANT = 20324;

    private static final int BLADE_BAT = 20480;

    private static final int SILVERSMITH_HAMMER = 1511;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int LESSER_HEALING_POTION = 1060;

    private static final int[][] LEADER_DROPLIST = new int[][]{{1564, 1, 10, 800000}, {1565, 1, 10, 800000}};

    private static final int[][] LIEUTENANT_DROPLIST = new int[][]{{1564, 1, 10, 600000}, {1565, 1, 10, 600000}};

    public Q108_JumbleTumbleDiamondFuss() {
        super(108, "Jumble, Tumble, Diamond Fuss");
        setItemsIds(1559, 1560, 1561, 1562, 1563, 1564, 1565, 1566, 1567, 1568,
                1569, 1570, 1571);
        addStartNpc(30523);
        addTalkId(30523, 30516, 30521, 30522, 30526, 30529, 30555);
        addKillId(20323, 20324, 20480);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState("Q108_JumbleTumbleDiamondFuss");
        String htmltext = event;
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30523-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1559, 1);
        } else if (event.equalsIgnoreCase("30555-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1560, 1);
            st.giveItems(1561, 1);
        } else if (event.equalsIgnoreCase("30526-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1562, 1);
            st.giveItems(1563, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q108_JumbleTumbleDiamondFuss");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DWARF) {
                    htmltext = "30523-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30523-01.htm";
                    break;
                }
                htmltext = "30523-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30523:
                        if (cond == 1) {
                            htmltext = "30523-04.htm";
                            break;
                        }
                        if (cond > 1 && cond < 7) {
                            htmltext = "30523-05.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30523-06.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1566, 1);
                            st.giveItems(1567, 1);
                            break;
                        }
                        if (cond > 7 && cond < 12) {
                            htmltext = "30523-07.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30523-08.htm";
                            st.takeItems(1571, -1);
                            st.giveItems(1511, 1);
                            st.giveItems(1060, 100);
                            if (player.isNewbie()) {
                                st.showQuestionMark(26);
                                if (player.isMageClass()) {
                                    st.playTutorialVoice("tutorial_voice_027");
                                    st.giveItems(5790, 3000);
                                } else {
                                    st.playTutorialVoice("tutorial_voice_026");
                                    st.giveItems(5789, 6000);
                                }
                            }
                            st.giveItems(4412, 10);
                            st.giveItems(4413, 10);
                            st.giveItems(4414, 10);
                            st.giveItems(4415, 10);
                            st.giveItems(4416, 10);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30516:
                        if (cond == 1) {
                            htmltext = "30516-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1559, 1);
                            st.giveItems(1560, 1);
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30516-02.htm";
                        break;
                    case 30555:
                        if (cond == 2) {
                            htmltext = "30555-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30555-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30555-04.htm";
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30555-05.htm";
                        break;
                    case 30529:
                        if (cond == 3) {
                            htmltext = "30529-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1561, 1);
                            st.giveItems(1562, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30529-02.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30529-03.htm";
                        break;
                    case 30526:
                        if (cond == 4) {
                            htmltext = "30526-01.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30526-03.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30526-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1563, 1);
                            st.takeItems(1564, -1);
                            st.takeItems(1565, -1);
                            st.giveItems(1566, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30526-05.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30526-06.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1567, 1);
                            st.giveItems(1568, 1);
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30526-07.htm";
                            break;
                        }
                        if (cond > 9)
                            htmltext = "30526-08.htm";
                        break;
                    case 30521:
                        if (cond == 9) {
                            htmltext = "30521-01.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1568, 1);
                            st.giveItems(1569, 1);
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30521-02.htm";
                            break;
                        }
                        if (cond > 10)
                            htmltext = "30521-03.htm";
                        break;
                    case 30522:
                        if (cond == 10) {
                            htmltext = "30522-01.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1569, 1);
                            st.giveItems(1570, 1);
                            break;
                        }
                        if (cond == 11) {
                            htmltext = Rnd.nextBoolean() ? "30522-02.htm" : "30522-04.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "30522-03.htm";
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
            case 20323:
                if (st.getInt("cond") == 5 && st.dropMultipleItems(LEADER_DROPLIST))
                    st.set("cond", "6");
                break;
            case 20324:
                if (st.getInt("cond") == 5 && st.dropMultipleItems(LIEUTENANT_DROPLIST))
                    st.set("cond", "6");
                break;
            case 20480:
                if (st.getInt("cond") == 11 && st.dropItems(1571, 1, 1, 200000)) {
                    st.takeItems(1570, 1);
                    st.set("cond", "12");
                }
                break;
        }
        return null;
    }
}
