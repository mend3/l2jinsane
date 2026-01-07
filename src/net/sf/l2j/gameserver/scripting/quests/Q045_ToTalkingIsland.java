package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q045_ToTalkingIsland extends Quest {
    private static final String qn = "Q045_ToTalkingIsland";

    private static final int GALLADUCCI = 30097;

    private static final int GENTLER = 30094;

    private static final int SANDRA = 30090;

    private static final int DUSTIN = 30116;

    private static final int ORDER_DOCUMENT_1 = 7563;

    private static final int ORDER_DOCUMENT_2 = 7564;

    private static final int ORDER_DOCUMENT_3 = 7565;

    private static final int MAGIC_SWORD_HILT = 7568;

    private static final int GEMSTONE_POWDER = 7567;

    private static final int PURIFIED_MAGIC_NECKLACE = 7566;

    private static final int MARK_OF_TRAVELER = 7570;

    private static final int SCROLL_OF_ESCAPE_SPECIAL = 7554;

    public Q045_ToTalkingIsland() {
        super(45, "To Talking Island");
        setItemsIds(7563, 7564, 7565, 7568, 7567, 7566);
        addStartNpc(30097);
        addTalkId(30097, 30094, 30090, 30116);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q045_ToTalkingIsland");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30097-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7563, 1);
        } else if (event.equalsIgnoreCase("30094-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7563, 1);
            st.giveItems(7568, 1);
        } else if (event.equalsIgnoreCase("30097-06.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7568, 1);
            st.giveItems(7564, 1);
        } else if (event.equalsIgnoreCase("30090-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7564, 1);
            st.giveItems(7567, 1);
        } else if (event.equalsIgnoreCase("30097-09.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7567, 1);
            st.giveItems(7565, 1);
        } else if (event.equalsIgnoreCase("30116-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7565, 1);
            st.giveItems(7566, 1);
        } else if (event.equalsIgnoreCase("30097-12.htm")) {
            st.takeItems(7570, -1);
            st.takeItems(7566, 1);
            st.rewardItems(7554, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q045_ToTalkingIsland");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() == ClassRace.HUMAN && player.getLevel() >= 3) {
                    if (st.hasQuestItems(7570)) {
                        htmltext = "30097-02.htm";
                        break;
                    }
                    htmltext = "30097-01.htm";
                    break;
                }
                htmltext = "30097-01a.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30097:
                        if (cond == 1) {
                            htmltext = "30097-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30097-05.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30097-07.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30097-08.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30097-10.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30097-11.htm";
                        break;
                    case 30094:
                        if (cond == 1) {
                            htmltext = "30094-01.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30094-03.htm";
                        break;
                    case 30090:
                        if (cond == 3) {
                            htmltext = "30090-01.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30090-03.htm";
                        break;
                    case 30116:
                        if (cond == 5) {
                            htmltext = "30116-01.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30116-03.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
