package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q050_LanoscosSpecialBait extends Quest {
    private static final String qn = "Q050_LanoscosSpecialBait";

    private static final int ESSENCE_OF_WIND = 7621;

    private static final int WIND_FISHING_LURE = 7610;

    public Q050_LanoscosSpecialBait() {
        super(50, "Lanosco's Special Bait");
        setItemsIds(7621);
        addStartNpc(31570);
        addTalkId(31570);
        addKillId(21026);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q050_LanoscosSpecialBait");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31570-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31570-07.htm")) {
            htmltext = "31570-06.htm";
            st.takeItems(7621, -1);
            st.rewardItems(7610, 4);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q050_LanoscosSpecialBait");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 27) ? "31570-02.htm" : "31570-01.htm";
                break;
            case 1:
                htmltext = (st.getQuestItemsCount(7621) == 100) ? "31570-04.htm" : "31570-05.htm";
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
        if (st.dropItems(7621, 1, 100, 500000))
            st.set("cond", "2");
        return null;
    }
}
