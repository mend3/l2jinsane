package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q645_GhostsOfBatur extends Quest {
    private static final String qn = "Q645_GhostsOfBatur";

    private static final int KARUDA = 32017;

    private static final int CURSED_GRAVE_GOODS = 8089;

    private static final int[][] REWARDS = new int[][]{{1878, 18}, {1879, 7}, {1880, 4}, {1881, 6}, {1882, 10}, {1883, 2}};

    public Q645_GhostsOfBatur() {
        super(645, "Ghosts Of Batur");
        addStartNpc(32017);
        addTalkId(32017);
        addKillId(22007, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q645_GhostsOfBatur");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32017-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32017-07.htm";
            st.takeItems(8089, -1);
            int[] reward = REWARDS[Integer.parseInt(event)];
            st.giveItems(reward[0], reward[1]);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q645_GhostsOfBatur");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 23) ? "32017-02.htm" : "32017-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32017-04.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32017-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(8089, 1, 180, 750000))
            st.set("cond", "2");
        return null;
    }
}
