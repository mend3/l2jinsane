package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q155_FindSirWindawood extends Quest {
    private static final String qn = "Q155_FindSirWindawood";

    private static final int OFFICIAL_LETTER = 1019;

    private static final int HASTE_POTION = 734;

    private static final int ABELLOS = 30042;

    private static final int WINDAWOOD = 30311;

    public Q155_FindSirWindawood() {
        super(155, "Find Sir Windawood");
        setItemsIds(1019);
        addStartNpc(30042);
        addTalkId(30311, 30042);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q155_FindSirWindawood");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30042-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1019, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q155_FindSirWindawood");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 3) ? "30042-01a.htm" : "30042-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30042:
                        htmltext = "30042-03.htm";
                        break;
                    case 30311:
                        if (st.hasQuestItems(1019)) {
                            htmltext = "30311-01.htm";
                            st.takeItems(1019, 1);
                            st.rewardItems(734, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
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
