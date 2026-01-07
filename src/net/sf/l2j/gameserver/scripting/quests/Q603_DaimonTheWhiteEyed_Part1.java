package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q603_DaimonTheWhiteEyed_Part1 extends Quest {
    private static final String qn = "Q603_DaimonTheWhiteEyed_Part1";

    private static final int EVIL_SPIRIT_BEADS = 7190;

    private static final int BROKEN_CRYSTAL = 7191;

    private static final int UNFINISHED_SUMMON_CRYSTAL = 7192;

    private static final int EYE_OF_ARGOS = 31683;

    private static final int MYSTERIOUS_TABLET_1 = 31548;

    private static final int MYSTERIOUS_TABLET_2 = 31549;

    private static final int MYSTERIOUS_TABLET_3 = 31550;

    private static final int MYSTERIOUS_TABLET_4 = 31551;

    private static final int MYSTERIOUS_TABLET_5 = 31552;

    private static final int CANYON_BANDERSNATCH_SLAVE = 21297;

    private static final int BUFFALO_SLAVE = 21299;

    private static final int GRENDEL_SLAVE = 21304;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q603_DaimonTheWhiteEyed_Part1() {
        super(603, "Daimon the White-Eyed - Part 1");
        CHANCES.put(21297, 500000);
        CHANCES.put(21299, 519000);
        CHANCES.put(21304, 673000);
        setItemsIds(7190, 7191);
        addStartNpc(31683);
        addTalkId(31683, 31548, 31549, 31550, 31551, 31552);
        addKillId(21299, 21304, 21297);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q603_DaimonTheWhiteEyed_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31683-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31683-06.htm")) {
            if (st.getQuestItemsCount(7191) > 4) {
                st.set("cond", "7");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7191, -1);
            } else {
                htmltext = "31683-07.htm";
            }
        } else if (event.equalsIgnoreCase("31683-10.htm")) {
            if (st.getQuestItemsCount(7190) > 199) {
                st.takeItems(7190, -1);
                st.giveItems(7192, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                st.set("cond", "7");
                htmltext = "31683-11.htm";
            }
        } else if (event.equalsIgnoreCase("31548-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7191, 1);
        } else if (event.equalsIgnoreCase("31549-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7191, 1);
        } else if (event.equalsIgnoreCase("31550-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7191, 1);
        } else if (event.equalsIgnoreCase("31551-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7191, 1);
        } else if (event.equalsIgnoreCase("31552-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7191, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q603_DaimonTheWhiteEyed_Part1");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31683-02.htm" : "31683-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31683:
                        if (cond < 6) {
                            htmltext = "31683-04.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "31683-05.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "31683-08.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "31683-09.htm";
                        break;
                    case 31548:
                        if (cond == 1) {
                            htmltext = "31548-01.htm";
                            break;
                        }
                        htmltext = "31548-03.htm";
                        break;
                    case 31549:
                        if (cond == 2) {
                            htmltext = "31549-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "31549-03.htm";
                        break;
                    case 31550:
                        if (cond == 3) {
                            htmltext = "31550-01.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31550-03.htm";
                        break;
                    case 31551:
                        if (cond == 4) {
                            htmltext = "31551-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "31551-03.htm";
                        break;
                    case 31552:
                        if (cond == 5) {
                            htmltext = "31552-01.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "31552-03.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "7");
        if (st == null)
            return null;
        if (st.dropItems(7190, 1, 200, CHANCES.get(npc.getNpcId())))
            st.set("cond", "8");
        return null;
    }
}
