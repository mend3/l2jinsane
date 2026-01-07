package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q649_ALooterAndARailroadMan extends Quest {
    private static final String qn = "Q649_ALooterAndARailroadMan";

    private static final int THIEF_GUILD_MARK = 8099;

    private static final int OBI = 32052;

    public Q649_ALooterAndARailroadMan() {
        super(649, "A Looter and a Railroad Man");
        setItemsIds(8099);
        addStartNpc(32052);
        addTalkId(32052);
        addKillId(22017, 22018, 22019, 22021, 22022, 22023, 22024, 22026);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q649_ALooterAndARailroadMan");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32052-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32052-3.htm")) {
            if (st.getQuestItemsCount(8099) < 200) {
                htmltext = "32052-3a.htm";
            } else {
                st.takeItems(8099, -1);
                st.rewardItems(57, 21698);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q649_ALooterAndARailroadMan");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 30) ? "32052-0a.htm" : "32052-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32052-2a.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32052-2.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(8099, 1, 200, 800000))
            st.set("cond", "2");
        return null;
    }
}
