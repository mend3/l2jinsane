package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q360_PlunderTheirSupplies extends Quest {
    private static final String qn = "Q360_PlunderTheirSupplies";

    private static final int SUPPLY_ITEM = 5872;

    private static final int SUSPICIOUS_DOCUMENT = 5871;

    private static final int RECIPE_OF_SUPPLY = 5870;

    private static final int[][][] DROPLIST = new int[][][]{{{5871, 1, 0, 50000}, {5872, 1, 0, 500000}}, {{5871, 1, 0, 50000}, {5872, 1, 0, 660000}}};

    public Q360_PlunderTheirSupplies() {
        super(360, "Plunder Their Supplies");
        setItemsIds(5870, 5872, 5871);
        addStartNpc(30873);
        addTalkId(30873);
        addKillId(20666, 20669);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q360_PlunderTheirSupplies");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30873-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30873-6.htm")) {
            st.takeItems(5872, -1);
            st.takeItems(5871, -1);
            st.takeItems(5870, -1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int supplyItems, reward;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q360_PlunderTheirSupplies");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 52) ? "30873-0a.htm" : "30873-0.htm";
                break;
            case 1:
                supplyItems = st.getQuestItemsCount(5872);
                if (supplyItems == 0) {
                    htmltext = "30873-3.htm";
                    break;
                }
                reward = 6000 + supplyItems * 100 + st.getQuestItemsCount(5870) * 6000;
                htmltext = "30873-5.htm";
                st.takeItems(5872, -1);
                st.takeItems(5870, -1);
                st.rewardItems(57, reward);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropMultipleItems(DROPLIST[(npc.getNpcId() == 20666) ? 0 : 1]);
        if (st.getQuestItemsCount(5871) == 5) {
            st.takeItems(5871, 5);
            st.giveItems(5870, 1);
        }
        return null;
    }
}
