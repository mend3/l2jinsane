package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q357_WarehouseKeepersAmbition extends Quest {
    private static final String qn = "Q357_WarehouseKeepersAmbition";

    private static final int JADE_CRYSTAL = 5867;

    private static final int FOREST_RUNNER = 20594;

    private static final int FLINE_ELDER = 20595;

    private static final int LIELE_ELDER = 20596;

    private static final int VALLEY_TREANT_ELDER = 20597;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q357_WarehouseKeepersAmbition() {
        super(357, "Warehouse Keeper's Ambition");
        CHANCES.put(20594, 400000);
        CHANCES.put(20595, 410000);
        CHANCES.put(20596, 440000);
        CHANCES.put(20597, 650000);
        setItemsIds(5867);
        addStartNpc(30686);
        addTalkId(30686);
        addKillId(20594, 20595, 20596, 20597);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q357_WarehouseKeepersAmbition");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30686-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30686-7.htm")) {
            int count = st.getQuestItemsCount(5867);
            if (count == 0) {
                htmltext = "30686-4.htm";
            } else {
                int reward = count * 425 + 3500;
                if (count >= 100)
                    reward += 7400;
                st.takeItems(5867, -1);
                st.rewardItems(57, reward);
            }
        } else if (event.equalsIgnoreCase("30686-8.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q357_WarehouseKeepersAmbition");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 47) ? "30686-0a.htm" : "30686-0.htm";
                break;
            case 1:
                htmltext = !st.hasQuestItems(5867) ? "30686-4.htm" : "30686-6.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5867, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
