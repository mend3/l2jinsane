package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q431_WeddingMarch extends Quest {
    private static final String qn = "Q431_WeddingMarch";

    private static final int KANTABILON = 31042;

    private static final int SILVER_CRYSTAL = 7540;

    private static final int WEDDING_ECHO_CRYSTAL = 7062;

    public Q431_WeddingMarch() {
        super(431, "Wedding March");
        setItemsIds(7540);
        addStartNpc(31042);
        addTalkId(31042);
        addKillId(20786, 20787);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q431_WeddingMarch");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31042-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31042-05.htm")) {
            if (st.getQuestItemsCount(7540) < 50) {
                htmltext = "31042-03.htm";
            } else {
                st.takeItems(7540, -1);
                st.giveItems(7062, 25);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q431_WeddingMarch");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 38) ? "31042-00.htm" : "31042-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31042-02.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = (st.getQuestItemsCount(7540) < 50) ? "31042-03.htm" : "31042-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(7540, 1, 50, 500000))
            st.set("cond", "2");
        return null;
    }
}
