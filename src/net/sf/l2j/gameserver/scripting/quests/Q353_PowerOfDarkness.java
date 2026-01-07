package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q353_PowerOfDarkness extends Quest {
    private static final String qn = "Q353_PowerOfDarkness";

    private static final int STONE = 5862;

    public Q353_PowerOfDarkness() {
        super(353, "Power of Darkness");
        setItemsIds(5862);
        addStartNpc(31044);
        addTalkId(31044);
        addKillId(20244, 20245, 20283, 20284);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q353_PowerOfDarkness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31044-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31044-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int stones;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q353_PowerOfDarkness");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 55) ? "31044-01.htm" : "31044-02.htm";
                break;
            case 1:
                stones = st.getQuestItemsCount(5862);
                if (stones == 0) {
                    htmltext = "31044-05.htm";
                    break;
                }
                htmltext = "31044-06.htm";
                st.takeItems(5862, -1);
                st.rewardItems(57, 2500 + 230 * stones);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5862, 1, 0, (npc.getNpcId() == 20244 || npc.getNpcId() == 20283) ? 480000 : 500000);
        return null;
    }
}
