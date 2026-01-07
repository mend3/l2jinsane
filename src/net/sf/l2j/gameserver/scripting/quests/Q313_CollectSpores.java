package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q313_CollectSpores extends Quest {
    private static final String qn = "Q313_CollectSpores";

    private static final int SPORE_SAC = 1118;

    public Q313_CollectSpores() {
        super(313, "Collect Spores");
        setItemsIds(1118);
        addStartNpc(30150);
        addTalkId(30150);
        addKillId(20509);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q313_CollectSpores");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30150-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q313_CollectSpores");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 8) ? "30150-02.htm" : "30150-03.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30150-06.htm";
                    break;
                }
                htmltext = "30150-07.htm";
                st.takeItems(1118, -1);
                st.rewardItems(57, 3500);
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
        if (st.dropItems(1118, 1, 10, 400000))
            st.set("cond", "2");
        return null;
    }
}
