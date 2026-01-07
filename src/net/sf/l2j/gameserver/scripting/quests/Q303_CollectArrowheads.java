package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q303_CollectArrowheads extends Quest {
    private static final String qn = "Q303_CollectArrowheads";

    private static final int ORCISH_ARROWHEAD = 963;

    public Q303_CollectArrowheads() {
        super(303, "Collect Arrowheads");
        setItemsIds(963);
        addStartNpc(30029);
        addTalkId(30029);
        addKillId(20361);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q303_CollectArrowheads");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30029-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q303_CollectArrowheads");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 10) ? "30029-01.htm" : "30029-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30029-04.htm";
                    break;
                }
                htmltext = "30029-05.htm";
                st.takeItems(963, -1);
                st.rewardItems(57, 1000);
                st.rewardExpAndSp(2000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(963, 1, 10, 400000))
            st.set("cond", "2");
        return null;
    }
}
