package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q113_StatusOfTheBeaconTower extends Quest {
    private static final String qn = "Q113_StatusOfTheBeaconTower";

    private static final int MOIRA = 31979;

    private static final int TORRANT = 32016;

    private static final int BOX = 8086;

    public Q113_StatusOfTheBeaconTower() {
        super(113, "Status of the Beacon Tower");
        setItemsIds(8086);
        addStartNpc(31979);
        addTalkId(31979, 32016);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q113_StatusOfTheBeaconTower");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31979-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(8086, 1);
        } else if (event.equalsIgnoreCase("32016-02.htm")) {
            st.takeItems(8086, 1);
            st.rewardItems(57, 21578);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q113_StatusOfTheBeaconTower");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 40) ? "31979-00.htm" : "31979-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31979:
                        htmltext = "31979-03.htm";
                        break;
                    case 32016:
                        htmltext = "32016-01.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
