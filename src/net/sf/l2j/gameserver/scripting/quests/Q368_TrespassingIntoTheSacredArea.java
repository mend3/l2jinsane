package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q368_TrespassingIntoTheSacredArea extends Quest {
    private static final String qn = "Q368_TrespassingIntoTheSacredArea";

    private static final int RESTINA = 30926;

    private static final int FANG = 5881;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q368_TrespassingIntoTheSacredArea() {
        super(368, "Trespassing into the Sacred Area");
        CHANCES.put(20794, 500000);
        CHANCES.put(20795, 770000);
        CHANCES.put(20796, 500000);
        CHANCES.put(20797, 480000);
        setItemsIds(5881);
        addStartNpc(30926);
        addTalkId(30926);
        addKillId(20794, 20795, 20796, 20797);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q368_TrespassingIntoTheSacredArea");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30926-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30926-05.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int fangs, reward;
        QuestState st = player.getQuestState("Q368_TrespassingIntoTheSacredArea");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 36) ? "30926-01a.htm" : "30926-01.htm";
                break;
            case 1:
                fangs = st.getQuestItemsCount(5881);
                if (fangs == 0) {
                    htmltext = "30926-03.htm";
                    break;
                }
                reward = 250 * fangs + ((fangs > 10) ? 5730 : 2000);
                htmltext = "30926-04.htm";
                st.takeItems(5881, -1);
                st.rewardItems(57, reward);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5881, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
