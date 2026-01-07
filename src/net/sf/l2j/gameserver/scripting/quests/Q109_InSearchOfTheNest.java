package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q109_InSearchOfTheNest extends Quest {
    private static final String qn = "Q109_InSearchOfTheNest";

    private static final int PIERCE = 31553;

    private static final int KAHMAN = 31554;

    private static final int SCOUT_CORPSE = 32015;

    private static final int SCOUT_MEMO = 8083;

    private static final int RECRUIT_BADGE = 7246;

    private static final int SOLDIER_BADGE = 7247;

    public Q109_InSearchOfTheNest() {
        super(109, "In Search of the Nest");
        setItemsIds(8083);
        addStartNpc(31553);
        addTalkId(31553, 32015, 31554);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q109_InSearchOfTheNest");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31553-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32015-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8083, 1);
        } else if (event.equalsIgnoreCase("31553-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8083, 1);
        } else if (event.equalsIgnoreCase("31554-02.htm")) {
            st.rewardItems(57, 5168);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q109_InSearchOfTheNest");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 66 && st.hasAtLeastOneQuestItem(7246, 7247)) {
                    htmltext = "31553-00.htm";
                    break;
                }
                htmltext = "31553-00a.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31553:
                        if (cond == 1) {
                            htmltext = "31553-01a.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31553-02.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "31553-03.htm";
                        break;
                    case 32015:
                        if (cond == 1) {
                            htmltext = "32015-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "32015-02.htm";
                        break;
                    case 31554:
                        if (cond == 3)
                            htmltext = "31554-01.htm";
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
