package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q052_WilliesSpecialBait extends Quest {
    private static final String qn = "Q052_WilliesSpecialBait";

    private static final int TARLK_EYE = 7623;

    private static final int EARTH_FISHING_LURE = 7612;

    public Q052_WilliesSpecialBait() {
        super(52, "Willie's Special Bait");
        setItemsIds(7623);
        addStartNpc(31574);
        addTalkId(31574);
        addKillId(20573);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q052_WilliesSpecialBait");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31574-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31574-07.htm")) {
            htmltext = "31574-06.htm";
            st.takeItems(7623, -1);
            st.rewardItems(7612, 4);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q052_WilliesSpecialBait");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 48) ? "31574-02.htm" : "31574-01.htm";
                break;
            case 1:
                htmltext = (st.getQuestItemsCount(7623) == 100) ? "31574-04.htm" : "31574-05.htm";
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(7623, 1, 100, 500000))
            st.set("cond", "2");
        return null;
    }
}
