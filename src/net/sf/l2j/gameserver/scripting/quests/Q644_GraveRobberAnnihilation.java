package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q644_GraveRobberAnnihilation extends Quest {
    private static final String qn = "Q644_GraveRobberAnnihilation";

    private static final int ORC_GRAVE_GOODS = 8088;

    private static final int[][] REWARDS = new int[][]{{1865, 30}, {1867, 40}, {1872, 40}, {1871, 30}, {1870, 30}, {1869, 30}};

    private static final int KARUDA = 32017;

    public Q644_GraveRobberAnnihilation() {
        super(644, "Grave Robber Annihilation");
        setItemsIds(8088);
        addStartNpc(32017);
        addTalkId(32017);
        addKillId(22003, 22004, 22005, 22006, 22008);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q644_GraveRobberAnnihilation");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32017-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32017-04.htm";
            st.takeItems(8088, -1);
            int[] reward = REWARDS[Integer.parseInt(event)];
            st.rewardItems(reward[0], reward[1]);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q644_GraveRobberAnnihilation");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "32017-06.htm" : "32017-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32017-05.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32017-07.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(8088, 1, 120, 500000))
            st.set("cond", "2");
        return null;
    }
}
