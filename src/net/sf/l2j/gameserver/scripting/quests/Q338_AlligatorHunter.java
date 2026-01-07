package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q338_AlligatorHunter extends Quest {
    private static final String qn = "Q338_AlligatorHunter";

    private static final int ALLIGATOR_PELT = 4337;

    public Q338_AlligatorHunter() {
        super(338, "Alligator Hunter");
        setItemsIds(4337);
        addStartNpc(30892);
        addTalkId(30892);
        addKillId(20135);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q338_AlligatorHunter");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30892-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30892-05.htm")) {
            int pelts = st.getQuestItemsCount(4337);
            int reward = pelts * 60;
            if (pelts > 10)
                reward += 3430;
            st.takeItems(4337, -1);
            st.rewardItems(57, reward);
        } else if (event.equalsIgnoreCase("30892-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q338_AlligatorHunter");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 40) ? "30892-00.htm" : "30892-01.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(4337) ? "30892-03.htm" : "30892-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItemsAlways(4337, 1, 0);
        return null;
    }
}
