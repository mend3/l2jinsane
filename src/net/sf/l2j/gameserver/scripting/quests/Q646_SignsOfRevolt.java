package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q646_SignsOfRevolt extends Quest {
    private static final String qn = "Q646_SignsOfRevolt";

    private static final int TORRANT = 32016;

    private static final int CURSED_DOLL = 8087;

    private static final int[][] REWARDS = new int[][]{{1880, 9}, {1881, 12}, {1882, 20}, {57, 21600}};

    public Q646_SignsOfRevolt() {
        super(646, "Signs Of Revolt");
        setItemsIds(8087);
        addStartNpc(32016);
        addTalkId(32016);
        addKillId(22029, 22030, 22031, 22032, 22033, 22034, 22035, 22036, 22037, 22038,
                22039, 22040, 22041, 22042, 22043, 22044, 22045, 22047, 22049);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q646_SignsOfRevolt");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32016-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32016-07.htm";
            st.takeItems(8087, -1);
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
        QuestState st = player.getQuestState("Q646_SignsOfRevolt");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 40) ? "32016-02.htm" : "32016-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32016-04.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32016-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(8087, 1, 180, 750000))
            st.set("cond", "2");
        return null;
    }
}
