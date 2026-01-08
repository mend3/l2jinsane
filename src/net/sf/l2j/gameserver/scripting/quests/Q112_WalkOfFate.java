package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q112_WalkOfFate extends Quest {
    private static final String qn = "Q112_WalkOfFate";

    private static final int LIVINA = 30572;

    private static final int KARUDA = 32017;

    private static final int ENCHANT_D = 956;

    public Q112_WalkOfFate() {
        super(112, "Walk of Fate");
        addStartNpc(30572);
        addTalkId(30572, 32017);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q112_WalkOfFate");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30572-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32017-02.htm")) {
            st.giveItems(956, 1);
            st.rewardItems(57, 4665);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q112_WalkOfFate");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "30572-00.htm" : "30572-01.htm";
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 30572 -> "30572-03.htm";
                    case 32017 -> "32017-01.htm";
                    default -> htmltext;
                };
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
