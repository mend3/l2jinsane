package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q369_CollectorOfJewels extends Quest {
    private static final String qn = "Q369_CollectorOfJewels";

    private static final int NELL = 30376;

    private static final int FLARE_SHARD = 5882;

    private static final int FREEZING_SHARD = 5883;

    private static final int ADENA = 57;

    private static final Map<Integer, int[]> DROPLIST = new HashMap<>();

    public Q369_CollectorOfJewels() {
        super(369, "Collector of Jewels");
        DROPLIST.put(20609, new int[]{5882, 630000});
        DROPLIST.put(20612, new int[]{5882, 770000});
        DROPLIST.put(20749, new int[]{5882, 850000});
        DROPLIST.put(20616, new int[]{5883, 600000});
        DROPLIST.put(20619, new int[]{5883, 730000});
        DROPLIST.put(20747, new int[]{5883, 850000});
        setItemsIds(5882, 5883);
        addStartNpc(30376);
        addTalkId(30376);
        for (int mob : DROPLIST.keySet()) {
            addKillId(mob);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q369_CollectorOfJewels");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30376-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30376-07.htm")) {
            st.playSound("ItemSound.quest_itemget");
        } else if (event.equalsIgnoreCase("30376-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, flare, freezing;
        QuestState st = player.getQuestState("Q369_CollectorOfJewels");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 25) ? "30376-01.htm" : "30376-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                flare = st.getQuestItemsCount(5882);
                freezing = st.getQuestItemsCount(5883);
                if (cond == 1) {
                    htmltext = "30376-04.htm";
                    break;
                }
                if (cond == 2 && flare >= 50 && freezing >= 50) {
                    htmltext = "30376-05.htm";
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(5882, -1);
                    st.takeItems(5883, -1);
                    st.rewardItems(57, 12500);
                    break;
                }
                if (cond == 3) {
                    htmltext = "30376-09.htm";
                    break;
                }
                if (cond == 4 && flare >= 200 && freezing >= 200) {
                    htmltext = "30376-10.htm";
                    st.takeItems(5882, -1);
                    st.takeItems(5883, -1);
                    st.rewardItems(57, 63500);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(true);
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        int[] drop = DROPLIST.get(npc.getNpcId());
        if (cond == 1) {
            if (st.dropItems(drop[0], 1, 50, drop[1]) && st.getQuestItemsCount((drop[0] == 5882) ? 5883 : 5882) >= 50)
                st.set("cond", "2");
        } else if (cond == 3 && st.dropItems(drop[0], 1, 200, drop[1]) && st.getQuestItemsCount((drop[0] == 5882) ? 5883 : 5882) >= 200) {
            st.set("cond", "4");
        }
        return null;
    }
}
