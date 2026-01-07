package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q359_ForSleeplessDeadmen extends Quest {
    private static final String qn = "Q359_ForSleeplessDeadmen";

    private static final int REMAINS = 5869;

    private static final int DOOM_SERVANT = 21006;

    private static final int DOOM_GUARD = 21007;

    private static final int DOOM_ARCHER = 21008;

    private static final int[] REWARDS = new int[]{6341, 6342, 6343, 6344, 6345, 6346, 5494, 5495};

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q359_ForSleeplessDeadmen() {
        super(359, "For Sleepless Deadmen");
        CHANCES.put(Integer.valueOf(21006), Integer.valueOf(320000));
        CHANCES.put(Integer.valueOf(21007), Integer.valueOf(340000));
        CHANCES.put(Integer.valueOf(21008), Integer.valueOf(420000));
        setItemsIds(5869);
        addStartNpc(30857);
        addTalkId(30857);
        addKillId(21006, 21007, 21008);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q359_ForSleeplessDeadmen");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30857-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30857-10.htm")) {
            st.giveItems(Rnd.get(REWARDS), 4);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q359_ForSleeplessDeadmen");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "30857-01.htm" : "30857-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30857-07.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30857-08.htm";
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(5869, -1);
                    break;
                }
                if (cond == 3)
                    htmltext = "30857-09.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(5869, 1, 60, CHANCES.get(Integer.valueOf(npc.getNpcId()))))
            st.set("cond", "2");
        return null;
    }
}
