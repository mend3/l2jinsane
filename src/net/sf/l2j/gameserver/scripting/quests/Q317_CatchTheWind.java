package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q317_CatchTheWind extends Quest {
    private static final String qn = "Q317_CatchTheWind";

    private static final int WIND_SHARD = 1078;

    public Q317_CatchTheWind() {
        super(317, "Catch the Wind");
        setItemsIds(1078);
        addStartNpc(30361);
        addTalkId(30361);
        addKillId(20036, 20044);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q317_CatchTheWind");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30361-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30361-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int shards;
        QuestState st = player.getQuestState("Q317_CatchTheWind");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 18) ? "30361-02.htm" : "30361-03.htm";
                break;
            case 1:
                shards = st.getQuestItemsCount(1078);
                if (shards == 0) {
                    htmltext = "30361-05.htm";
                    break;
                }
                htmltext = "30361-07.htm";
                st.takeItems(1078, -1);
                st.rewardItems(57, 40 * shards + ((shards >= 10) ? 2988 : 0));
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(1078, 1, 0, 500000);
        return null;
    }
}
