package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q354_ConquestOfAlligatorIsland extends Quest {
    private static final String qn = "Q354_ConquestOfAlligatorIsland";

    private static final int ALLIGATOR_TOOTH = 5863;

    private static final int TORN_MAP_FRAGMENT = 5864;

    private static final int PIRATE_TREASURE_MAP = 5915;

    private static final Map<Integer, int[][]> DROPLIST = new HashMap<>();

    public Q354_ConquestOfAlligatorIsland() {
        super(354, "Conquest of Alligator Island");
        DROPLIST.put(Integer.valueOf(20804), new int[][]{{5863, 1, 0, 490000}, {5864, 1, 0, 100000}});
        DROPLIST.put(Integer.valueOf(20805), new int[][]{{5863, 1, 0, 560000}, {5864, 1, 0, 100000}});
        DROPLIST.put(Integer.valueOf(20806), new int[][]{{5863, 1, 0, 500000}, {5864, 1, 0, 100000}});
        DROPLIST.put(Integer.valueOf(20807), new int[][]{{5863, 1, 0, 600000}, {5864, 1, 0, 100000}});
        DROPLIST.put(Integer.valueOf(20808), new int[][]{{5863, 1, 0, 690000}, {5864, 1, 0, 100000}});
        DROPLIST.put(Integer.valueOf(20991), new int[][]{{5863, 1, 0, 600000}, {5864, 1, 0, 100000}});
        setItemsIds(5863, 5864);
        addStartNpc(30895);
        addTalkId(30895);
        addKillId(20804, 20805, 20806, 20807, 20808, 20991);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q354_ConquestOfAlligatorIsland");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30895-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30895-03.htm")) {
            if (st.hasQuestItems(5864))
                htmltext = "30895-03a.htm";
        } else if (event.equalsIgnoreCase("30895-05.htm")) {
            int amount = st.getQuestItemsCount(5863);
            if (amount > 0) {
                int reward = amount * 220 + 3100;
                if (amount >= 100) {
                    reward += 7600;
                    htmltext = "30895-05b.htm";
                } else {
                    htmltext = "30895-05a.htm";
                }
                st.takeItems(5863, -1);
                st.rewardItems(57, reward);
            }
        } else if (event.equalsIgnoreCase("30895-07.htm")) {
            if (st.getQuestItemsCount(5864) >= 10) {
                htmltext = "30895-08.htm";
                st.takeItems(5864, 10);
                st.giveItems(5915, 1);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30895-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q354_ConquestOfAlligatorIsland");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 38) ? "30895-00.htm" : "30895-01.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(5864) ? "30895-03a.htm" : "30895-03.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropMultipleItems(DROPLIST.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
