package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q051_OFullesSpecialBait extends Quest {
    private static final String qn = "Q051_OFullesSpecialBait";

    private static final int LOST_BAIT = 7622;

    private static final int ICY_AIR_LURE = 7611;

    public Q051_OFullesSpecialBait() {
        super(51, "O'Fulle's Special Bait");
        setItemsIds(7622);
        addStartNpc(31572);
        addTalkId(31572);
        addKillId(20552);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q051_OFullesSpecialBait");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31572-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31572-07.htm")) {
            htmltext = "31572-06.htm";
            st.takeItems(7622, -1);
            st.rewardItems(7611, 4);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q051_OFullesSpecialBait");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 36) ? "31572-02.htm" : "31572-01.htm";
                break;
            case 1:
                htmltext = (st.getQuestItemsCount(7622) == 100) ? "31572-04.htm" : "31572-05.htm";
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItemsAlways(7622, 1, 100))
            st.set("cond", "2");
        return null;
    }
}
