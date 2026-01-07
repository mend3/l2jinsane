package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q297_GatekeepersFavor extends Quest {
    private static final String qn = "Q297_GatekeepersFavor";

    private static final int STARSTONE = 1573;

    private static final int GATEKEEPER_TOKEN = 1659;

    public Q297_GatekeepersFavor() {
        super(297, "Gatekeeper's Favor");
        setItemsIds(1573);
        addStartNpc(30540);
        addTalkId(30540);
        addKillId(20521);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q297_GatekeepersFavor");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30540-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q297_GatekeepersFavor");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30540-01.htm" : "30540-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30540-04.htm";
                    break;
                }
                htmltext = "30540-05.htm";
                st.takeItems(1573, -1);
                st.rewardItems(1659, 2);
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
        if (st.dropItems(1573, 1, 20, 500000))
            st.set("cond", "2");
        return null;
    }
}
