package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q324_SweetestVenom extends Quest {
    private static final String qn = "Q324_SweetestVenom";

    private static final int VENOM_SAC = 1077;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q324_SweetestVenom() {
        super(324, "Sweetest Venom");
        CHANCES.put(20034, 220000);
        CHANCES.put(20038, 230000);
        CHANCES.put(20043, 250000);
        setItemsIds(1077);
        addStartNpc(30351);
        addTalkId(30351);
        addKillId(20034, 20038, 20043);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q324_SweetestVenom");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30351-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q324_SweetestVenom");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 18) ? "30351-02.htm" : "30351-03.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30351-05.htm";
                    break;
                }
                htmltext = "30351-06.htm";
                st.takeItems(1077, -1);
                st.rewardItems(57, 5810);
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
        if (st.dropItems(1077, 1, 10, CHANCES.get(npc.getNpcId())))
            st.set("cond", "2");
        return null;
    }
}
