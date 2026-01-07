package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q661_MakingTheHarvestGroundsSafe extends Quest {
    private static final String qn = "Q661_MakingTheHarvestGroundsSafe";

    private static final int NORMAN = 30210;

    private static final int STING_OF_GIANT_POISON_BEE = 8283;

    private static final int CLOUDY_GEM = 8284;

    private static final int TALON_OF_YOUNG_ARANEID = 8285;

    private static final int ADENA = 57;

    private static final int GIANT_POISON_BEE = 21095;

    private static final int CLOUDY_BEAST = 21096;

    private static final int YOUNG_ARANEID = 21097;

    public Q661_MakingTheHarvestGroundsSafe() {
        super(661, "Making the Harvest Grounds Safe");
        setItemsIds(8283, 8284, 8285);
        addStartNpc(30210);
        addTalkId(30210);
        addKillId(21095, 21096, 21097);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q661_MakingTheHarvestGroundsSafe");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30210-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30210-04.htm")) {
            int item1 = st.getQuestItemsCount(8283);
            int item2 = st.getQuestItemsCount(8284);
            int item3 = st.getQuestItemsCount(8285);
            int sum = 0;
            sum = item1 * 57 + item2 * 56 + item3 * 60;
            if (item1 + item2 + item3 >= 10)
                sum += 2871;
            st.takeItems(8283, item1);
            st.takeItems(8284, item2);
            st.takeItems(8285, item3);
            st.rewardItems(57, sum);
        } else if (event.equalsIgnoreCase("30210-06.htm")) {
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q661_MakingTheHarvestGroundsSafe");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 21) ? "30210-01a.htm" : "30210-01.htm";
                break;
            case 1:
                htmltext = st.hasAtLeastOneQuestItem(8283, 8284, 8285) ? "30210-03.htm" : "30210-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(npc.getNpcId() - 12812, 1, 0, 500000);
        return null;
    }
}
