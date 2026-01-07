package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q358_IllegitimateChildOfAGoddess extends Quest {
    private static final String qn = "Q358_IllegitimateChildOfAGoddess";

    private static final int SCALE = 5868;

    private static final int[] REWARDS = new int[]{6329, 6331, 6333, 6335, 6337, 6339, 5364, 5366};

    public Q358_IllegitimateChildOfAGoddess() {
        super(358, "Illegitimate Child of a Goddess");
        setItemsIds(5868);
        addStartNpc(30862);
        addTalkId(30862);
        addKillId(20672, 20673);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q358_IllegitimateChildOfAGoddess");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30862-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q358_IllegitimateChildOfAGoddess");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 63) ? "30862-01.htm" : "30862-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30862-06.htm";
                    break;
                }
                htmltext = "30862-07.htm";
                st.takeItems(5868, -1);
                st.giveItems(Rnd.get(REWARDS), 1);
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
        if (st.dropItems(5868, 1, 108, (npc.getNpcId() == 20672) ? 680000 : 660000))
            st.set("cond", "2");
        return null;
    }
}
