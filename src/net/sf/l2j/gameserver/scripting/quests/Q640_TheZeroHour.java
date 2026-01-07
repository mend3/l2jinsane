package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q640_TheZeroHour extends Quest {
    private static final String qn = "Q640_TheZeroHour";

    private static final int KAHMAN = 31554;

    private static final int FANG_OF_STAKATO = 8085;

    private static final int[][] REWARDS = new int[][]{{12, 4042, 1}, {6, 4043, 1}, {6, 4044, 1}, {81, 1887, 10}, {33, 1888, 5}, {30, 1889, 10}, {150, 5550, 10}, {131, 1890, 10}, {123, 1893, 5}};

    public Q640_TheZeroHour() {
        super(640, "The Zero Hour");
        setItemsIds(8085);
        addStartNpc(31554);
        addTalkId(31554);
        addKillId(22105, 22106, 22107, 22108, 22109, 22110, 22111, 22113, 22114, 22115,
                22116, 22117, 22118, 22119, 22121);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q640_TheZeroHour");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31554-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31554-05.htm")) {
            if (!st.hasQuestItems(8085))
                htmltext = "31554-06.htm";
        } else if (event.equalsIgnoreCase("31554-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (StringUtil.isDigit(event)) {
            int[] reward = REWARDS[Integer.parseInt(event)];
            if (st.getQuestItemsCount(8085) >= reward[0]) {
                htmltext = "31554-09.htm";
                st.takeItems(8085, reward[0]);
                st.rewardItems(reward[1], reward[2]);
            } else {
                htmltext = "31554-06.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2, st = player.getQuestState("Q640_TheZeroHour");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 66) {
                    htmltext = "31554-00.htm";
                    break;
                }
                st2 = player.getQuestState("Q109_InSearchOfTheNest");
                htmltext = (st2 != null && st2.isCompleted()) ? "31554-01.htm" : "31554-10.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(8085) ? "31554-04.htm" : "31554-03.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItemsAlways(8085, 1, 0);
        return null;
    }
}
